package com.example.util

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.YouPlayerApplication

object NotificationHelper {
    private const val TAG = "NotificationHelper"

    fun canSendNotifications(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun showTrendingNotification(context: Context, videoTitle: String, channelName: String) {
        if (!canSendNotifications(context)) {
            YouPlayerLogger.w(TAG, "Notification permission not granted, skipping notification")
            return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, YouPlayerApplication.CHANNEL_ID_ALERTS)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("🔥 Trending on YouPlayer")
            .setContentText("$videoTitle • $channelName")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$videoTitle\nBy $channelName • Tap to watch now with zero ads and private streaming.")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(1001, notification)
        YouPlayerLogger.i(TAG, "Sent trending push notification for: $videoTitle")
    }

    fun showSupporterNotification(context: Context) {
        if (!canSendNotifications(context)) return

        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, YouPlayerApplication.CHANNEL_ID_ALERTS)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("⭐ YouPlayer Supporter Activated")
            .setContentText("Thank you for supporting open source! VIP perks are unlocked.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(1002, notification)
        YouPlayerLogger.i(TAG, "Sent Supporter activation notification")
    }
}
