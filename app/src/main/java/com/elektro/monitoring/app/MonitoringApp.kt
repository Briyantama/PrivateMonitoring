package com.elektro.monitoring.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.elektro.monitoring.R
import com.elektro.monitoring.data.service.BackgroundService
import com.elektro.monitoring.helper.Constants
import com.elektro.monitoring.viewmodel.AuthViewModel
import com.elektro.monitoring.viewmodel.DataViewModel
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MonitoringApp : Application() {

    @Inject
    lateinit var dataViewModel: DataViewModel
    private val handler2 = Handler(Looper.getMainLooper())

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        val notificationManager = applicationContext
            .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        handler2.postDelayed(runnable, 1000)
        if (notificationManager.getNotificationChannel(Constants.NOTIFICATION_CHANNEL_ID) == null) {
            val name = applicationContext.getString(R.string.app_name)
            val channel = NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_ID,
                name,
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    private val runnable = object : Runnable {
        override fun run() {
            dataViewModel.update()
            handler2.postDelayed(this, 1000)
        }
    }
}