package com.example.lookey.data.network

import com.example.lookey.data.model.ApiResponse
import com.example.lookey.data.model.LoginResponse
import retrofit2.Response

class Repository {
    private val apiService = RetrofitClient.apiService

    // ===================== 회원 =====================

    //로그인
    suspend fun googleAuth(accessToken: String): Response<ApiResponse<LoginResponse>> {
        val headerValue = "Bearer $accessToken"
        return apiService.googleLogin(authorizationHeader = headerValue)
    }
}