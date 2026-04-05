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
            val response = yahoo.getChart(symbol, "1m", "1d")
            val result   = response.chart.result?.firstOrNull()
                ?: return ApiResult.Error("No data for $symbol")

            val price  = result.indicators.quote.firstOrNull()?.close?.lastNotNull()
                ?: return ApiResult.Error("No price for $symbol")
            
            val quote = StockQuote(symbol, symbol, price, 0.0)
            QuoteCache.put(quote)
            ApiResult.Success(quote)
        } catch (e: Exception) {
            ApiResult.Error("API Blocked ($symbol): ${e.message}", e)
        }
    }

    private fun List<Double?>.lastNotNull(): Double? = lastOrNull { it != null }

    suspend fun getBulkQuotes(symbols: List<String>): ApiResult<List<StockQuote>> {
        val quotes = mutableListOf<StockQuote>()
        for (s in symbols) {
            val res = getQuote(s)
            if (res is ApiResult.Success) quotes.add(res.data)
        }
        return if (quotes.isEmpty()) ApiResult.Error("Market load failed") else ApiResult.Success(quotes)
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
            val quoteRes = getQuote(symbol)
            if (quoteRes is ApiResult.Error) return ApiResult.Error(quoteRes.message)
            val q = (quoteRes as ApiResult.Success).data

            ApiResult.Success(
                StockProfile(
                    symbol             = symbol,
                    name               = q.name,
                    exchange           = "N/A",
                    industry           = "N/A",
                    marketCapFormatted = "N/A",
                    week52High         = null,
                    week52Low          = null,
                    peRatio            = null,
                    beta               = null
                )
            )
        } catch (e: Exception) {
            ApiResult.Error("Profile error ($symbol): ${e.message}", e)
        }
    }
}