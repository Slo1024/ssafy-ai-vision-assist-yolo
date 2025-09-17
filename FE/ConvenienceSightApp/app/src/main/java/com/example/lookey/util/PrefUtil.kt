package com.example.lookey.util

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

object PrefUtil {
    private const val PREF_NAME = "auth"
    private const val KEY_JWT = "jwt_token"
    private const val KEY_REFRESH = "refresh_token"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USER_NAME = "user_name"

    // EncryptedSharedPreferences 초기화
    private fun getPrefs(context: Context) = EncryptedSharedPreferences.create(
        PREF_NAME,
        MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveUserId(context: Context, userId: String) {
        val prefs = getPrefs(context)
        prefs.edit().putString(KEY_USER_ID, userId).apply()
    }

    fun getUserId(context: Context): String? {
        val prefs = getPrefs(context)
        return prefs.getString(KEY_USER_ID, null)
    }

    fun saveJwtToken(context: Context, token: String) {
        val prefs = getPrefs(context)
        prefs.edit().putString(KEY_JWT, token).apply()
    }

    fun getJwtToken(context: Context): String? {
        val prefs = getPrefs(context)
        return prefs.getString(KEY_JWT, null)
    }

    // Refresh Token 저장/조회
    fun saveRefreshToken(context: Context, token: String) {
        val prefs = getPrefs(context)
        prefs.edit().putString(KEY_REFRESH, token).apply()
    }

    fun getRefreshToken(context: Context): String? {
        val prefs = getPrefs(context)
        return prefs.getString(KEY_REFRESH, null)
    }

    fun saveUserName(context: Context, userName: String?) {
        val prefs = getPrefs(context)   // applicationContext로 통일
        prefs.edit().putString(KEY_USER_NAME, userName ?: "사용자").apply()
    }

    fun getUserName(context: Context): String {
        val prefs = getPrefs(context)   // applicationContext로 통일
        return prefs.getString(KEY_USER_NAME, "사용자") ?: "사용자"
    }

    fun clear(context: Context) {
        val prefs = getPrefs(context)
        prefs.edit().clear().apply()
    }
}
