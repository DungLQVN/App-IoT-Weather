package com.example.myapplication

import android.content.Context
import androidx.appcompat.app.AppCompatActivity

open class ChangLanguage : AppCompatActivity() {
    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val lang = prefs.getString("My_Lang", "vi") ?: "vi"
        val context = LocaleHelper.setLocale(newBase, lang)
        super.attachBaseContext(context)
    }
}
