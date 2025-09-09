package com.example.lookey.data.remote

import com.example.lookey.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

// API 서비스 인터페이스
interface ApiService {
    
    @GET("health")
    suspend fun getHealth(): retrofit2.Response<Map<String, Any>>
    
    @GET("users/{id}")
    suspend fun getUser(@Path("id") userId: String): retrofit2.Response<User>
    
    @GET("products")
    suspend fun getProducts(@Query("page") page: Int = 0): retrofit2.Response<ProductResponse>
}

// 데이터 클래스 예시
data class User(
    val id: String,
    val name: String,
    val email: String
)

data class ProductResponse(
    val products: List<Product>,
    val totalCount: Int,
    val page: Int
)

data class Product(
    val id: String,
    val name: String,
    val description: String,
    val price: Double
)

// API 클라이언트 싱글톤
object ApiClient {
    
    private val loggingInterceptor by lazy {
        HttpLoggingInterceptor().apply {
            level = when (BuildConfig.LOG_LEVEL) {
                "DEBUG" -> HttpLoggingInterceptor.Level.BODY
                "INFO" -> HttpLoggingInterceptor.Level.BASIC
                else -> HttpLoggingInterceptor.Level.NONE
            }
        }
    }
    
    private val httpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL + "/")
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
    
    // 환경 정보 출력 (디버깅용)
    fun printEnvironmentInfo() {
        if (BuildConfig.DEBUG_ENABLED) {
            println("=== API Environment Info ===")
            println("Environment: ${BuildConfig.ENVIRONMENT}")
            println("API Base URL: ${BuildConfig.API_BASE_URL}")
            println("Debug Enabled: ${BuildConfig.DEBUG_ENABLED}")
            println("Log Level: ${BuildConfig.LOG_LEVEL}")
            println("============================")
        }
    }
}

