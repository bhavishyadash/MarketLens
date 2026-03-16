package com.example.marketlens.data.repository

import com.example.marketlens.data.model.AlertDirection
import com.example.marketlens.data.model.PriceAlert
import com.example.marketlens.data.network.ApiResult

interface AlertRepository {
    suspend fun getAlerts(): ApiResult<List<PriceAlert>>
    suspend fun getActiveAlerts(): ApiResult<List<PriceAlert>>
    suspend fun addAlert(symbol: String, targetPrice: Double, direction: AlertDirection): ApiResult<Unit>
    suspend fun markTriggered(alertId: String): ApiResult<Unit>
    suspend fun deleteAlert(alertId: String): ApiResult<Unit>
}
