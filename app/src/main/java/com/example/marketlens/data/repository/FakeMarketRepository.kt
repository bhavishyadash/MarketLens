package com.example.marketlens.repository

import com.example.marketlens.network.ApiResult
import com.example.marketlens.viewmodel.StockRowUi

class FakeMarketRepository : MarketRepository {

    private val fake = listOf(
        StockRowUi("AAPL", "Apple Inc.", 187.23, 1.12),
        StockRowUi("MSFT", "Microsoft", 412.10, -0.38),
        StockRowUi("NVDA", "NVIDIA", 890.22, 2.41),
        StockRowUi("TSLA", "Tesla", 242.88, -3.72),
        StockRowUi("AMZN", "Amazon", 156.44, 0.65),
        StockRowUi("GOOGL", "Alphabet", 141.88, 0.54),
        StockRowUi("META", "Meta", 355.70, -0.92)
    ).associateBy { it.symbol }

    override suspend fun getQuote(symbol: String): ApiResult<StockRowUi> {
        val item = fake[symbol]
        return if (item != null) ApiResult.Success(item)
        else ApiResult.Error("No fake data for $symbol")
    }
}