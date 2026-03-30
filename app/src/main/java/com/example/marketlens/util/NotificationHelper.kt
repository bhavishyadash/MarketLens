package com.example.marketlens.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.example.marketlens.data.model.AlertDirection
import com.example.marketlens.data.model.NewsSignal
import com.example.marketlens.data.model.PriceAlert
import com.example.marketlens.data.model.SignalStrength

object NotificationHelper {

    private const val CHANNEL_ALERTS  = "price_alerts"
    private const val CHANNEL_SIGNALS = "news_signals"

    fun createChannel(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_ALERTS, "Price Alerts", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Notifications when your stock price targets are reached"
            }
        )

        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_SIGNALS, "News Signals", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Informational signals derived from financial news"
            }
        )
    }

    fun sendAlertNotification(context: Context, alert: PriceAlert, currentPrice: Double) {
        val direction = if (alert.direction == AlertDirection.ABOVE) "above" else "below"
        val notification = NotificationCompat.Builder(context, CHANNEL_ALERTS)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("${alert.symbol} price alert triggered")
            .setContentText("${alert.symbol} is now $${"%.2f".format(currentPrice)}, $direction your target of $${"%.2f".format(alert.targetPrice)}")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(alert.id.hashCode(), notification)
    }

    fun sendSignalNotification(context: Context, signal: NewsSignal) {
        val priority = when (signal.strength) {
            SignalStrength.HIGH   -> NotificationCompat.PRIORITY_HIGH
            SignalStrength.MEDIUM -> NotificationCompat.PRIORITY_DEFAULT
            SignalStrength.LOW    -> NotificationCompat.PRIORITY_LOW
        }

        val title = "${signal.sector} sector signal — ${signal.strength.name}"
        val body  = if (signal.affectedSymbols.isNotEmpty())
            "${signal.reason}. May affect: ${signal.affectedSymbols.joinToString(", ")}"
        else
            signal.reason

        val notification = NotificationCompat.Builder(context, CHANNEL_SIGNALS)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(priority)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(signal.id.hashCode(), notification)
    }
}
