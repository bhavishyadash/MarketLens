package com.example.marketlens

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.marketlens.util.NotificationHelper
import com.example.marketlens.worker.AlertCheckWorker
import com.example.marketlens.worker.NewsSignalWorker
import java.util.concurrent.TimeUnit
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase

class MarketLensApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        NotificationHelper.createChannel(this)
        Firebase.analytics.setAnalyticsCollectionEnabled(true)

        WorkManager.getInstance(this).apply {

            enqueueUniquePeriodicWork(
                "alert_check",
                ExistingPeriodicWorkPolicy.KEEP,
                PeriodicWorkRequestBuilder<AlertCheckWorker>(15, TimeUnit.MINUTES).build()
            )

            enqueueUniquePeriodicWork(
                "news_signal_check",
                ExistingPeriodicWorkPolicy.KEEP,
                PeriodicWorkRequestBuilder<NewsSignalWorker>(30, TimeUnit.MINUTES).build()
            )
        }
    }
}
