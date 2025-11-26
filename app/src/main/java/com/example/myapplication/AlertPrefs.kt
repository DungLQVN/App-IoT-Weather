package com.example.myapplication

import android.content.Context

object AlertPrefs {

    private const val PREF = "AlertPrefs"
    private const val KEY_LAST_ALERT = "last_alert_time"

    fun getLastAlertTime(context: Context): Long {
        val sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        return sp.getLong(KEY_LAST_ALERT, 0L)
    }

    fun updateLastAlertTime(context: Context) {
        val sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        sp.edit().putLong(KEY_LAST_ALERT, System.currentTimeMillis()).apply()
    }
}
