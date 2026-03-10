package com.example.mycafeme

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("cafe_session", Context.MODE_PRIVATE)
    private val gson = Gson()

    // ── เก็บข้อมูล User (เปลี่ยนเป็น CustomerData) ──
    fun saveUser(user: CustomerData, role: String) {
        val userJson = gson.toJson(user)
        prefs.edit().apply {
            putString("user_data", userJson)
            putString("user_role", role)
            putBoolean("is_logged_in", true)
            apply()
        }
    }

    // ── ดึงข้อมูล User (เปลี่ยนเป็น CustomerData) ──
    fun getUser(): CustomerData? {
        val json = prefs.getString("user_data", null)
        return if (json != null) gson.fromJson(json, CustomerData::class.java) else null
    }

    fun getRole(): String? = prefs.getString("user_role", null)

    fun isLoggedIn(): Boolean = prefs.getBoolean("is_logged_in", false)

    // ── ลบ Cache (Logout) ──
    fun clearSession() {
        prefs.edit().clear().apply()
    }
}