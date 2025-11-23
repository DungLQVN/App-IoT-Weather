package com.example.myapplication

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.work.Worker
import androidx.work.WorkerParameters
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class WeatherWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    private val client = OkHttpClient()
    private val apiKey = "ca0baf6a818c66173efb56fb03d42588"

    override fun doWork(): Result {
        try {
            val prefs = applicationContext.getSharedPreferences("Settings", Context.MODE_PRIVATE)
            val lang = prefs.getString("My_Lang", "vi") ?: "vi"

            val lat = prefs.getFloat("LAT", 0f).toDouble()
            val lon = prefs.getFloat("LON", 0f).toDouble()

            if (lat == 0.0 || lon == 0.0) return Result.retry()

            val url = "https://api.openweathermap.org/data/2.5/weather" +
                    "?lat=$lat&lon=$lon&appid=$apiKey&units=metric&lang=$lang"

            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()

            val json = response.body?.string() ?: return Result.failure()
            val obj = JSONObject(json)

            val temp = obj.getJSONObject("main").getDouble("temp")
            val desc = obj.getJSONArray("weather").getJSONObject(0).getString("description")
            val city = obj.getString("name")

            val message = "$city: ${temp.toInt()}°C – $desc"

            NotificationHelper.showNotification(applicationContext, "Weather Update", message)
            NotificationStorage.saveNotification(applicationContext, message)

            return Result.success()

        } catch (e: Exception) {
            e.printStackTrace()
            return Result.failure()
        }
    }
}


