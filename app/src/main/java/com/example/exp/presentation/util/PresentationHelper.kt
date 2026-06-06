package com.example.exp.presentation.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.example.exp.R
import kotlin.random.Random

fun sendTestNotification(context: Context) {

    val channelId = "test_channel"

    val manager =
        context.getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager

    val channel = NotificationChannel(
        channelId,
        "Test Channel",
        NotificationManager.IMPORTANCE_DEFAULT
    )

    manager.createNotificationChannel(channel)

    val notification =
        NotificationCompat.Builder(
            context,
            channelId
        )
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Google Pay")
            .setContentText("₹1200 sent to Aman Raj")
            .build()

    manager.notify(
        Random.nextInt(1000),
        notification
    )
}