package com.example.myapplication

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.app.Notification


object NotificationHelper {

    private const val CHANNEL_ID = "weather_channel"

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Weather Notifications",
                NotificationManager.IMPORTANCE_HIGH   // 🔥 Quan trọng cao
            )

            // 🔥 Cho phép hiển thị ở màn hình khóa
            channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC

            val manager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }


    fun showNotification(context: Context, title: String, message: String) {
        createChannel(context)

        // ⛔ Android 13+ phải có quyền POST_NOTIFICATIONS mới được notify
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return   // Không có quyền → dừng, tránh crash Worker
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notifications)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)  // 🔥 bật heads-up popup
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // 🔥 hiển thị trên màn hình khóa
            .setAutoCancel(true)


        NotificationManagerCompat.from(context)
            .notify(System.currentTimeMillis().toInt(), builder.build())
    }
}
