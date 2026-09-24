package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.example.data.local.YouPlayerDatabase
import com.example.util.FirebaseConfigHelper
import com.example.util.YouPlayerLogger

class YouPlayerApplication : Application() {

    val database: YouPlayerDatabase by lazy {
        YouPlayerDatabase.getDatabase(this)
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        YouPlayerLogger.i("Application", "YouPlayer started successfully")
        createNotificationChannels()
        FirebaseConfigHelper.initialize(this)
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID_ALERTS,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = getString(R.string.notification_channel_desc)
                enableVibration(true)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
            YouPlayerLogger.d("Application", "Notification channel created: $CHANNEL_ID_ALERTS")
        }
    }

    companion object {
        const val CHANNEL_ID_ALERTS = "youplayer_alerts_channel"
        lateinit var instance: YouPlayerApplication
            private set
    }
}
