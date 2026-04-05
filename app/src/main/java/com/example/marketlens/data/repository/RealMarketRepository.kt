package com.example.marketlens.data.repository

import com.example.marketlens.data.QuoteCache
import com.example.marketlens.data.model.SearchResult
import com.example.marketlens.data.model.StockCandle
import com.example.marketlens.data.model.StockProfile
import com.example.marketlens.data.model.StockQuote
import com.example.marketlens.data.network.ApiResult
import com.example.marketlens.data.network.YahooFinanceApi

class RealMarketRepository(
    private val yahoo: YahooFinanceApi
) : MarketRepository {

    override suspend fun getQuote(symbol: String): ApiResult<StockQuote> {
        QuoteCache.get(symbol)?.let { return ApiResult.Success(it) }

        return try {
            val response = yahoo.getQuotes(symbol)
            val dto = response.quoteResponse.result?.firstOrNull()
                ?: return ApiResult.Error("No data for $symbol")

            val price  = dto.regularMarketPrice
                ?: return ApiResult.Error("No price data for $symbol")
            val change = dto.regularMarketChangePercent ?: 0.0
            val name   = dto.longName ?: dto.shortName ?: symbol

            val quote = StockQuote(symbol, name, price, change)
            QuoteCache.put(quote)
            ApiResult.Success(quote)
        } catch (e: Exception) {
            ApiResult.Error("Network error ($symbol): ${e.message}", e)
        }
    }

    suspend fun getBulkQuotes(symbols: List<String>): ApiResult<List<StockQuote>> {
        return try {
            val joined   = symbols.joinToString(",")
            val response = yahoo.getQuotes(joined)
            val results  = response.quoteResponse.result
                ?: return ApiResult.Error("No market data available")

            val quotes = results.mapNotNull { dto ->
                val price  = dto.regularMarketPrice ?: return@mapNotNull null
                val change = dto.regularMarketChangePercent ?: 0.0
                val name   = dto.longName ?: dto.shortName ?: dto.symbol
                val quote  = StockQuote(dto.symbol, name, price, change)
                QuoteCache.put(quote)
                quote
            }
            ApiResult.Success(quotes)
        } catch (e: Exception) {
            ApiResult.Error("Market load failed: ${e.message}", e)
        }
    }

    override suspend fun searchSymbols(query: String): ApiResult<List<SearchResult>> {
        return try {
            val response = yahoo.search(query = query, quotesCount = 15, newsCount = 0)
            val results  = response.quotes
                ?.filter { it.type == "Equity" || it.type == null }
                ?.map { SearchResult(it.symbol, it.longname ?: it.shortname ?: it.symbol, "Common Stock") }
                ?: emptyList()
            ApiResult.Success(results)
        } catch (e: Exception) {
            ApiResult.Error("Search failed: ${e.message}", e)
        }
    }

    override suspend fun getCandles(
        symbol: String, resolution: String, from: Long, to: Long
    ): ApiResult<StockCandle> {
        return try {
            val daysBack = (to - from) / 86400L
            val (interval, range) = when {
                resolution == "W"  -> "1wk" to "2y"
                daysBack > 60      -> "1d"  to "3mo"
                else               -> "1d"  to "1mo"
            }

            val response = yahoo.getChart(symbol, interval, range)
            val result   = response.chart.result?.firstOrNull()
                ?: return ApiResult.Error("No chart data for $symbol")

            val closePrices = result.indicators.quote
                .firstOrNull()
                ?.close
                ?.filterNotNull()
                ?: return ApiResult.Error("No price data for $symbol")

            if (closePrices.isEmpty()) return ApiResult.Error("No chart data for $symbol")

            val timestamps = closePrices.indices.map { it.toLong() }
            ApiResult.Success(StockCandle(timestamps, closePrices, "ok"))

        } catch (e: Exception) {
            ApiResult.Error("Chart error ($symbol): ${e.message}", e)
        }
    }

    override suspend fun getStockProfile(symbol: String): ApiResult<StockProfile> {
        return try {
            val response = yahoo.getQuotes(symbol)
            val dto = response.quoteResponse.result?.firstOrNull()
                ?: return ApiResult.Error("No profile data for $symbol")

            ApiResult.Success(
                StockProfile(
                    symbol             = symbol,
                    name               = dto.longName ?: dto.shortName ?: symbol,
                    exchange           = dto.fullExchangeName ?: "N/A",
                    industry           = dto.industry ?: dto.sector ?: "N/A",
                    marketCapFormatted = formatMarketCap(dto.marketCap),
                    week52High         = dto.fiftyTwoWeekHigh,
                    week52Low          = dto.fiftyTwoWeekLow,
                    peRatio            = dto.trailingPE,
                    beta               = dto.beta
                )
            )
        } catch (e: Exception) {
            ApiResult.Error("Profile error ($symbol): ${e.message}", e)
        }
    }

    private fun formatMarketCap(marketCapBytes: Long?): String {
        if (marketCapBytes == null) return "N/A"
        return when {
            marketCapBytes >= 1_000_000_000_000L -> "$%.2fT".format(marketCapBytes / 1_000_000_000_000.0)
            marketCapBytes >= 1_000_000_000L     -> "$%.1fB".format(marketCapBytes / 1_000_000_000.0)
            marketCapBytes >= 1_000_000L         -> "$%.1fM".format(marketCapBytes / 1_000_000.0)
            else                                 -> "$$marketCapBytes"
        }
    }
}