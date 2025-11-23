package com.example.myapplication

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

class GasAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {

        // 🔥 chạy worker kiểm tra gas ngay khi báo thức bắn
        val work = OneTimeWorkRequestBuilder<GasWorker>().build()
        WorkManager.getInstance(context).enqueue(work)

        // 🔥 Đặt báo thức tiếp theo sau 1 phút
        setNextCheck(context)
    }

    private fun setNextCheck(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // 🔥 BẮT BUỘC TRÊN ANDROID 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                // Không có quyền → KHÔNG ĐƯỢC đặt alarm
                return
            }
        }

        val intent = Intent(context, GasAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val next = System.currentTimeMillis() + 60_000

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            next,
            pendingIntent
        )
    }
}
