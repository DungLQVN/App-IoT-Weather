package com.example.myapplication

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray

class GasWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    private val client = OkHttpClient()

    // 🔥 API GAS của bạn — chỉ cần dán link
    private val gasApiUrl = "YOUR_API_URL_HERE"

    override fun doWork(): Result {
        try {
            val request = Request.Builder().url(gasApiUrl).build()
            val response = client.newCall(request).execute()

            val json = response.body?.string() ?: return Result.failure()

            // API trả về mảng JSON → lấy phần tử đầu tiên
            val array = JSONArray(json)
            val gasValue = array.getJSONObject(0).getInt("gas")

            // 🔥 Nếu gas >= 80 → bật THÔNG BÁO NGAY LẬP TỨC
            if (gasValue >= 80) {
                NotificationHelper.showNotification(
                    applicationContext,
                    "⚠️ Gas Warning",
                    "Gas level is too high: $gasValue%"
                )
            }

            return Result.success()

        } catch (e: Exception) {
            e.printStackTrace()
            return Result.failure()
        }
    }
}
