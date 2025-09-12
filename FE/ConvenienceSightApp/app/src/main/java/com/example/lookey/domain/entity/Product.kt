package com.example.lookey.domain.entity

data class DetectResult(
    val id: String,            // 바코드 or 임시 UUID
    val name: String,          // "코카콜라 제로 500ml"
    val price: Int?,           // 2200
    val promo: String?,        // "1+1", "2+1"
    val hasAllergy: Boolean,   // 사용자 알레르기 매칭 여부
    val allergyNote: String?,  // "유당 포함"
    val confidence: Float,     // 0.0 ~ 1.0
)