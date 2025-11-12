package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Language : ThemeLightDark() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_language)

        // Căn chỉnh phần hiển thị toàn màn hình
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Nút quay lại Setting
        val iconBack = findViewById<ImageView>(R.id.icon_back)
        iconBack.setOnClickListener {
            val intent = Intent(this, Setting::class.java)
            startActivity(intent)
            finish()
        }

        // Hai layout để chọn ngôn ngữ
        val boxEng = findViewById<LinearLayout>(R.id.box_language_eng)
        val boxVn = findViewById<LinearLayout>(R.id.box_language_vn)

        boxEng.setOnClickListener {
            setLanguage("en")
        }

        boxVn.setOnClickListener {
            setLanguage("vi")
        }
    }

    private fun setLanguage(langCode: String) {
        LocaleHelper.setLocale(this, langCode)

        // Lưu lại ngôn ngữ (SharedPreferences)
        val prefs = getSharedPreferences("Settings", MODE_PRIVATE)
        prefs.edit().putString("My_Lang", langCode).apply()

        // Reload lại Activity để cập nhật giao diện
        val intent = Intent(this, Language::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        finish()
    }

    // Đảm bảo khi Activity được tạo, nó dùng đúng ngôn ngữ đã chọn
    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val lang = prefs.getString("My_Lang", "vi") ?: "vi"
        val context = LocaleHelper.setLocale(newBase, lang)
        super.attachBaseContext(context)
    }
}
