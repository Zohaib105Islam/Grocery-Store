package com.example.blinkit.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.blinkit.R
import com.example.blinkit.activities.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlin.random.Random

class NotificationService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val channelId = "Blinkit"
        val channel=NotificationChannel(channelId,"Blinkit",NotificationManager.IMPORTANCE_HIGH).apply {
            description="Blinkit messages"
            enableLights(true)
        }
        val manager=getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)

        val pendingIntent=PendingIntent.getActivity(this,0, Intent(this,MainActivity::class.java),PendingIntent.FLAG_UPDATE_CURRENT)

        val notification=NotificationCompat.Builder(this,channelId)
            .setContentTitle(message.data["title"])
            .setContentText(message.data["body"])
            .setSmallIcon(R.drawable.app_icon)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        manager.notify(Random.nextInt(),notification)
    }
}