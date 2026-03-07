package com.example.marketlens.data.repository

import com.example.marketlens.data.model.StockQuote
import com.example.marketlens.data.network.ApiResult

/*
    Bug fixed:
    1. Package was "com.example.marketlens.repository"
    2. Was building StockRowUi (UI model) — now builds StockQuote (domain model)

    FakeMarketRepository is used during Phase 1 so all screens work
    without any real API calls. In Phase 2 it gets replaced by
    RealMarketRepository which talks to Finnhub.

    The ViewModel doesn't know or care whether it's talking to
    Fake or Real — it only knows the MarketRepository interface.
    That's the whole point of the interface.
*/
class FakeMarketRepository : MarketRepository {

    // A map of symbol → StockQuote so lookups are O(1)
    private val fakeData: Map<String, StockQuote> = listOf(
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
        return if (quote != null) {
            ApiResult.Success(quote)
        } else {
            ApiResult.Error("No fake data found for symbol: $symbol")
        }
    }
}
