package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Setting : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_setting)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // Lấy icon và set sự kiện click
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

        val HomeIcon: ImageView = findViewById(R.id.icon_home)
        HomeIcon.setOnClickListener {
            val intent = Intent(this, Dashboard::class.java)
            startActivity(intent)
        }

        val iconForward = findViewById<ImageView>(R.id.icon_forward)

        iconForward.setOnClickListener {
            // Chuyển sang màn Language
            val intent = Intent(this, Language::class.java)
            startActivity(intent)
        }
    }
}