package com.example.lookey.data.network

import com.example.lookey.data.local.TokenProvider
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.lookey.data.model.RefreshRequest
import com.example.lookey.util.AuthListener
import com.example.lookey.util.PrefUtil
import kotlinx.coroutines.runBlocking
import com.example.lookey.BuildConfig


object RetrofitClient {
    private val BASE_URL = BuildConfig.API_BASE_URL

    var authListener: AuthListener? = null

//    private val gson = GsonBuilder()
//        .create()

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS) // 연결 타임아웃 30초
        .readTimeout(30, TimeUnit.SECONDS) // 읽기 타임아웃 30초
        .writeTimeout(30, TimeUnit.SECONDS) // 쓰기 타임아웃 30초
        .hostnameVerifier { _, _ -> true } // 호스트명 검증 비활성화 (로컬 테스트용)
        .addInterceptor { chain ->
            val originalRequest = chain.request()
            val context = originalRequest.tag(Context::class.java)
            val builder = chain.request().newBuilder()

            // 토큰 부착
            val accessToken = TokenProvider.token ?: context?.let { PrefUtil.getJwtToken(it) }
            accessToken?.let { builder.addHeader("Authorization", "Bearer $it") }

            var request = builder.build()
            var response = chain.proceed(request)

            if (response.code == 401 && context != null) {
                val refreshToken = PrefUtil.getRefreshToken(context)
                if (!refreshToken.isNullOrEmpty()) {
                    val newToken = refreshAccessToken(refreshToken)
                    if (!newToken.isNullOrEmpty()) {
                        PrefUtil.saveJwtToken(context, newToken)
                        TokenProvider.token = newToken
                        request = originalRequest.newBuilder()
                            .header("Authorization", "Bearer $newToken")
                            .build()
                        response = chain.proceed(request)
                    } else {
                        PrefUtil.clear(context); authListener?.onLogout()
                    }
                } else {
                    PrefUtil.clear(context); authListener?.onLogout()
                }
            }

            // ✅ 여기부터를 추가
            Log.d("RetrofitClient", "=== API 요청 URL === ${request.url}")
            Log.d("RetrofitClient", "=== Status === ${response.code}")

            if (!response.isSuccessful) {
                val errBody = try {
                    response.peekBody(1024 * 1024).string() // 바디 복사본
                } catch (_: Throwable) { "" }
                Log.e("RetrofitClient", "=== Error body === $errBody")
            }
            // ✅ 여기까지 추가

            response
        }

        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: ApiService by lazy { retrofit.create(ApiService::class.java) }

    // -------------------------------
    // Refresh Token으로 새 Access Token 발급
    private fun refreshAccessToken(refreshToken: String): String? = runBlocking {
        try {
            val response = apiService.refreshToken(RefreshRequest(refreshToken))
            if (response.isSuccessful) {
                response.body()?.data?.jwtToken
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

//    val api: ApiService by lazy {
//        Retrofit.Builder()
//            .baseUrl(BASE_URL)
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//            .create(ApiService::class.java)
//    }
}