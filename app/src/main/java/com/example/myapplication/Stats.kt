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
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import android.graphics.Color

class Stats : ThemeLightDark() {

    private val client = OkHttpClient()

    // Lịch sử 60 điểm
    private val gasHistory = ArrayDeque<Int>()
    private val tempHistory = ArrayDeque<Int>()
    private val humHistory = ArrayDeque<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_stats)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val sys = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom)
            insets
        }

        // Navigation
        findViewById<ImageView>(R.id.icon_setting).setOnClickListener {
            startActivity(Intent(this, Setting::class.java))
        }
        findViewById<ImageView>(R.id.icon_notifications).setOnClickListener {
            startActivity(Intent(this, Notification::class.java))
        }
        findViewById<ImageView>(R.id.icon_home).setOnClickListener {
            startActivity(Intent(this, Dashboard::class.java))
        }

        // Bắt đầu cập nhật dữ liệu mỗi 5 giây
       startLiveAPI()
//        startChartUpdater()

    }
// test du lieu thong bao
    private fun startChartUpdater() {
        SensorDataProvider.start()

        val handler = Handler(Looper.getMainLooper())

        val runnable = object : Runnable {
            override fun run() {
                val gas = SensorDataProvider.gas
                val air = 100 - gas

                val gasList = SensorDataProvider.gasHistory.toList()
                val tempList = SensorDataProvider.tempHistory.toList()
                val humList = SensorDataProvider.humHistory.toList()

                runOnUiThread {
                    showLineChart(findViewById(R.id.chart_gas), gasList, "Gas", Color.RED)
                    showLineChart(findViewById(R.id.chart_temp), tempList, "Temp", Color.CYAN)
                    showLineChart(findViewById(R.id.chart_hum), humList, "Hum", Color.GREEN)
                }

                handler.postDelayed(this, 1000)
            }
        }

        handler.post(runnable)
    }


    // -------------------------------------------
    //  GỌI API MỖI 5 GIÂY – LƯU 60 ĐIỂM
    // -------------------------------------------
    private fun startLiveAPI() {
        val handler = Handler(Looper.getMainLooper())

        val runnable = object : Runnable {
            override fun run() {
                Thread {
                    try {
                        val req = Request.Builder()
                            .url("http://10.117.81.211:8000/node/node0")
                            .build()

                        val res = client.newCall(req).execute()
                        val json = res.body?.string() ?: return@Thread

                        val arr = JSONArray(json)
                        val obj = arr.getJSONObject(0)

                        val gas = obj.getInt("gas")
                        val temp = obj.getDouble("temperature").toInt()
                        val hum = obj.getDouble("humidity").toInt()

                        // Lưu vào lịch sử
                        addHistoryValue(gasHistory, gas)
                        addHistoryValue(tempHistory, temp)
                        addHistoryValue(humHistory, hum)

                        runOnUiThread {
                            updateAllCharts()
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }.start()

                handler.postDelayed(this, 5000) // ⏰ 5 GIÂY / lần
            }
        }

        handler.post(runnable)
    }

    // -------------------------------------------
    //  THÊM GIÁ TRỊ VÀO LỊCH SỬ (tối đa 60 điểm)
    // -------------------------------------------
    private fun addHistoryValue(list: ArrayDeque<Int>, value: Int) {
        if (list.size >= 60) list.removeFirst()
        list.addLast(value)
    }

    // -------------------------------------------
    //  CẬP NHẬT TẤT CẢ LINE CHART
    // -------------------------------------------
    private fun updateAllCharts() {
        showLineChart(findViewById(R.id.chart_gas), gasHistory.toList(), "Gas", Color.RED)
        showLineChart(findViewById(R.id.chart_temp), tempHistory.toList(), "Temp", Color.CYAN)
        showLineChart(findViewById(R.id.chart_hum), humHistory.toList(), "Humidity", Color.GREEN)
    }


    // -------------------------------------------
    // Line Chart
    // -------------------------------------------
    private fun showLineChart(chart: LineChart, values: List<Int>, label: String, color: Int) {
        val entries = ArrayList<Entry>()

        for (i in values.indices) {
            entries.add(Entry(i.toFloat(), values[i].toFloat()))
        }

        val dataSet = LineDataSet(entries, label)
        dataSet.color = color
        dataSet.valueTextColor = color
        dataSet.circleRadius = 3f
        dataSet.setCircleColor(color)
        dataSet.lineWidth = 2f
        dataSet.setDrawFilled(false)

        chart.data = LineData(dataSet)
        chart.description.isEnabled = false
        chart.setTouchEnabled(true)
        chart.isDragEnabled = true
        chart.setScaleEnabled(true)

        chart.invalidate()
    }
}
