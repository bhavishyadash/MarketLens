package com.example.marketlens.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.marketlens.data.AppContainer
import com.example.marketlens.data.model.AlertDirection
import com.example.marketlens.data.network.ApiResult
import com.example.marketlens.util.NotificationHelper

class AlertCheckWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val alertRepo  = AppContainer.alertRepository
        val marketRepo = AppContainer.repository

        val alertsResult = alertRepo.getActiveAlerts()
        val alerts = when (alertsResult) {
            is ApiResult.Success -> alertsResult.data
            is ApiResult.Error   -> return Result.success()
        }

        if (alerts.isEmpty()) return Result.success()

        val symbolGroups = alerts.groupBy { it.symbol }

        for ((symbol, symbolAlerts) in symbolGroups) {
            val quoteResult = marketRepo.getQuote(symbol)
            val currentPrice = when (quoteResult) {
                is ApiResult.Success -> quoteResult.data.price
                is ApiResult.Error   -> continue
            }

            for (alert in symbolAlerts) {
                val triggered = when (alert.direction) {
                    AlertDirection.ABOVE -> currentPrice >= alert.targetPrice
                    AlertDirection.BELOW -> currentPrice <= alert.targetPrice
                }
                if (triggered) {
                    NotificationHelper.sendAlertNotification(context, alert, currentPrice)
                    alertRepo.markTriggered(alert.id)
                }
            }
        }

        return Result.success()
    }
}
