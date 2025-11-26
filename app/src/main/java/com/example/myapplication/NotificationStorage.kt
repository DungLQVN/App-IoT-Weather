package com.example.myapplication

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object NotificationStorage {

    private const val PREF_NAME = "WeatherHistory"
    private const val KEY_LIST = "NotificationList"

    fun saveNotification(context: Context, message: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val list = JSONArray(prefs.getString(KEY_LIST, "[]"))

        val item = JSONObject()
        item.put("time", System.currentTimeMillis())
        item.put("message", message)

        list.put(item)

        prefs.edit().putString(KEY_LIST, list.toString()).apply()
    }

    fun getNotifications(context: Context): JSONArray {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return JSONArray(prefs.getString(KEY_LIST, "[]"))
    }

    fun clearAll(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LIST, "[]").apply()
    }
}
