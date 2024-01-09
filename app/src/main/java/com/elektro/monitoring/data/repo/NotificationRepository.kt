package com.elektro.monitoring.data.repo

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.elektro.monitoring.R
import com.elektro.monitoring.app.MainActivity
import com.elektro.monitoring.helper.Constants.NOTIFICATION_CHANNEL_ID

class NotificationRepository {

    fun sendNotification(context: Context, title: String, text: String): Notification{
        val intent = MainActivity.newIntent(context.applicationContext)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

        val stackBuilder = TaskStackBuilder.create(context)
            .addParentStack(MainActivity::class.java)
            .addNextIntent(intent)
        val notificationPendingIntent = stackBuilder
            .getPendingIntent(getUniqueId(), PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.baseline_notifications)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(notificationPendingIntent)
            .setAutoCancel(true)
            .build()
    }

    private fun getUniqueId() = (System.currentTimeMillis() % 10000).toInt()
}
