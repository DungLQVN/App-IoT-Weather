package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Setting : ThemeLightDark() {

    private lateinit var toggleThumb: View
    private lateinit var toggleContainer: RelativeLayout
    private lateinit var iconSun: ImageView
    private lateinit var iconMoon: ImageView

    private var isDarkMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_setting)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Điều hướng icon
        findViewById<ImageView>(R.id.icon_notifications).setOnClickListener {
            startActivity(Intent(this, Notification::class.java))
        }
        findViewById<ImageView>(R.id.icon_stats).setOnClickListener {
            startActivity(Intent(this, Stats::class.java))
        }
        findViewById<ImageView>(R.id.icon_home).setOnClickListener {
            startActivity(Intent(this, Dashboard::class.java))
        }
        findViewById<ImageView>(R.id.icon_forward).setOnClickListener {
            startActivity(Intent(this, Language::class.java))
        }

        // Toggle
        toggleContainer = findViewById(R.id.dark_mode_toggle)
        toggleThumb = findViewById(R.id.toggle_thumb)
        iconSun = findViewById(R.id.icon_sun)
        iconMoon = findViewById(R.id.icon_moon)

        // Lấy trạng thái hiện tại
        isDarkMode = ThemeManager.isDarkMode(this)
        updateToggleUI(isDarkMode, animate = false)

        toggleContainer.setOnClickListener {
            isDarkMode = !isDarkMode
            ThemeManager.setDarkMode(this, isDarkMode)
            updateToggleUI(isDarkMode, animate = true)
            recreate()
        }
    }

    private fun updateToggleUI(isDarkMode: Boolean, animate: Boolean) {
        toggleContainer.post {
            // ✅ Tính quãng đường di chuyển thật (200dp container - 90dp thumb - padding 10dp)
            val moveDistance = toggleContainer.width - toggleThumb.width - 10f

            // ✅ Xác định hướng di chuyển
            val targetX = if (isDarkMode) moveDistance else 0f

            // ✅ Animation mượt
            if (animate) {
                toggleThumb.animate()
                    .translationX(targetX)
                    .setDuration(300)
                    .setInterpolator(android.view.animation.AccelerateDecelerateInterpolator())
                    .start()
            } else {
                toggleThumb.translationX = targetX
            }

            // ✅ Làm mờ icon không được chọn
            iconSun.alpha = if (isDarkMode) 0.4f else 1f
            iconMoon.alpha = if (isDarkMode) 1f else 0.4f

            // ✅ Giữ bo tròn khi đổi nền
            val backgroundDrawable = ContextCompat.getDrawable(
                this,
                if (isDarkMode) R.drawable.rectangleradiusblack else R.drawable.rectangleradiuswhite
            )
            toggleContainer.background = backgroundDrawable

            // ✅ Cập nhật màu cho phần "Ngôn ngữ"
            val languageContainer = findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.language_container)
            val languageLabel = findViewById<android.widget.TextView>(R.id.language_label)
            val iconForward = findViewById<ImageView>(R.id.icon_forward)

// Nền bo góc của language_container
            val langBackgroundDrawable = ContextCompat.getDrawable(
                this,
                if (isDarkMode) R.drawable.rectangleradiusblack else R.drawable.rectangleradiuswhite
            )
            languageContainer.background = langBackgroundDrawable

// Màu chữ và icon
            val textColor = ContextCompat.getColor(
                this,
                if (isDarkMode) R.color.white else R.color.black
            )
            languageLabel.setTextColor(textColor)
            iconForward.setColorFilter(textColor)

        }
    }

}
