package com.example.myapplication


import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import okhttp3.*
import org.json.JSONObject
import com.bumptech.glide.Glide



class Dashboard : ThemeLightDark() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val client = OkHttpClient()
    private val apiKey = "ca0baf6a818c66173efb56fb03d42588"

    companion object {
        private const val LOCATION_PERMISSION_REQUEST = 1001
    }

    private lateinit var tvTemp: TextView
    private lateinit var tvDesc: TextView
    private lateinit var imgWeather: ImageView

    private lateinit var tvHumidity: TextView

    private lateinit var tvWindSpeed: TextView

    private var isCelsius = true
    private var currentTempC = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dashboard)

        // Xử lý edge-to-edge padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // ⚠️ PHẢI khởi tạo fusedLocationClient TRƯỚC
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Lấy icon và set sự kiện click
        val settingsIcon: ImageView = findViewById(R.id.icon_setting)
        settingsIcon.setOnClickListener {
            val intent = Intent(this, Setting::class.java)
            startActivity(intent)
        }

        val NotiIcon: ImageView = findViewById(R.id.icon_notifications)
        NotiIcon.setOnClickListener {
            val intent = Intent(this, Notification::class.java)
            startActivity(intent)
        }

        val StatsIcon: ImageView = findViewById(R.id.icon_stats)
        StatsIcon.setOnClickListener {
            val intent = Intent(this, Stats::class.java)
            startActivity(intent)
        }

        tvTemp = findViewById(R.id.tv_temp)
        tvDesc = findViewById(R.id.tv_weather_desc)
        imgWeather = findViewById(R.id.img_weather)
        tvHumidity = findViewById(R.id.tv_humidity)
        tvWindSpeed = findViewById(R.id.tv_windspeed)

        checkLocationPermission()

        val boxTemp = findViewById<LinearLayout>(R.id.box_temp)
        boxTemp.setOnClickListener {
            if (isCelsius) {
                // Đổi sang °F
                val tempF = currentTempC * 9 / 5 + 32
                tvTemp.text = "${tempF.toInt()}°F"
                isCelsius = false
            } else {
                // Đổi về °C
                tvTemp.text = "${currentTempC.toInt()}°C"
                isCelsius = true
            }
        }

    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST
            )
        } else {
            getLastLocation()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_PERMISSION_REQUEST &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            getLastLocation()
        } else {
            tvDesc.text = "Không có quyền truy cập vị trí"
        }
    }

    private fun getLastLocation() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                getWeather(it.latitude, it.longitude)
            } ?: run {
                tvDesc.text = "Không lấy được vị trí"
            }
        }
    }

    private fun getWeather(lat: Double, lon: Double) {
        val url =
            "https://api.openweathermap.org/data/2.5/weather?lat=$lat&lon=$lon&appid=$apiKey&units=metric&lang=vi"

        val request = Request.Builder().url(url).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: java.io.IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                val json = response.body?.string()
                val jsonObj = JSONObject(json)
                val tempC = jsonObj.getJSONObject("main").getDouble("temp")
                currentTempC = tempC
                val weather = jsonObj.getJSONArray("weather").getJSONObject(0)
                val desc = weather.getString("description")
                val icon = weather.getString("icon")
                val city = jsonObj.getString("name") // 🏙️ Lấy tên thành phố
                val humidity = jsonObj.getJSONObject("main").getInt("humidity") // Độ ẩm
                val windSpeed = jsonObj.getJSONObject("wind").getDouble("speed") *3.6// Tốc độ gió

                runOnUiThread {
                    tvTemp.text = "${tempC.toInt()}°C"
                    tvDesc.text = city

                    tvHumidity.text = "$humidity %"
                    tvWindSpeed.text = "$windSpeed km/h"

                    val iconUrl = "https://openweathermap.org/img/wn/${icon}@2x.png"
                    Glide.with(this@Dashboard).load(iconUrl).into(imgWeather)
                }
            }
        })
    }

//    private fun getWeatherIcon(icon: String): Int {
//        return when (icon) {
//            "01d" -> R.drawable.ic_sunny
//            "01n" -> R.drawable.ic_moon
//            "02d", "02n" -> R.drawable.ic_partly_cloudy
//            "03d", "03n", "04d", "04n" -> R.drawable.ic_cloud
//            "09d", "09n", "10d", "10n" -> R.drawable.ic_rain
//            "11d", "11n" -> R.drawable.ic_thunder
//            "13d", "13n" -> R.drawable.ic_snow
//            "50d", "50n" -> R.drawable.ic_fog
//            else -> R.drawable.ic_cloud
//        }
//    }
}

