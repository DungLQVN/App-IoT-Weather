package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Notification : ThemeLightDark() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_notification)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // ----------- ICONS -----------
        val settingsIcon: ImageView = findViewById(R.id.icon_setting)
        settingsIcon.setOnClickListener {
            startActivity(Intent(this, Setting::class.java))
        }

        val statsIcon: ImageView = findViewById(R.id.icon_stats)
        statsIcon.setOnClickListener {
            startActivity(Intent(this, Stats::class.java))
        }

        val homeIcon: ImageView = findViewById(R.id.icon_home)
        homeIcon.setOnClickListener {
            startActivity(Intent(this, Dashboard::class.java))
        }

        // ----------- HIỂN THỊ LỊCH SỬ THÔNG BÁO -----------
        val container = findViewById<LinearLayout>(R.id.history_container)

        val jsonArray = NotificationStorage.getNotifications(this)

// Duyệt từ mới nhất → cũ nhất
        for (i in jsonArray.length() - 1 downTo 0) {

            val item = jsonArray.getJSONObject(i)

            val view = layoutInflater.inflate(R.layout.item_notification, container, false)

            // Tạo text time
            val time = SimpleDateFormat("HH:mm dd/MM", Locale.getDefault())
                .format(Date(item.getLong("time")))

            val message = item.getString("message")

            // Gán vào layout
            val tvTime = view.findViewById<TextView>(R.id.tv_time)
            val tvMsg = view.findViewById<TextView>(R.id.tv_message)

            tvTime.text = time
            tvMsg.text = message

            // Thêm vào container
            container.addView(view)
        }


    }
}
