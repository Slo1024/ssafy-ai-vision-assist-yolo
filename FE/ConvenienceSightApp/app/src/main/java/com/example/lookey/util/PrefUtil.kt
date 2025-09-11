package com.example.lookey.util

import android.content.Context

object PrefUtil {
    private const val PREF_NAME = "auth"
    private const val KEY_JWT = "jwt_token"

    fun saveUserId(context: Context, userId: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString("user_id", userId).apply()
    }

    fun getUserId(context: Context): String? {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString("user_id", null)
    }

    fun saveJwtToken(context: Context, token: String) { // ✅ 추가
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_JWT, token).apply()
    }

    fun getJwtToken(context: Context): String? { // ✅ 추가
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_JWT, null)
    }

    fun clear(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }
}
