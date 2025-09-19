package com.example.lookey.data.remote.dto

import com.google.gson.annotations.SerializedName

// 공통 래퍼를 ApiResponse<T>로 쓰므로 이 파일엔 result 본문만 정의
data class NavResult(
    val summary: String?,
    val actions: List<String> = emptyList(),
    val landmarks: List<NavLandmark> = emptyList(),
    val routes: NavRoutes? = null,
    @SerializedName("tts_hint") val ttsHint: String?
)

data class NavLandmark(
    val name: String,
    @SerializedName("direction_bucket") val directionBucket: String,
    @SerializedName("bbox_image_norm") val bboxImageNorm: List<Double> = emptyList(),
    val confidence: Double? = null
)

data class NavRoutes(
    val forward: NavRouteInfo? = null,
    val left: NavRouteInfo? = null,
    val right: NavRouteInfo? = null
)

data class NavRouteInfo(
    val clearance: String,          // "wide" | "narrow" | "blocked"
    val note: String? = null
)
