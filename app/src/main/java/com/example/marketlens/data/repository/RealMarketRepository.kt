package com.example.marketlens.data.repository

import com.example.marketlens.data.model.SearchResult
import com.example.marketlens.data.model.StockCandle
import com.example.marketlens.data.model.StockQuote
import com.example.marketlens.data.network.ApiResult
import com.example.marketlens.data.network.MarketApi

class RealMarketRepository(private val api: MarketApi) : MarketRepository {

    override suspend fun getQuote(symbol: String): ApiResult<StockQuote> {
        return try {
            val dto = api.getQuote(symbol)
            ApiResult.Success(StockQuote(symbol, symbol, dto.currentPrice, dto.percentChange))
        } catch (e: Exception) {
            ApiResult.Error("Could not load quote for $symbol: ${e.message}", e)
        }
    }

    override suspend fun searchSymbols(query: String): ApiResult<List<SearchResult>> {
        return try {
            val dto = api.searchSymbols(query)
            val results = dto.result
                .filter { it.type == "Common Stock" || it.type == "ETP" }
                .map { SearchResult(it.symbol, it.description, it.type) }
            ApiResult.Success(results)
        } catch (e: Exception) {
            ApiResult.Error("Search failed: ${e.message}", e)
        }
    }

    override suspend fun getCandles(symbol: String, resolution: String, from: Long, to: Long): ApiResult<StockCandle> {
        return try {
            val dto = api.getCandles(symbol, resolution, from, to)
            if (dto.status != "ok") {
                return ApiResult.Error("No chart data available for $symbol in this time range")
            }
            ApiResult.Success(StockCandle(dto.timestamps!!, dto.closePrices!!, dto.status))
        } catch (e: Exception) {
            ApiResult.Error("Could not load chart for $symbol: ${e.message}", e)
        }
    }
}