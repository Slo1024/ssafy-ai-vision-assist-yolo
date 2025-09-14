package com.example.lookey.data.network

import com.example.lookey.data.model.ApiResponse
import com.example.lookey.data.model.LoginResponse
import retrofit2.Response

class Repository {
    private val apiService = RetrofitClient.apiService

    // ===================== 회원 =====================

    //로그인
    suspend fun googleAuth(idToken: String): Response<LoginResponse> {
        return RetrofitClient.apiService.googleLogin("Bearer $idToken")
    }
}