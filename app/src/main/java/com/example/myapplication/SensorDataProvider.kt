package com.example.myapplication

import android.os.Handler
import android.os.Looper
import kotlin.random.Random

object SensorDataProvider {

    var gas: Int = 0
    var temp: Int = 0
    var hum: Int = 0

    val gasHistory = ArrayDeque<Int>()
    val tempHistory = ArrayDeque<Int>()
    val humHistory = ArrayDeque<Int>()

    private val handler = Handler(Looper.getMainLooper())
    private var started = false

    fun start() {
        if (started) return
        started = true

        val runnable = object : Runnable {
            override fun run() {

                // Tạo dữ liệu random
                gas = Random.nextInt(20, 100)
                temp = Random.nextInt(20, 45)
                hum = Random.nextInt(30, 100)

                // Lưu lịch sử 60 điểm
                addToHistory(gasHistory, gas)
                addToHistory(tempHistory, temp)
                addToHistory(humHistory, hum)

                handler.postDelayed(this, 1000) // 1s cập nhật 1 lần
            }
        }
        handler.post(runnable)
    }

    private fun addToHistory(list: ArrayDeque<Int>, value: Int) {
        if (list.size >= 60) list.removeFirst()
        list.addLast(value)
    }
}
