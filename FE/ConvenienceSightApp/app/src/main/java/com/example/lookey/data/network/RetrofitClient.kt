// app/src/main/java/com/example/lookey/data/network/RetrofitClient.kt
package com.example.lookey.data.network

import com.example.lookey.BuildConfig
import com.example.lookey.AppCtx
import com.example.lookey.data.local.TokenProvider
import com.example.lookey.data.model.RefreshRequest
import com.example.lookey.util.AuthListener
import com.example.lookey.util.PrefUtil
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.util.Log
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private val BASE_URL = BuildConfig.API_BASE_URL
    var authListener: AuthListener? = null

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .hostnameVerifier { _, _ -> true }
        .addInterceptor { chain ->
            val original = chain.request()
            val builder = original.newBuilder()

            // ① 메모리 우선
            var token: String? = TokenProvider.token

            // ② 없으면 저장소에서 로드
            if (token.isNullOrEmpty()) {
                token = PrefUtil.getJwtToken(AppCtx.app)
                if (!token.isNullOrEmpty()) TokenProvider.token = token
            }

            token?.let {
                builder.addHeader("Authorization", "Bearer $it")
                Log.d("RetrofitClient", "=== Authorization 헤더 추가됨 === Bearer $it")
            } ?: Log.w("RetrofitClient", "=== JWT 토큰 없음 ===")

            var request = builder.build()
            Log.d("RetrofitClient", "=== Sending request to ${request.url} ===")

            var response = chain.proceed(request)

            if (response.code == 401) {
                val refresh = PrefUtil.getRefreshToken(AppCtx.app)
                if (!refresh.isNullOrEmpty()) {
                    val newToken = refreshAccessToken(refresh)
                    if (!newToken.isNullOrEmpty()) {
                        PrefUtil.saveJwtToken(AppCtx.app, newToken)
                        TokenProvider.token = newToken
                        response.close()
                        request = original.newBuilder()
                            .header("Authorization", "Bearer $newToken")
                            .build()
                        response = chain.proceed(request)
                    } else {
                        PrefUtil.clear(AppCtx.app)
                        authListener?.onLogout()
                    }
                } else {
                    PrefUtil.clear(AppCtx.app)
                    authListener?.onLogout()
                }
            }

            Log.d("RetrofitClient", "=== API 요청 URL === ${request.url}")
            Log.d("RetrofitClient", "=== Status === ${response.code}")
            if (!response.isSuccessful) {
                val body = runCatching { response.peekBody(1024 * 1024).string() }.getOrNull().orEmpty()
                Log.e("RetrofitClient", "=== Error body === $body")
            }
            response
        }
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)                // ← 끝에 / 포함되어야 함
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: ApiService by lazy { retrofit.create(ApiService::class.java) }

    // 인터셉터 없는 얇은 클라이언트로 refresh (순환 방지)
    private fun refreshAccessToken(refreshToken: String): String? = runBlocking {
        try {
            val bare = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(OkHttpClient())
                .build()
                .create(ApiService::class.java)

            val resp = bare.refreshToken(RefreshRequest(refreshToken))
            if (resp.isSuccessful) resp.body()?.data?.jwtToken else null
        } catch (_: Throwable) {
            null
        }
    }
}
