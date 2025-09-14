package com.example.lookey.data.network

import com.example.lookey.data.local.TokenProvider
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "https://j13e101.p.ssafy.io/dev/" // 실제 API 주소로 교체
//    private const val BASE_URL = "https://10.0.2.2:8443/" // HTTPS 8443 포트

    private val gson = GsonBuilder()
        .create()

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS) // 연결 타임아웃 30초
        .readTimeout(30, TimeUnit.SECONDS) // 읽기 타임아웃 30초
        .writeTimeout(30, TimeUnit.SECONDS) // 쓰기 타임아웃 30초
        .hostnameVerifier { _, _ -> true } // 호스트명 검증 비활성화 (로컬 테스트용)
        .addInterceptor { chain ->
            val builder = chain.request().newBuilder()
                .addHeader("Content-Type", "application/json")
                .addHeader("Type", "BEARER")  // ✅ Type 헤더 추가

            // JWT 토큰이 있으면 Authorization 헤더에 추가
            TokenProvider.token?.let {
                builder.addHeader("Authorization", "Bearer $it")
            }

            val request = builder.build()

            // 요청 로그
            android.util.Log.d("RetrofitClient", "=== API 요청 시작 ===")
            android.util.Log.d("RetrofitClient", "URL: ${request.url}")
            android.util.Log.d("RetrofitClient", "Method: ${request.method}")
            android.util.Log.d("RetrofitClient", "Headers: ${request.headers}")

            try {
                val response = chain.proceed(request)

                // 응답 로그
                android.util.Log.d("RetrofitClient", "=== API 응답 받음 ===")
                android.util.Log.d("RetrofitClient", "Status: ${response.code}")
                android.util.Log.d("RetrofitClient", "Message: ${response.message}")
                android.util.Log.d("RetrofitClient", "result: ${response.request}")

                response
            } catch (e: Exception) {
                android.util.Log.e("RetrofitClient", "=== API 호출 실패 ===", e)
                throw e
            }

//            val request = chain.request().newBuilder()
//                .addHeader("Content-Type", "application/json; charset=utf8")
//                .addHeader("Type", "BEARER")
//                .addHeader("Access-Token", TokenProvider.token ?: "1")
//                .build()
//
//            val response = chain.proceed(request)
//
//            // 📌 여기서 응답 로그 찍기!
//            val responseBody = response.peekBody(Long.MAX_VALUE).string()
//            android.util.Log.d("RetrofitClient", "서버 응답 본문:\n$responseBody")
//
//            response
        }
        .build()



    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ApiService::class.java)
    }

    internal val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    val api: ApiService by lazy { retrofit.create(ApiService::class.java) }
}