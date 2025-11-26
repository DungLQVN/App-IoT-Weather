package com.example.myapplication

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.io.IOException

class GasWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    private val client = OkHttpClient()
    private val gasApiUrl = "http://10.117.81.211:8000/node/node0"

    override fun doWork(): Result {
        val request = Request.Builder().url(gasApiUrl).build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e("GasWorker", "HTTP error: ${response.code}")
                    return Result.failure()
                }

                val bodyText = response.body?.string()
                if (bodyText.isNullOrBlank()) {
                    Log.e("GasWorker", "Empty response body")
                    return Result.failure()
                }

                val newest = parseNewest(bodyText)
                if (newest == null) {
                    Log.e("GasWorker", "Cannot parse JSON: $bodyText")
                    return Result.failure()
                }

                val gasValue = newest.gas
                val tempValue = newest.temp
                val humValue = newest.hum

                Log.d("GasWorker", "Gas=$gasValue Temp=$tempValue Hum=$humValue")

                // ====================== CẢNH BÁO GAS ======================
                // GAS
                if (gasValue > 70) {
                    val msg = "⚠️ Gas cao: $gasValue%"
                    NotificationHelper.showNotification(applicationContext, "⚠️ Cảnh Báo Gas", msg)
                    NotificationStorage.saveNotification(applicationContext, msg)

                    AlertScheduler.startRepeatingAlert(applicationContext)
                }

// TEMP
                if (tempValue > 40) {
                    val msg = "⚠️ Nhiệt độ cao: $tempValue°C"
                    NotificationHelper.showNotification(applicationContext, "⚠️ Nhiệt Độ Cao", msg)
                    NotificationStorage.saveNotification(applicationContext, msg)

                    AlertScheduler.startRepeatingAlert(applicationContext)
                }

// HUMIDITY
                if (humValue > 80) {
                    val msg = "⚠️ Độ ẩm cao: $humValue%"
                    NotificationHelper.showNotification(applicationContext, "⚠️ Độ Ẩm Cao", msg)
                    NotificationStorage.saveNotification(applicationContext, msg)

                    AlertScheduler.startRepeatingAlert(applicationContext)
                }

                return Result.success()
            }
        } catch (e: IOException) {
            Log.e("GasWorker", "Network error", e)
            return Result.retry()
        } catch (e: Exception) {
            Log.e("GasWorker", "Unexpected error", e)
            return Result.failure()
        }
    }

    // Parse JSON trả về bản ghi mới nhất
    private fun parseNewest(text: String): SensorData? {
        return try {
            val arr = JSONArray(text)
            if (arr.length() == 0) return null

            val newest = arr.getJSONObject(0)
            SensorData(
                gas = newest.getInt("gas"),
                temp = newest.getDouble("temperature").toInt(),
                hum = newest.getDouble("humidity").toInt()
            )
        } catch (e: Exception) {
            Log.e("GasWorker", "Parse error", e)
            null
        }
    }

    data class SensorData(
        val gas: Int,
        val temp: Int,
        val hum: Int
    )
}


// test
//package com.example.myapplication
//
//import android.content.Context
//import android.util.Log
//import androidx.work.Worker
//import androidx.work.WorkerParameters
//import kotlin.random.Random
//
//class GasWorker(context: Context, workerParams: WorkerParameters) :
//    Worker(context, workerParams) {
//
//    // 30 giây
//    private val ALERT_INTERVAL = 30_000L
//
//    override fun doWork(): Result {
//
//        SensorDataProvider.start()
//
//        val gas = SensorDataProvider.gas
//        val temp = SensorDataProvider.temp
//        val hum = SensorDataProvider.hum
//
//        if (gas > 70) {
//            NotificationHelper.showNotification(
//                applicationContext,
//                "⚠️ Gas cao",
//                "Gas hiện tại: $gas%"
//            )
//        }
//
//        if (temp > 40) {
//            NotificationHelper.showNotification(
//                applicationContext,
//                "⚠️ Nhiệt độ cao",
//                "Nhiệt độ: $temp°C"
//            )
//        }
//
//        if (hum > 80) {
//            NotificationHelper.showNotification(
//                applicationContext,
//                "⚠️ Độ ẩm cao",
//                "Độ ẩm: $hum%"
//            )
//        }
//
//        return Result.success()
//    }
//
//}
