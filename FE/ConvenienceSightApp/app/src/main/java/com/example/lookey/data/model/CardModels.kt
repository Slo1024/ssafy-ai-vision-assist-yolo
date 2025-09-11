package com.example.lookey.data.model

// 공통 API 응답 모델
data class ApiResponse<T>(
    val status: Int,
    val message: String,
    val result: T
)
