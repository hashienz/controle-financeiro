package com.example.myapplication.localstorage

import android.content.Context
import android.content.SharedPreferences

class PreferenciasApp(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("lume_prefs", Context.MODE_PRIVATE)

    fun setLoggedIn(isLoggedIn: Boolean) {
        prefs.edit().putBoolean("is_logged_in", isLoggedIn).apply()
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean("is_logged_in", false)
    }
}
