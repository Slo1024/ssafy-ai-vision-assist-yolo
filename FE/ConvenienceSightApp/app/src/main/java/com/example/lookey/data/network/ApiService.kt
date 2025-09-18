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
}
