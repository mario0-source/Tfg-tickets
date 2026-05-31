package com.example.myapplication.auth

import android.content.Context

class SessionManager(private val context: Context) {

    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)

    fun saveToken(token: String?) {
        prefs.edit()
            .putString("jwt", token)
            .apply()
    }

    fun saveEmail(email: String?) {
        prefs.edit()
            .putString("email", email)
            .apply()
    }

    fun getEmail(): String? {
        return prefs.getString("email", null)
    }

    fun getToken(): String? {
        return prefs.getString("jwt", null)
    }

    fun clearToken() {
        prefs.edit()
            .remove("jwt")
            .remove("email")
            .apply()
    }
}