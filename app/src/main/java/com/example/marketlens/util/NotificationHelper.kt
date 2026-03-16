package com.example.marketlens.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.example.marketlens.data.model.AlertDirection
import com.example.marketlens.data.model.PriceAlert

object NotificationHelper {

    private const val CHANNEL_ID   = "price_alerts"
    private const val CHANNEL_NAME = "Price Alerts"

    fun createChannel(context: Context) {
        val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH).apply {
            description = "Notifications when your stock price targets are reached"
        }
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    fun sendAlertNotification(context: Context, alert: PriceAlert, currentPrice: Double) {
        val direction = if (alert.direction == AlertDirection.ABOVE) "above" else "below"
        val title = "${alert.symbol} price alert triggered"
        val body  = "${alert.symbol} is now $${"%.2f".format(currentPrice)}, $direction your target of $${"%.2f".format(alert.targetPrice)}"

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(alert.id.hashCode(), notification)
    }
}
