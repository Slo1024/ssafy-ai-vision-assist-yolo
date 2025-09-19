// app/src/main/java/com/example/lookey/data/network/ApiService.kt
package com.example.lookey.data.network

import com.example.lookey.data.model.ApiResponse
import com.example.lookey.data.model.LoginResponse
import com.example.lookey.data.model.RefreshRequest
import com.example.lookey.data.remote.dto.*
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

    // PRODUCT-005
    @Multipart
    @POST("api/product/search")
    suspend fun searchShelf(
        @Part images: List<MultipartBody.Part>
    ): Response<ApiResponse<ShelfSearchResult>>

    // PRODUCT-006
    @Multipart
    @POST("api/product/search/location")
    suspend fun searchProductLocation(
        @Part currentFrame: MultipartBody.Part,
        @Part("product_name") productName: RequestBody
    ): Response<ApiResponse<LocationSearchResult>>

    // NAV-001: 길 안내
    @Multipart
    @POST("api/NAV")
    suspend fun navGuide(
        @Part image: MultipartBody.Part
    ): Response<ApiResponse<NavResult>>





    // === AI-001: 편의점 구조 분석 ===
    @Multipart
    @POST("api/v1/vision/ai")
    suspend fun aiVisionAnalyze(
        @Part image: MultipartBody.Part // name="image"
    ): Response<AiVisionResponse>

    // === AI-002: 매대에 상품존재 여부 (AI 간단 응답) ===
    @Multipart
    @POST("api/product/search/ai")
    suspend fun aiShelfSearch(
        @Part images: List<MultipartBody.Part>,          // name="shelf_images" (4장)
        @Part("cart_product_names") cartNames: RequestBody // application/json
    ): Response<AiShelfSearchResponse>

    // === AI-003: 화면으로부터 상품 위치 (AI 간단 응답) ===
    @Multipart
    @POST("api/product/location/ai")
    suspend fun aiLocation(
        @Part currentFrame: MultipartBody.Part,         // name="current_frame"
        @Part("product_name") productName: RequestBody  // text/plain
    ): Response<AiLocationResponse>



}
