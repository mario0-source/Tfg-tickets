package com.example.myapplication.auth

import android.content.Context

class SessionManager(private val context: Context) {

    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)

    fun saveToken(token: String?) {
        prefs.edit()
            .putString("jwt", token)
            .apply()
    }

    fun getToken(): String? {
        return prefs.getString("jwt", null)
    }

    fun clearToken() {
        prefs.edit()
            .remove("jwt")
            .apply()
    }
}