package com.example.lookey.data.remote.dto.product


import com.google.gson.annotations.SerializedName

data class ShelfSearchResult(
    @SerializedName("count") val count: Int?,
    @SerializedName("matched_names") val matchedNames: List<String>?
)