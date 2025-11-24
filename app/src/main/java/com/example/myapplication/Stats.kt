package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray

class Stats : ThemeLightDark() {

    private val client = OkHttpClient()

    // 🔥 Auto refresh handler
    private val refreshHandler = Handler(Looper.getMainLooper())
    private lateinit var refreshRunnable: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_stats)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val sys = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom)
            insets
        }

        findViewById<ImageView>(R.id.icon_setting).setOnClickListener {
            startActivity(Intent(this, Setting::class.java))
        }
        findViewById<ImageView>(R.id.icon_notifications).setOnClickListener {
            startActivity(Intent(this, Notification::class.java))
        }
        findViewById<ImageView>(R.id.icon_home).setOnClickListener {
            startActivity(Intent(this, Dashboard::class.java))
        }

        // 🔥 Lần đầu load chart
        loadGasChart()

        // 🔥 Tự động refresh mỗi 3 giây
        refreshRunnable = Runnable {
            loadGasChart()
            refreshHandler.postDelayed(refreshRunnable, 3000)
        }
        refreshHandler.postDelayed(refreshRunnable, 3000)
    }

    override fun onDestroy() {
        super.onDestroy()
        refreshHandler.removeCallbacks(refreshRunnable)
    }

    private fun loadGasChart() {
        Thread {
            try {
                val req = Request.Builder()
                    .url("http://10.117.81.211:8000/node/node0")
                    .build()

                val res = client.newCall(req).execute()
                val json = res.body?.string() ?: return@Thread

                val arr = JSONArray(json)
                val gas = arr.getJSONObject(0).getInt("gas")
                val air = 100 - gas

                runOnUiThread {
                    showPieChart(gas, air)
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    private fun showPieChart(gas: Int, air: Int) {
        val pieChart = findViewById<PieChart>(R.id.pieChart)
        val tvSub = findViewById<TextView>(R.id.tv_sub)

        val entries = arrayListOf(
            PieEntry(gas.toFloat(), "Gas"),
            PieEntry(air.toFloat(), "Không khí")
        )

        val dataSet = PieDataSet(entries, "")
        dataSet.colors = listOf(
            android.graphics.Color.parseColor("#FF3B30"), // Gas
            android.graphics.Color.parseColor("#4CD964")  // Không khí
        )

        val labelColor = ContextCompat.getColor(this, R.color.textColor)
        dataSet.valueTextSize = 14f
        dataSet.valueTextColor = labelColor

        val data = PieData(dataSet)
        pieChart.data = data

        // Không cho xoay
        pieChart.isRotationEnabled = false
        pieChart.isHighlightPerTapEnabled = false

        pieChart.setUsePercentValues(true)
        pieChart.setDrawEntryLabels(false)

        // Donut style
        pieChart.isDrawHoleEnabled = true
        pieChart.holeRadius = 60f
        pieChart.transparentCircleRadius = 65f

        // Animation
        pieChart.animateY(1000)

        pieChart.description.isEnabled = false

        tvSub.text = "Cập nhật: Gas $gas%"
        tvSub.setTextColor(labelColor)

        pieChart.invalidate()
    }
}
