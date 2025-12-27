package com.example.marketlens.repository

import com.example.marketlens.network.ApiResult
import com.example.marketlens.viewmodel.StockRowUi

interface MarketRepository {
    suspend fun getQuote(symbol: String): ApiResult<StockRowUi>
}