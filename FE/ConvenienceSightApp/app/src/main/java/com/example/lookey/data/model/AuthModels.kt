package com.example.lookey.data.model

data class LoginResponse(
    val jwtToken: String,
    val userId: String
)