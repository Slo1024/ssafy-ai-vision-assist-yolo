package com.example.lookey.network.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PlaceResponseDto(
    val status: Int,
    val message: String,
    val result: ResultDto?
)

@JsonClass(generateAdapter = true)
data class ResultDto(
    val items: List<PlaceItemDto>
)

@JsonClass(generateAdapter = true)
data class PlaceItemDto(
    val name: String,
    val address: String,
    val lat: Double,
    val lng: Double,
    val distance: Int? = null,
    val brand: String? = null,
    val placeId: String? = null
)
