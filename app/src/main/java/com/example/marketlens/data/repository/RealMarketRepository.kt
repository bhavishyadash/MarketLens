package com.example.marketlens.data.repository

import com.example.marketlens.data.QuoteCache
import com.example.marketlens.data.model.SearchResult
import com.example.marketlens.data.model.StockCandle
import com.example.marketlens.data.model.StockProfile
import com.example.marketlens.data.model.StockQuote
import com.example.marketlens.data.network.ApiResult
import com.example.marketlens.data.network.MarketApi
import com.example.marketlens.data.network.YahooFinanceApi
import kotlinx.coroutines.delay

class RealMarketRepository(
    private val api:   MarketApi,
    private val yahoo: YahooFinanceApi
) : MarketRepository {


    override suspend fun getQuote(symbol: String): ApiResult<StockQuote> {
        QuoteCache.get(symbol)?.let { return ApiResult.Success(it) }

        return retryWithBackoff {
            val dto = api.getQuote(symbol)
            val quote = StockQuote(symbol, symbol, dto.currentPrice, dto.percentChange)
            QuoteCache.put(quote)
            ApiResult.Success(quote)
        }
    }


    override suspend fun searchSymbols(query: String): ApiResult<List<SearchResult>> {
        return retryWithBackoff {
            val dto = api.searchSymbols(query)
            val results = dto.result
                .filter { it.type == "Common Stock" || it.type == "ETP" }
                .map { SearchResult(it.symbol, it.description, it.type) }
            ApiResult.Success(results)
        }
    }


    override suspend fun getCandles(
        symbol: String, resolution: String, from: Long, to: Long
    ): ApiResult<StockCandle> {
        return try {
            val daysBack = (to - from) / 86400L
            val (interval, range) = when {
                resolution == "W"    -> "1wk" to "2y"
                daysBack > 60        -> "1d"  to "3mo"
                else                 -> "1d"  to "1mo"
            }

            val response = yahoo.getChart(symbol, interval, range)
            val result   = response.chart.result?.firstOrNull()
                ?: return ApiResult.Error("No chart data available for $symbol")

            val closePrices = result.indicators.quote
                .firstOrNull()
                ?.close
                ?.filterNotNull()
                ?: return ApiResult.Error("No price data for $symbol")

            if (closePrices.isEmpty()) return ApiResult.Error("No chart data for $symbol")

            val timestamps = closePrices.indices.map { it.toLong() }
            ApiResult.Success(StockCandle(timestamps, closePrices, "ok"))

        } catch (e: Exception) {
            ApiResult.Error("Could not load chart for $symbol: ${e.message}", e)
        }
    }


    override suspend fun getStockProfile(symbol: String): ApiResult<StockProfile> {
        return retryWithBackoff {
            val profileDto = api.getStockProfile(symbol)
            val metricDto  = api.getStockMetric(symbol).metric
            ApiResult.Success(
                StockProfile(
                    symbol             = symbol,
                    name               = profileDto.name,
                    exchange           = profileDto.exchange,
                    industry           = profileDto.industry,
                    marketCapFormatted = formatMarketCap(profileDto.marketCapMillions),
                    week52High         = metricDto.week52High,
                    week52Low          = metricDto.week52Low,
                    peRatio            = metricDto.peRatio,
                    beta               = metricDto.beta
                )
            )
        }
    }

    private fun formatMarketCap(millions: Double): String = when {
        millions >= 1_000_000 -> "$%.2fT".format(millions / 1_000_000)
        millions >= 1_000     -> "$%.1fB".format(millions / 1_000)
        else                  -> "$%.1fM".format(millions)
    }

    private suspend fun <T> retryWithBackoff(block: suspend () -> ApiResult<T>): ApiResult<T> {
        // Attempt up to 3 times with an exponential backoff between attempts.
        val delaysBetweenAttempts = listOf(500L, 1000L)
        var lastError: ApiResult<T> = ApiResult.Error("Unknown error")

        for (attempt in 0..delaysBetweenAttempts.size) {
            try {
                return block()
            } catch (e: Exception) {
                lastError = ApiResult.Error("Network error: ${e.message}", e)
                if (attempt < delaysBetweenAttempts.size) {
                    delay(delaysBetweenAttempts[attempt])
                }
            }
        }
        return lastError
    }
}