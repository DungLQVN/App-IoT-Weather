package com.example.myapplication

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray

class AlertReceiver : BroadcastReceiver() {

    private val client = OkHttpClient()

    override fun onReceive(context: Context, intent: Intent?) {
        Thread {
            try {
                val req = Request.Builder()
                    .url("http://10.117.81.211:8000/node/node0")
                    .build()

                val res = client.newCall(req).execute()
                val json = res.body?.string() ?: return@Thread

                val arr = JSONArray(json)
                val newest = arr.getJSONObject(0)

                val gas = newest.getInt("gas")
                val temp = newest.getDouble("temperature").toInt()
                val hum = newest.getDouble("humidity").toInt()

                var shouldContinue = false
                var msg = ""

                if (gas > 70) {
                    msg = "⚠️ Gas vẫn cao: $gas%"
                    shouldContinue = true
                }

                if (temp > 40) {
                    msg = "⚠️ Nhiệt độ vẫn cao: $temp°C"
                    shouldContinue = true
                }

                if (hum > 80) {
                    msg = "⚠️ Độ ẩm vẫn cao: $hum%"
                    shouldContinue = true
                }

                if (shouldContinue) {
                    NotificationHelper.showNotification(context, "Cảnh báo lặp lại", msg)
                    NotificationStorage.saveNotification(context, msg)

                    // lặp tiếp 30s lần nữa
                    AlertScheduler.startRepeatingAlert(context)
                } else {
                    // tắt lặp nếu mọi thứ bình thường
                    AlertScheduler.stopRepeatingAlert(context)
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }
}
