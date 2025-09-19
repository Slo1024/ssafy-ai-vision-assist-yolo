package com.example.lookey.data.remote.dto

import com.google.gson.annotations.SerializedName

// === AI-001 /api/v1/vision/ai =========================
data class AiVisionResponse(
    val data: AiVisionData,
    val success: Boolean,
    val message: String,
    val timestamp: Long
)

data class AiVisionData(
    val directions: TripleBool,   // 이동 가능 방향
    val obstacles: TripleBool,    // 장애물 존재
    val counter: Boolean,         // 계산대 감지 여부 (방향 정보는 없음)
    val category: String,         // "beverage" | "snack" | "unknown"
    val people: TripleBool        // 사람 위치
)

data class TripleBool(
    val left: Boolean,
    val front: Boolean,
    val right: Boolean
)

// === AI-002 /api/product/search/ai =====================
data class AiShelfSearchResponse(
    @SerializedName("matched_names") val matchedNames: List<String>
)

// === AI-003 /api/product/location/ai ===================
data class AiLocationResponse(
    val case: String,     // "DIRECTION" | "SINGLE_RECOGNIZED" | "NotFound"
    val output: String?   // 방향 버킷 또는 단일 인식된 상품명
)
