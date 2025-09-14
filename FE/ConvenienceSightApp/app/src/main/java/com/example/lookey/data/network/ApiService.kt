package com.example.lookey.data.network

import com.example.lookey.data.model.ApiResponse
import com.example.lookey.data.model.LoginResponse
import retrofit2.Response
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {
    // 구글
    @POST("api/auth/google")
    suspend fun googleLogin(
        @Header("Authorization") authorizationHeader: String
    ): Response<LoginResponse>
}