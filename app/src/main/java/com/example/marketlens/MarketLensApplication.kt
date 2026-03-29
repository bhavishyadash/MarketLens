package com.example.marketlens

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.marketlens.util.NotificationHelper
import com.example.marketlens.worker.AlertCheckWorker
import java.util.concurrent.TimeUnit

class MarketLensApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        NotificationHelper.createChannel(this)

        val alertWork = PeriodicWorkRequestBuilder<AlertCheckWorker>(15, TimeUnit.MINUTES).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "alert_check",
            ExistingPeriodicWorkPolicy.KEEP,
            alertWork
        )
    }
}
