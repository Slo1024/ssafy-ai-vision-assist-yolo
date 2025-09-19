// app/src/main/java/com/example/lookey/data/network/ApiService.kt
package com.example.lookey.data.network

import com.example.lookey.data.model.ApiResponse
import com.example.lookey.data.model.CartAddRequest
import com.example.lookey.data.model.CartListResponse
import com.example.lookey.data.model.CartRemoveRequest
import com.example.lookey.data.model.LoginResponse
import com.example.lookey.data.model.ProductSearchResponse
import com.example.lookey.data.model.RefreshRequest
import com.example.lookey.data.remote.dto.navigation.VisionAnalyzeResponse
import com.example.lookey.data.remote.dto.product.LocationSearchResult
import com.example.lookey.data.remote.dto.product.ShelfSearchResult
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*
import com.example.lookey.data.model.allergy.*


interface ApiService {
    @POST("api/auth/google")
    suspend fun googleLogin(
        @Header("Authorization") authorizationHeader: String
    ): Response<LoginResponse>

    @POST("api/auth/refresh")
    fun refreshToken(@Body request: RefreshRequest): Response<LoginResponse>

    // 005: 그대로 multipart
    @Multipart
    @POST("api/v1/product/search")
    suspend fun searchShelf(
        @Part shelfImage: MultipartBody.Part
    ): Response<ApiResponse<ShelfSearchResult>>

    // 006: 스웨거 정의가 JSON + query 인 경우를 지원
    @POST("api/v1/product/search/location")
    suspend fun searchProductLocationJson(
        @Query("product_name") productName: String,
        @Body body: Map<String, String>   // { "current_frame": "<base64>" }
    ): Response<ApiResponse<LocationSearchResult>>

    // 기존 multipart 메서드는 유지(서버가 멀티파트로 바꾸면 사용)
    @Multipart
    @POST("api/v1/product/search/location")
    suspend fun searchProductLocation(
        @Part currentFrame: MultipartBody.Part,
        @Query("product_name") productName: String
    ): Response<ApiResponse<LocationSearchResult>>

    // AI-001: JSON 버전 추가 (스웨거 기준)
    @POST("api/v1/vision/ai/analyze")
    suspend fun navGuideJson(
        @Body body: Map<String, String>   // { "file": "<base64>" }
    ): Response<VisionAnalyzeResponse>

    // 기존 multipart 메서드도 유지(서버가 멀티파트 허용 시 사용)
    @Multipart
    @POST("api/v1/vision/ai/analyze")
    suspend fun navGuide(
        @Part image: MultipartBody.Part
    ): Response<VisionAnalyzeResponse>

    // ---- 알레르기: 내 목록 조회 ----
    @GET("api/v1/allergy")
    suspend fun getAllergies(): Response<AllergyGetResponse>


    @GET("api/v1/allergy/search/{searchword}")
    suspend fun searchAllergies(
        @Path("searchword") searchword: String
    ): Response<AllergySearchResponse>


    // ---- 알레르기: 추가 ----
    @POST("api/v1/allergy")
    suspend fun addAllergy(
        @Body body: AllergyPostRequest
    ): Response<AllergyAddResponse>

    // ---- 알레르기: 삭제 (body에 allergy_id 담아 보냄) ----
    @HTTP(method = "DELETE", path = "api/v1/allergy", hasBody = true)
    suspend fun deleteAllergy(
        @Body body: AllergyDeleteRequest
    ): Response<AllergyDeleteResponse>

    @GET("api/v1/carts")
    suspend fun getCartList(): Response<CartListResponse>

    @GET("api/v1/carts/search/{searchword}")
    suspend fun searchProducts(@Path("searchword") keyword: String): Response<ProductSearchResponse>

    @POST("api/v1/carts")
    suspend fun addToCart(@Body request: CartAddRequest): Response<Void>

    @HTTP(method = "DELETE", path = "api/v1/carts", hasBody = true)
    suspend fun removeFromCart(@Body request: CartRemoveRequest): Response<Void>
}