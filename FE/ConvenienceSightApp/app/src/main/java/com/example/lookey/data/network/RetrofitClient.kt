// app/src/main/java/com/example/lookey/data/network/RetrofitClient.kt
package com.example.lookey.data.network

import com.example.lookey.BuildConfig
import com.example.lookey.AppCtx
import com.example.lookey.data.local.TokenProvider
import com.example.lookey.data.model.RefreshRequest
import com.example.lookey.util.AuthListener
import com.example.lookey.util.PrefUtil
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import android.util.Log
import retrofit2.create

object RetrofitClient {
    private val BASE_URL = if (BuildConfig.API_BASE_URL.endsWith("/")) BuildConfig.API_BASE_URL else BuildConfig.API_BASE_URL + "/"
    var authListener: AuthListener? = null

    // 1) 액세스 토큰 헤더만 붙이는 인터셉터
    private class AuthInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val original = chain.request()
            val builder = original.newBuilder()

            // 토큰 가져오기 및 상태 로깅
            var token = TokenProvider.token
            Log.d("AuthInterceptor", "TokenProvider.token exists: ${!token.isNullOrEmpty()}")

            if (token.isNullOrEmpty()) {
                token = PrefUtil.getJwtToken(AppCtx.app)
                Log.d("AuthInterceptor", "PrefUtil.getJwtToken exists: ${!token.isNullOrEmpty()}")
                if (!token.isNullOrEmpty()) {
                    TokenProvider.token = token
                    Log.d("AuthInterceptor", "Token updated in TokenProvider")
                }
            }

            if (!token.isNullOrEmpty()) {
                val authHeader = "Bearer $token"
                builder.header("Authorization", authHeader)
                Log.d("AuthInterceptor", "Request URL: ${original.url}")
                Log.d("AuthInterceptor", "Authorization header: ${authHeader.take(50)}...")

                // JWT 토큰 디코딩하여 만료 시간 확인 (Base64 디코딩)
                try {
                    val parts = token.split(".")
                    if (parts.size == 3) {
                        val payload = String(android.util.Base64.decode(parts[1], android.util.Base64.URL_SAFE or android.util.Base64.NO_PADDING))
                        Log.d("AuthInterceptor", "JWT Payload: $payload")
                    }
                } catch (e: Exception) {
                    Log.e("AuthInterceptor", "Failed to decode JWT", e)
                }
            } else {
                Log.w("AuthInterceptor", "No token available for ${original.url}")
            }

            return chain.proceed(builder.build())
        }
    }

    // 2) 401 시 refresh → 1회 재시도 (인터셉터 미사용 bare 클라이언트로 호출)
    private class TokenAuthenticator(
        private val baseUrl: String,
        private val listener: AuthListener?
    ) : Authenticator {

        @Synchronized
        override fun authenticate(route: Route?, response: Response): Request? {
            Log.d("TokenAuthenticator", "401 received for ${response.request.url}")

            // 이미 한 번 재시도했다면 중단(무한루프 방지)
            if (response.priorResponse != null) {
                Log.w("TokenAuthenticator", "Already retried once, stopping to prevent infinite loop")
                return null
            }

            val refresh = PrefUtil.getRefreshToken(AppCtx.app)
            if (refresh.isNullOrEmpty()) {
                Log.e("TokenAuthenticator", "No refresh token available")
                // Refresh token이 없으면 JWT token으로 시도 (일부 서버는 JWT만 사용)
                val jwtToken = PrefUtil.getJwtToken(AppCtx.app)
                if (!jwtToken.isNullOrEmpty()) {
                    Log.w("TokenAuthenticator", "Trying to use JWT token as refresh token")
                    // JWT 토큰으로 재시도
                    return response.request.newBuilder()
                        .header("Authorization", "Bearer $jwtToken")
                        .build()
                }
                return logoutAndNull()
            }

            Log.d("TokenAuthenticator", "Attempting token refresh with refresh token: ${refresh.take(20)}...")

            return try {
                val bare = Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create<ApiService>()

                val resp = bare.refreshToken(RefreshRequest(refresh))

                if (!resp.isSuccessful) {
                    Log.e("TokenAuthenticator", "Refresh failed - HTTP ${resp.code()}: ${resp.message()}")
                    Log.e("TokenAuthenticator", "Response body: ${resp.errorBody()?.string()}")
                    return logoutAndNull()
                }

                val newAccess = resp.body()?.data?.jwtToken
                if (newAccess.isNullOrEmpty()) {
                    Log.e("TokenAuthenticator", "Refresh successful but no new token in response")
                    return logoutAndNull()
                }

                Log.d("TokenAuthenticator", "Token refresh successful, new token: ${newAccess.take(20)}...")

                // 새 토큰 저장
                PrefUtil.saveJwtToken(AppCtx.app, newAccess)
                TokenProvider.token = newAccess

                // 저장 확인
                val savedToken = PrefUtil.getJwtToken(AppCtx.app)
                if (savedToken != newAccess) {
                    Log.e("TokenAuthenticator", "Token save verification failed!")
                }

                response.request.newBuilder()
                    .header("Authorization", "Bearer $newAccess")
                    .build()
            } catch (t: Throwable) {
                Log.e("TokenAuthenticator", "Token refresh exception", t)
                logoutAndNull()
            }
        }

        private fun logoutAndNull(): Request? {
            Log.w("TokenAuthenticator", "Logging out user due to authentication failure")
            try {
                listener?.onLogout()
                Log.d("TokenAuthenticator", "AuthListener notified of logout")
            } catch (e: Throwable) {
                Log.e("TokenAuthenticator", "Error notifying logout listener", e)
            }
            PrefUtil.clear(AppCtx.app)
            TokenProvider.token = null
            return null
        }
    }

    private val logging = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.HEADERS // 디버그 모드에서는 헤더 포함 로깅
        } else {
            HttpLoggingInterceptor.Level.BASIC
        }
    }

    private val okHttp: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            // ❌ hostnameVerifier { _, _ -> true } 제거 (보안/오동작 원인)
            .addInterceptor(AuthInterceptor())
            .addInterceptor(logging)
            .authenticator(TokenAuthenticator(BASE_URL, authListener))
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: ApiService by lazy { retrofit.create(ApiService::class.java) }

    // 인증 없는 클라이언트 (테스트용)
    val noAuthClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .build()
    }

    val noAuthApiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(noAuthClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
