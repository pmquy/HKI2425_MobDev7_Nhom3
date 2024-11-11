package com.example.facebook.data

import android.content.SharedPreferences

class UserPreferenceRepository (
    private val sharedPreferences: SharedPreferences
) {
    fun setToken(token: String) {
        sharedPreferences.edit().putString("token", token).apply()
    }
    fun getToken(): String? {
        return sharedPreferences.getString("token", null)
    }

}