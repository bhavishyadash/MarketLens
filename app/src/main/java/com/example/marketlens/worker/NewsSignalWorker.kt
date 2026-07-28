package com.example.marketlens.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.marketlens.data.AppContainer
import com.example.marketlens.data.model.SignalStrength
import com.example.marketlens.data.network.ApiResult
import com.example.marketlens.util.NotificationHelper
import com.example.marketlens.signal.SignalEngine


class NewsSignalWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val newsRepo     = AppContainer.newsRepository
        val watchlistRepo = AppContainer.watchlistRepository
        val signalRepo   = AppContainer.signalRepository
        val settingsRepo = AppContainer.settingsRepository

        val settings = when (val r = settingsRepo.getSettings()) {
            is ApiResult.Success -> r.data
            is ApiResult.Error   -> return Result.success()
        }

        if (!settings.signalsEnabled) return Result.success()

        val articlesResult = newsRepo.getMarketNews()
        val articles = when (articlesResult) {
            is ApiResult.Success -> articlesResult.data
            is ApiResult.Error   -> return Result.retry()
        }

        val watchlistSymbols = when (val r = watchlistRepo.getWatchlistSymbols()) {
            is ApiResult.Success -> r.data
            is ApiResult.Error   -> emptyList()
        }

        val seenIds = signalRepo.getSeenArticleIds()

        var signals = SignalEngine.process(
            articles         = articles,
            watchlistSymbols = watchlistSymbols,
            alreadySeenIds   = seenIds
        )

        if (settings.highSignalsOnly) {
            signals = signals.filter { it.strength == SignalStrength.HIGH }
        }

        if (settings.watchlistSectorOnly) {
            signals = signals.filter { it.affectedSymbols.isNotEmpty() }
        }

        if (signals.isEmpty()) return Result.success()

        signalRepo.saveSignals(signals)

        signalRepo.addSeenArticleIds(articles.map { it.id })

        if (settings.notifyOnSignal) {
            signals.filter { it.strength == SignalStrength.HIGH || it.strength == SignalStrength.MEDIUM }
                .take(3)
                .forEach { signal ->
                    NotificationHelper.sendSignalNotification(context, signal)
                }
        }

        return Result.success()
    }
}