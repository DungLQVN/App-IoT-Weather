package com.example.myapplication

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent

object AlertScheduler {

    private const val ALERT_REQ = 999

    fun startRepeatingAlert(context: Context) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlertReceiver::class.java)
        val pi = PendingIntent.getBroadcast(
            context, ALERT_REQ, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        am.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + 30_000,   // 30 giây
            pi
        )
    }

    fun stopRepeatingAlert(context: Context) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlertReceiver::class.java)
        val pi = PendingIntent.getBroadcast(
            context, ALERT_REQ, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        am.cancel(pi)
    }
}
