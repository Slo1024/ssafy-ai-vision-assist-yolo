package com.example.lookey.util

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import java.io.IOException

object PrefUtil {
    private const val PREF_NAME = "auth"
    private const val KEY_JWT = "jwt_token"
    private const val KEY_REFRESH = "refresh_token"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USER_NAME = "user_name"

    // EncryptedSharedPreferences 초기화 (복호화 실패 시 안전하게 재생성)
    private fun getPrefs(context: Context): SharedPreferences {
        return try {
            EncryptedSharedPreferences.create(
                PREF_NAME,
                MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            Log.w("PrefUtil", "EncryptedSharedPreferences 생성 실패, 기존 파일 삭제 후 재생성", e)
            try {
                context.deleteSharedPreferences(PREF_NAME)
            } catch (ex: Exception) {
                Log.e("PrefUtil", "기존 SharedPreferences 삭제 실패", ex)
            }
            EncryptedSharedPreferences.create(
                PREF_NAME,
                MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }
    }


    fun saveUserId(context: Context, userId: String) {
        getPrefs(context).edit().putString(KEY_USER_ID, userId).apply()
    }

    fun getUserId(context: Context): String? {
        return try {
            getPrefs(context).getString(KEY_USER_ID, null)
        } catch (e: Exception) {
            Log.w("PrefUtil", "getUserId 복호화 실패, null 반환", e)
            null
        }
    }

    fun saveJwtToken(context: Context, token: String) {
        getPrefs(context).edit().putString(KEY_JWT, token).apply()
    }

    fun getJwtToken(context: Context): String? {
        return try {
            getPrefs(context).getString(KEY_JWT, null)
        } catch (e: Exception) {
            Log.w("PrefUtil", "getJwtToken 복호화 실패, null 반환", e)
            null
        }
    }

    fun saveRefreshToken(context: Context, token: String) {
        getPrefs(context).edit().putString(KEY_REFRESH, token).apply()
    }

    fun getRefreshToken(context: Context): String? {
        return try {
            getPrefs(context).getString(KEY_REFRESH, null)
        } catch (e: Exception) {
            Log.w("PrefUtil", "getRefreshToken 복호화 실패, null 반환", e)
            null
        }
    }

    fun saveUserName(context: Context, userName: String?) {
        getPrefs(context).edit().putString(KEY_USER_NAME, userName).apply()
    }

    fun getUserName(context: Context): String? {
        return getPrefs(context).getString(KEY_USER_NAME, null)
    }


    fun clear(context: Context) {
        try {
            getPrefs(context).edit().clear().apply()
        } catch (e: Exception) {
            Log.w("PrefUtil", "clear 복호화 실패, SharedPreferences 삭제 시도", e)
            try {
                context.deleteSharedPreferences(PREF_NAME)
            } catch (ex: Exception) {
                Log.e("PrefUtil", "SharedPreferences 완전 삭제 실패", ex)
            }
        }
    }
}
