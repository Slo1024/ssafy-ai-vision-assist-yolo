package com.example.lookey.data.remote.dto.product

import com.google.gson.annotations.SerializedName

data class LocationSearchResult(
    @SerializedName("case") val caseType: String?,   // "DIRECTION" | "SINGLE_RECOGNIZED"
    @SerializedName("target") val target: Target?,
    @SerializedName("info") val info: Info?
) {
    data class Target(
        @SerializedName("name") val name: String?,
        @SerializedName("direction_bucket") val directionBucket: String?
    )
    data class Info(
        @SerializedName("name") val name: String?,
        @SerializedName("price") val price: Int?,
        @SerializedName("event") val event: String?,
        @SerializedName("allergy") val allergy: Boolean?
    )
}