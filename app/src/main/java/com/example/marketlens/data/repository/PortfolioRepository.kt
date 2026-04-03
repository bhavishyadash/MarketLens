package com.example.marketlens.data.repository

import com.example.marketlens.data.model.PortfolioHolding
import com.example.marketlens.data.network.ApiResult

interface PortfolioRepository {
    suspend fun getHoldings(): ApiResult<List<PortfolioHolding>>
    suspend fun saveHolding(holding: PortfolioHolding): ApiResult<Unit>
    suspend fun deleteHolding(symbol: String): ApiResult<Unit>
}
