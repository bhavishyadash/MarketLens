package com.example.marketlens.data.repository

import com.example.marketlens.data.model.SearchResult
import com.example.marketlens.data.model.StockCandle
import com.example.marketlens.data.model.StockQuote
import com.example.marketlens.data.network.ApiResult

class FakeMarketRepository : MarketRepository {

    private val fakeData = listOf(
        StockQuote("AAPL",  "Apple Inc.",  187.23,  1.12),
        StockQuote("MSFT",  "Microsoft",   412.10, -0.38),
        StockQuote("NVDA",  "NVIDIA",      890.22,  2.41),
        StockQuote("TSLA",  "Tesla",       242.88, -3.72),
        StockQuote("AMZN",  "Amazon",      156.44,  0.65),
        StockQuote("GOOGL", "Alphabet",    141.88,  0.54),
        StockQuote("META",  "Meta",        355.70, -0.92)
    ).associateBy { it.symbol }

    override suspend fun getQuote(symbol: String): ApiResult<StockQuote> {
        val quote = fakeData[symbol]
        return if (quote != null) ApiResult.Success(quote)
        else ApiResult.Error("No fake data for $symbol")
    }

    override suspend fun searchSymbols(query: String): ApiResult<List<SearchResult>> {
        val results = fakeData.values
            .filter { it.symbol.contains(query, ignoreCase = true) || it.name.contains(query, ignoreCase = true) }
            .map { SearchResult(it.symbol, it.name, "Common Stock") }
        return ApiResult.Success(results)
    }

    override suspend fun getCandles(symbol: String, resolution: String, from: Long, to: Long): ApiResult<StockCandle> {
        return ApiResult.Success(StockCandle(emptyList(), emptyList(), "no_data"))
    }
}