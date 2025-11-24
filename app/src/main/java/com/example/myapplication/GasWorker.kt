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

                // ----------- CÁC NGƯỠNG CẢNH BÁO -----------
                if (gasValue > 70) {
                    NotificationHelper.showNotification(
                        applicationContext,
                        "⚠️ Cảnh Báo Gas",
                        "Nồng độ gas cao: $gasValue%"
                    )
                }

                if (tempValue > 40) {
                    NotificationHelper.showNotification(
                        applicationContext,
                        "⚠️ Cảnh Báo Nhiệt Độ",
                        "Nhiệt độ phòng quá cao: $tempValue°C"
                    )
                }

                if (humValue > 80) {
                    NotificationHelper.showNotification(
                        applicationContext,
                        "⚠️ Cảnh Báo Độ Ẩm",
                        "Độ ẩm quá cao: $humValue%"
                    )
                }

                return Result.success()
            }
        } catch (e: IOException) {
            Log.e("GasWorker", "Network error", e)
            return Result.retry()
        }
        catch (e: Exception) {
            Log.e("GasWorker", "Unexpected error", e)
            return Result.failure()
        }
    }

    // ---------------------------------------------------------
    // Parse JSON → trả về bản ghi mới nhất chứa temp, hum, gas
    // ---------------------------------------------------------
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
