// app/src/main/java/com/example/lookey/data/network/ApiService.kt
package com.example.lookey.data.network

import com.example.lookey.data.model.ApiResponse
import com.example.lookey.data.model.LoginResponse
import com.example.lookey.data.model.RefreshRequest
import com.example.lookey.data.remote.dto.navigation.VisionAnalyzeResponse
import com.example.lookey.data.remote.dto.product.LocationSearchResult
import com.example.lookey.data.remote.dto.product.ShelfSearchResult
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*



interface ApiService {
    @POST("api/auth/google")
    suspend fun googleLogin(
        @Header("Authorization") authorizationHeader: String
    ): Response<LoginResponse>

    @POST("api/auth/refresh")
    fun refreshToken(@Body request: RefreshRequest): Response<LoginResponse>

    @Multipart
    @POST("api/v1/product/search")
    suspend fun searchShelf(
        @Part shelfImage: MultipartBody.Part
    ): Response<ApiResponse<ShelfSearchResult>>

    // 스웨거 기준: product_name = query
    // 백엔드가 멀티파트 file + query 조합을 받도록 구현돼 있다면 이 형태로 맞음.
    @Multipart
    @POST("api/v1/product/search/location")
    suspend fun searchProductLocation(
        @Part currentFrame: MultipartBody.Part,
        @Query("product_name") productName: String
    ): Response<ApiResponse<LocationSearchResult>>

    // AI-001: 우선 멀티파트 버전 유지
    @Multipart
    @POST("api/v1/vision/ai/analyze")
    suspend fun navGuide(
        @Part image: MultipartBody.Part
    ): Response<VisionAnalyzeResponse>

    // (선택) 스웨거가 JSON만 표기돼 있고 실제도 JSON이라면 이걸로 호출 테스트
    @POST("api/v1/vision/ai/analyze")
    suspend fun navGuideJson(
        @Body body: Map<String, String> // body["file"] 에 base64
    ): Response<VisionAnalyzeResponse>
}