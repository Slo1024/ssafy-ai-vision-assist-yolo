package com.example.lookey.data.remote.dto

import com.google.gson.annotations.SerializedName

// ---------- PRODUCT-005 (매대 4장 결과) ----------
data class ShelfSearchResult(
    val count: Int,
    @SerializedName("matched_names") val matchedNames: List<String>
)

// ---------- PRODUCT-006 (현재 화면 + 상품명 결과) ----------
data class LocationSearchResult(
    @SerializedName("case") val caseType: String, // "DIRECTION" | "SINGLE_RECOGNIZED"
    val target: TargetInfo?, // case=DIRECTION
    val info: ProductInfo?   // case=SINGLE_RECOGNIZED
)

data class TargetInfo(
    val name: String,
    @SerializedName("direction_bucket") val directionBucket: String
)

data class ProductInfo(
    val name: String,
    val price: Int?,
    @SerializedName("event") val event: String?,
    @SerializedName("allergy") val allergy: Boolean
)