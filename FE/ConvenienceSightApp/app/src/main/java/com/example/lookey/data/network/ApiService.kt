package com.example.lookey.data.network

import com.example.lookey.data.model.ApiResponse
import com.example.lookey.data.model.CartAddRequest
import com.example.lookey.data.model.CartListResponse
import com.example.lookey.data.model.CartRemoveRequest
import com.example.lookey.data.model.LoginResponse
import com.example.lookey.data.model.ProductSearchResponse
import com.example.lookey.data.model.RefreshRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    // 구글
    @POST("api/auth/google")
    suspend fun googleLogin(
        @Header("Authorization") authorizationHeader: String
    ): Response<LoginResponse>

    // Refresh Token으로 새 Access Token 발급
    @POST("api/auth/refresh")
    fun refreshToken(@Body request: RefreshRequest): Response<LoginResponse>

    @GET("api/v1/carts")
    suspend fun getCartList(): Response<CartListResponse>

    @GET("api/v1/carts/search/{searchword}")
    suspend fun searchProducts(@Path("searchword") keyword: String): Response<ProductSearchResponse>

    @POST("api/v1/carts")
    suspend fun addToCart(@Body request: CartAddRequest): Response<Void>

    @DELETE("api/v1/carts")
    suspend fun removeFromCart(@Query("cart_id") cartId: Int): Response<Void>


}