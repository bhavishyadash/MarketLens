package com.example.marketlens.data.repository

import com.example.marketlens.data.QuoteCache
import com.example.marketlens.data.model.SearchResult
import com.example.marketlens.data.model.StockCandle
import com.example.marketlens.data.model.StockProfile
import com.example.marketlens.data.model.StockQuote
import com.example.marketlens.data.network.ApiResult
import com.example.marketlens.data.network.YahooCrumbProvider
import com.example.marketlens.data.network.YahooFinanceApi
import com.example.marketlens.data.network.dto.YahooQuoteSummaryResultDto
import com.example.marketlens.data.network.dto.YahooSearchQuoteDto
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import retrofit2.HttpException
import java.io.IOException
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

class RealMarketRepository(
    private val yahoo: YahooFinanceApi,
    private val session: YahooCrumbProvider? = null
) : MarketRepository {

    private data class CachedProfile(val value: StockProfile, val cachedAtMs: Long)

    private val profileCache = ConcurrentHashMap<String, CachedProfile>()
    private val requestSemaphore = Semaphore(MAX_CONCURRENT_REQUESTS)

    override suspend fun getQuote(symbol: String): ApiResult<StockQuote> {
        val normalized = normalizeSymbol(symbol)
        QuoteCache.get(normalized)?.let { return ApiResult.Success(it) }

        return try {
            val response = retryYahoo { yahoo.getChart(normalized, interval = "1d", range = "5d") }
            val result = response.chart?.result?.firstOrNull()
                ?: return ApiResult.Error(chartError(response.chart?.error?.description, normalized))

            val meta = result.meta
            val closes = result.indicators?.quote?.firstOrNull()?.close.orEmpty()
            val price = meta?.regularMarketPrice ?: closes.lastOrNull { it != null }
                ?: return ApiResult.Error("Yahoo Finance returned no current price for $normalized.")

            val previousClose = meta?.previousClose
                ?: closes.filterNotNull().dropLast(1).lastOrNull()
                ?: meta?.chartPreviousClose

            val percentChange = meta?.regularMarketChangePercent
                ?: previousClose
                    ?.takeIf { it != 0.0 }
                    ?.let { ((price - it) / it) * 100.0 }
                ?: 0.0

            val quote = StockQuote(
                symbol = meta?.symbol ?: normalized,
                name = meta?.longName ?: meta?.shortName ?: normalized,
                price = price,
                percentChange = percentChange
            )
            QuoteCache.put(quote)
            ApiResult.Success(quote)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            ApiResult.Error("Could not load $normalized from Yahoo Finance. ${friendlyCause(e)}", e)
        }
    }

    override suspend fun searchSymbols(query: String): ApiResult<List<SearchResult>> {
        val cleaned = query.trim()
        if (cleaned.isEmpty()) return ApiResult.Success(emptyList())

        return try {
            val response = retryYahoo {
                yahoo.search(
                    query = cleaned,
                    quotesCount = SEARCH_RESULT_LIMIT,
                    newsCount = 0,
                    enableFuzzyQuery = false
                )
            }

            val results = response.quotes.orEmpty()
                .asSequence()
                .filter(::isSupportedSecurity)
                .filter { it.isYahooFinance != false }
                .mapNotNull { dto ->
                    val resultSymbol = dto.symbol?.trim()?.takeIf { it.isNotEmpty() }
                        ?: return@mapNotNull null
                    SearchResult(
                        symbol = resultSymbol,
                        description = dto.longName ?: dto.shortName ?: resultSymbol,
                        type = dto.typeDisplay ?: dto.quoteType ?: "Equity"
                    )
                }
                .distinctBy { it.symbol.uppercase(Locale.US) }
                .take(SEARCH_RESULT_LIMIT)
                .toList()

            ApiResult.Success(results)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            ApiResult.Error("Could not search Yahoo Finance. ${friendlyCause(e)}", e)
        }
    }

    override suspend fun getCandles(
        symbol: String,
        resolution: String,
        from: Long,
        to: Long
    ): ApiResult<StockCandle> {
        val normalized = normalizeSymbol(symbol)

        return try {
            val daysBack = ((to - from).coerceAtLeast(0L) / SECONDS_PER_DAY)
            val (interval, range) = intervalAndRange(resolution, daysBack)
            val response = retryYahoo { yahoo.getChart(normalized, interval, range) }
            val result = response.chart?.result?.firstOrNull()
                ?: return ApiResult.Error(chartError(response.chart?.error?.description, normalized))

            val timestamps = result.timestamps.orEmpty()
            val closes = result.indicators?.quote?.firstOrNull()?.close.orEmpty()
            val pointCount = minOf(timestamps.size, closes.size)
            val points = (0 until pointCount).mapNotNull { index ->
                val close = closes[index]
                if (close == null || !close.isFinite()) null else timestamps[index] to close
            }

            if (points.isEmpty()) {
                return ApiResult.Error("Yahoo Finance returned no chart data for $normalized.")
            }

            ApiResult.Success(
                StockCandle(
                    timestamps = points.map { it.first },
                    closePrices = points.map { it.second },
                    status = "ok"
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            ApiResult.Error("Could not load the $normalized chart from Yahoo Finance. ${friendlyCause(e)}", e)
        }
    }

    override suspend fun getStockProfile(symbol: String): ApiResult<StockProfile> {
        val normalized = normalizeSymbol(symbol)
        profileCache[normalized]
            ?.takeIf { System.currentTimeMillis() - it.cachedAtMs < PROFILE_TTL_MS }
            ?.let { return ApiResult.Success(it.value) }

        return try {
            coroutineScope {
                val chartDeferred = async {
                    requestOrNull { yahoo.getChart(normalized, interval = "1d", range = "1y") }
                }
                val searchDeferred = async {
                    requestOrNull {
                        yahoo.search(
                            query = normalized,
                            quotesCount = 8,
                            newsCount = 0,
                            enableFuzzyQuery = false
                        )
                    }
                }
                val summaryDeferred = async { getQuoteSummaryOrNull(normalized) }

                val chartResult = chartDeferred.await()?.chart?.result?.firstOrNull()
                val exactSearchMatch = searchDeferred.await()?.quotes.orEmpty().firstOrNull {
                    it.symbol.equals(normalized, ignoreCase = true)
                }
                val summary = summaryDeferred.await()
                val meta = chartResult?.meta
                val detail = summary?.summaryDetail
                val price = summary?.price

                if (meta == null && exactSearchMatch == null && summary == null) {
                    return@coroutineScope ApiResult.Error("No Yahoo Finance profile is available for $normalized.")
                }

                val profile = StockProfile(
                    symbol = price?.symbol ?: meta?.symbol ?: exactSearchMatch?.symbol ?: normalized,
                    name = price?.longName
                        ?: price?.shortName
                        ?: meta?.longName
                        ?: meta?.shortName
                        ?: exactSearchMatch?.longName
                        ?: exactSearchMatch?.shortName
                        ?: normalized,
                    exchange = price?.exchangeName
                        ?: price?.exchange
                        ?: meta?.fullExchangeName
                        ?: meta?.exchangeName
                        ?: exactSearchMatch?.exchangeDisplay
                        ?: exactSearchMatch?.exchange
                        ?: "N/A",
                    industry = summary?.assetProfile?.industry
                        ?: exactSearchMatch?.industry
                        ?: summary?.assetProfile?.sector
                        ?: exactSearchMatch?.sector
                        ?: "N/A",
                    marketCapFormatted = formatMarketCap(
                        detail?.marketCap?.raw ?: price?.marketCap?.raw
                    ),
                    week52High = detail?.fiftyTwoWeekHigh?.raw ?: meta?.fiftyTwoWeekHigh,
                    week52Low = detail?.fiftyTwoWeekLow?.raw ?: meta?.fiftyTwoWeekLow,
                    peRatio = detail?.trailingPe?.raw,
                    beta = summary?.defaultKeyStatistics?.beta?.raw ?: detail?.beta?.raw
                )

                profileCache[normalized] = CachedProfile(profile, System.currentTimeMillis())
                ApiResult.Success(profile)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            ApiResult.Error("Could not load the $normalized profile from Yahoo Finance. ${friendlyCause(e)}", e)
        }
    }

    private suspend fun getQuoteSummaryOrNull(symbol: String): YahooQuoteSummaryResultDto? {
        val crumbProvider = session ?: return null
        val firstCrumb = crumbProvider.getCrumb() ?: return null

        return try {
            retryYahoo { yahoo.getQuoteSummary(symbol = symbol, crumb = firstCrumb) }
                .quoteSummary
                ?.result
                ?.firstOrNull()
        } catch (e: CancellationException) {
            throw e
        } catch (e: HttpException) {
            if (e.code() != 401 && e.code() != 403) return null

            crumbProvider.invalidate()
            val refreshedCrumb = crumbProvider.getCrumb(forceRefresh = true) ?: return null
            requestOrNull { yahoo.getQuoteSummary(symbol = symbol, crumb = refreshedCrumb) }
                ?.quoteSummary
                ?.result
                ?.firstOrNull()
        } catch (_: Exception) {
            null
        }
    }

    private suspend fun <T> requestOrNull(block: suspend () -> T): T? = try {
        retryYahoo(block)
    } catch (e: CancellationException) {
        throw e
    } catch (_: Exception) {
        null
    }

    private suspend fun <T> retryYahoo(block: suspend () -> T): T {
        var attempt = 0
        while (true) {
            try {
                return requestSemaphore.withPermit { block() }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (attempt >= MAX_RETRY_COUNT || !isRetriable(e)) throw e
                val retryAfterMs = (e as? HttpException)
                    ?.response()
                    ?.headers()
                    ?.get("Retry-After")
                    ?.toLongOrNull()
                    ?.times(1_000L)
                delay((retryAfterMs ?: RETRY_DELAYS_MS[attempt]).coerceAtMost(MAX_RETRY_DELAY_MS))
                attempt++
            }
        }
    }

    private fun isRetriable(error: Exception): Boolean = when (error) {
        is IOException -> true
        is HttpException -> error.code() == 429 || error.code() in 500..599
        else -> false
    }

    private fun isSupportedSecurity(dto: YahooSearchQuoteDto): Boolean {
        val quoteType = dto.quoteType?.uppercase(Locale.US)
        val displayType = dto.typeDisplay?.uppercase(Locale.US)
        return quoteType in SUPPORTED_QUOTE_TYPES || displayType in SUPPORTED_DISPLAY_TYPES
    }

    private fun intervalAndRange(resolution: String, daysBack: Long): Pair<String, String> = when {
        resolution.equals("W", ignoreCase = true) ->
            "1wk" to if (daysBack > 400L) "2y" else "1y"
        daysBack <= 7L -> "5m" to "5d"
        daysBack <= 31L -> "1d" to "1mo"
        daysBack <= 93L -> "1d" to "3mo"
        daysBack <= 186L -> "1d" to "6mo"
        daysBack <= 370L -> "1d" to "1y"
        else -> "1wk" to "2y"
    }

    private fun formatMarketCap(value: Double?): String {
        if (value == null || !value.isFinite() || value <= 0.0) return "N/A"
        return when {
            value >= 1_000_000_000_000.0 -> "$%.2fT".format(Locale.US, value / 1_000_000_000_000.0)
            value >= 1_000_000_000.0 -> "$%.1fB".format(Locale.US, value / 1_000_000_000.0)
            value >= 1_000_000.0 -> "$%.1fM".format(Locale.US, value / 1_000_000.0)
            else -> "$%,.0f".format(Locale.US, value)
        }
    }

    private fun chartError(description: String?, symbol: String): String =
        description?.takeIf { it.isNotBlank() }
            ?: "Yahoo Finance returned no data for $symbol."

    private fun friendlyCause(error: Exception): String = when (error) {
        is HttpException -> when (error.code()) {
            401, 403 -> "Yahoo rejected the request; please retry."
            404 -> "The symbol was not found."
            429 -> "Yahoo is rate-limiting requests; please wait and retry."
            else -> "Yahoo returned HTTP ${error.code()}."
        }
        is IOException -> "Check your internet connection and retry."
        else -> error.message?.takeIf { it.isNotBlank() } ?: "Please retry."
    }

    private fun normalizeSymbol(symbol: String): String = symbol.trim().uppercase(Locale.US)

    private companion object {
        const val SECONDS_PER_DAY = 86_400L
        const val SEARCH_RESULT_LIMIT = 20
        const val MAX_CONCURRENT_REQUESTS = 4
        const val PROFILE_TTL_MS = 6 * 60 * 60 * 1000L
        const val MAX_RETRY_COUNT = 2
        const val MAX_RETRY_DELAY_MS = 5_000L
        val RETRY_DELAYS_MS = longArrayOf(500L, 1_200L)
        val SUPPORTED_QUOTE_TYPES = setOf("EQUITY", "ETF")
        val SUPPORTED_DISPLAY_TYPES = setOf("EQUITY", "ETF")
    }
}
