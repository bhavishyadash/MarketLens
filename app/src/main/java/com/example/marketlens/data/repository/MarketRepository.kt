package com.example.marketlens.data.repository

import com.example.marketlens.data.model.SearchResult
import com.example.marketlens.data.model.StockCandle
import com.example.marketlens.data.model.StockQuote
import com.example.marketlens.data.network.ApiResult

interface MarketRepository {
    suspend fun getQuote(symbol: String): ApiResult<StockQuote>
    suspend fun searchSymbols(query: String): ApiResult<List<SearchResult>>
    suspend fun getCandles(symbol: String, resolution: String, from: Long, to: Long): ApiResult<StockCandle>
}