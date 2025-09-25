package com.example.lookey.data.model

import com.google.gson.annotations.SerializedName

data class LoginData(
    @SerializedName("jwtToken")
    val jwtToken: String,

    @SerializedName(value = "refreshToken", alternate = ["refresh_token", "refresh"])
    val refreshToken: String? = null,  // 여러 가능한 필드명 처리

    @SerializedName("userId")
    val userId: Int,

    @SerializedName("userName")
    val userName: String
)