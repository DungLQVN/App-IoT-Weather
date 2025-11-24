package com.example.myapplication

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject

class Dashboard : ThemeLightDark() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val weatherClient = OkHttpClient()
    private val gasClient = OkHttpClient()

    private val apiKey = "ca0baf6a818c66173efb56fb03d42588"

    private lateinit var tvTemp: TextView
    private lateinit var tvDesc: TextView
    private lateinit var imgWeather: ImageView
    private lateinit var tvHumidity: TextView
    private lateinit var tvWindSpeed: TextView
    private lateinit var tvGasValue: TextView
    private lateinit var tvTempIndoor: TextView
    private lateinit var tvHumidityIndoor: TextView


    private var currentTempC = 0.0
    private var isCelsius = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dashboard)

        // Edge-to-edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, ins ->
            val bars = ins.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            ins
        }

        // ASK FOR EXACT ALARM PERMISSION ON ANDROID 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val am = getSystemService(AlarmManager::class.java)
            if (!am.canScheduleExactAlarms()) {
                startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
                return
            }
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        tvTemp = findViewById(R.id.tv_temp)
        tvDesc = findViewById(R.id.tv_weather_desc)
        imgWeather = findViewById(R.id.img_weather)
        tvHumidity = findViewById(R.id.tv_humidity)
        tvWindSpeed = findViewById(R.id.tv_windspeed)
        tvGasValue = findViewById(R.id.tv_gas_value)
        tvTempIndoor = findViewById(R.id.tv_temp_indoor)
        tvHumidityIndoor = findViewById(R.id.tv_humidity_indoor)


        // Navigation icons
        findViewById<ImageView>(R.id.icon_setting).setOnClickListener {
            startActivity(Intent(this, Setting::class.java))
        }
        findViewById<ImageView>(R.id.icon_notifications).setOnClickListener {
            startActivity(Intent(this, Notification::class.java))
        }
        findViewById<ImageView>(R.id.icon_stats).setOnClickListener {
            startActivity(Intent(this, Stats::class.java))
        }

        // WEATHER
        checkLocationPermission()
        setNextHourWeatherAlarm()

        // GAS MONITORING
        startGasAlarm()    // chạy nền từng phút
        startGasAutoRefresh()  // realtime khi mở Dashboard

        // Nút đổi °C/°F
        findViewById<LinearLayout>(R.id.box_temp).setOnClickListener {
            toggleTemperatureUnit()
        }
    }

    // -------------------------------
    // 🔥 REALTIME GAS UPDATE EVERY 1 SECONDS
    // -------------------------------
    private fun startGasAutoRefresh() {
        val handler = android.os.Handler(mainLooper)
        val runnable = object : Runnable {
            override fun run() {
                loadGasValue()
                handler.postDelayed(this, 1_000)  // 🔥 gọi API mỗi 1 giây
            }
        }
        handler.post(runnable)
    }


    private fun loadGasValue() {
        Thread {
            try {
                val request = Request.Builder()
                    .url("http://10.117.81.211:8000/node/node0")
                    .build()

                val response = gasClient.newCall(request).execute()
                val json = response.body?.string() ?: return@Thread

                val fixedJson = if (!json.trim().startsWith("[")) "[$json]" else json
                val arr = JSONArray(fixedJson)

                // 🔥 LẤY BẢN GHI MỚI NHẤT (INDEX 0)
                val newest = arr.getJSONObject(0)

                val gasValue = newest.getInt("gas")
                val tempValue = newest.getDouble("temperature")
                val humValue = newest.getDouble("humidity")

                runOnUiThread {
                    tvGasValue.text = "$gasValue"

                    // 🔥 HIỆN NHIỆT ĐỘ & ĐỘ ẨM TRONG NHÀ
                    tvTempIndoor.text = "${tempValue.toInt()}°C"
                    tvHumidityIndoor.text = "${humValue.toInt()}%"
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }




    // -------------------------------
    // 🔥 GAS ALARM mỗi phút
    // -------------------------------
    private fun startGasAlarm() {
        val am = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(this, GasAlarmReceiver::class.java)
        val pi = PendingIntent.getBroadcast(
            this, 1, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        am.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis(),
            pi
        )
    }

    // -------------------------------
    // WEATHER
    // -------------------------------
    private fun setNextHourWeatherAlarm() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(this, WeatherAlarmReceiver::class.java)
        val pi = PendingIntent.getBroadcast(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val now = java.util.Calendar.getInstance()
        now.set(java.util.Calendar.MINUTE, 0)
        now.set(java.util.Calendar.SECOND, 0)
        now.set(java.util.Calendar.MILLISECOND, 0)
        now.add(java.util.Calendar.HOUR_OF_DAY, 1)

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            now.timeInMillis,
            pi
        )
    }

    // -------------------------------
    // WEATHER LOGIC
    // -------------------------------
    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                1001
            )
        } else getLastLocation()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 1001 &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            getLastLocation()
        }
    }


    private fun getLastLocation() {
        if (
            ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let { getWeather(it.latitude, it.longitude) }
        }
    }

    private fun getWeather(lat: Double, lon: Double) {
        val url =
            "https://api.openweathermap.org/data/2.5/weather?lat=$lat&lon=$lon&appid=$apiKey&units=metric&lang=vi"

        val req = Request.Builder().url(url).build()

        weatherClient.newCall(req).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {}

            override fun onResponse(call: okhttp3.Call, resp: okhttp3.Response) {
                val json = resp.body?.string() ?: return
                val obj = JSONObject(json)

                val temp = obj.getJSONObject("main").getDouble("temp")
                val desc = obj.getJSONArray("weather").getJSONObject(0).getString("description")
                val icon = obj.getJSONArray("weather").getJSONObject(0).getString("icon")
                val city = obj.getString("name")

                currentTempC = temp

                runOnUiThread {
                    tvTemp.text = "${temp.toInt()}°C"
                    tvDesc.text = city

                    Glide.with(this@Dashboard)
                        .load("https://openweathermap.org/img/wn/${icon}@2x.png")
                        .into(imgWeather)
                }
            }
        })
    }

    private fun toggleTemperatureUnit() {
        isCelsius = !isCelsius
        val displayed = if (isCelsius)
            "${currentTempC.toInt()}°C"
        else
            "${(currentTempC * 9 / 5 + 32).toInt()}°F"

        tvTemp.text = displayed
    }
}
