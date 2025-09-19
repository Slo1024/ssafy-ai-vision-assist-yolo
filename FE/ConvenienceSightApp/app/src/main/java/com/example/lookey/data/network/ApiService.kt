package com.example.lookey.data.network

import com.example.lookey.data.model.ApiResponse
import com.example.lookey.data.model.CartAddRequest
import com.example.lookey.data.model.CartListResponse
import com.example.lookey.data.model.CartRemoveRequest
import com.example.lookey.data.model.LoginResponse
import com.example.lookey.data.model.ProductSearchResponse
import com.example.lookey.data.model.RefreshRequest
import com.example.lookey.data.model.allergy.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    // 구글
    @POST("api/auth/google")
    suspend fun googleLogin(
        @Header("Authorization") authorizationHeader: String
    ): Response<LoginResponse>

    // Refresh Token으로 새 Access Token 발급
    @POST("api/auth/refresh")
    fun refreshToken(@Body request: RefreshRequest): Response<LoginResponse>

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