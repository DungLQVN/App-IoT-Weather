package com.example.myapplication

import android.content.Intent
import android.os.Bundle
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

    private val client = OkHttpClient()   // OKHttp client

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_stats)

        // xử lý edge-to-edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val sys = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom)
            insets
        }

        // --- CLICK ICON ---
        findViewById<ImageView>(R.id.icon_setting).setOnClickListener {
            startActivity(Intent(this, Setting::class.java))
        }
        findViewById<ImageView>(R.id.icon_notifications).setOnClickListener {
            startActivity(Intent(this, Notification::class.java))
        }
        findViewById<ImageView>(R.id.icon_home).setOnClickListener {
            startActivity(Intent(this, Dashboard::class.java))
        }

        loadGasChart()
    }

    private fun loadGasChart() {
        Thread {
            try {
                val req = Request.Builder()
                    .url("https://YOUR_API_HERE") // 🔥 thay API thật vào đây
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

    /** Hiển thị chart test */
//    private fun loadGasChart() {
//        val gas = 55
//        val air = 45
//        showPieChart(gas, air)
//    }


    /** Vẽ donut chart */
    private fun showPieChart(gas: Int, air: Int) {
        val pieChart = findViewById<PieChart>(R.id.pieChart)
        val tvSub = findViewById<TextView>(R.id.tv_sub)

        val entries = arrayListOf(
            PieEntry(gas.toFloat(), "Gas"),
            PieEntry(air.toFloat(), "Không khí")
        )

        // 👉 Chỉ dùng 1 dataset thôi
        val dataSet = PieDataSet(entries, "")

        // 👉 Màu Gas + Không khí
        dataSet.colors = listOf(
            android.graphics.Color.parseColor("#FF3B30"), // Đỏ
            android.graphics.Color.parseColor("#4CD964")  // Xanh lá
        )

        val labelColor = ContextCompat.getColor(this, R.color.textColor)

        dataSet.valueTextSize = 14f
        dataSet.valueTextColor = labelColor

        // 👉 Đưa đúng dataset vào PieData
        val data = PieData(dataSet)
        pieChart.data = data

        // 👉 Không cho xoay
        pieChart.isRotationEnabled = false
        pieChart.isHighlightPerTapEnabled = false

        // 👉 Hiển thị %
        pieChart.setUsePercentValues(true)
        pieChart.setDrawEntryLabels(false)

        // 👉 Donut style
        pieChart.isDrawHoleEnabled = true
        pieChart.holeRadius = 60f
        pieChart.transparentCircleRadius = 65f

        // Animation
        pieChart.animateY(1000)

        pieChart.description.isEnabled = false

        tvSub.setTextColor(labelColor)
        tvSub.text = "Cập nhật: Gas $gas%"

        pieChart.invalidate()
    }

}
