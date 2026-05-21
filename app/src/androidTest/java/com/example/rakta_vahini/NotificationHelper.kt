package com.raktavahini.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

object NotificationHelper {

    private const val CHANNEL_ID = "rakta_vahini_channel"
    private const val CHANNEL_NAME = "Rakta-Vahini Alerts"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Blood donation notifications"
                enableVibration(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun sendThankYouNotification(context: Context, donorName: String) {
        createNotificationChannel(context)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("🩸 Thank You, $donorName!")
            .setContentText("Your donation has been logged. You just saved a life!")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Thank you for donating blood. You are eligible to donate again in 90 days. Rakta-Vahini salutes you! 🙏")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        try {
            NotificationManagerCompat.from(context).notify(
                System.currentTimeMillis().toInt(), notification
            )
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
}