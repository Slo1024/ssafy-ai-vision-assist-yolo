package com.example.lookey.data.model

import com.google.gson.annotations.SerializedName

data class ProductSearchResponse(
    val status: Int,
    val message: String,
    val result: Result
) {
    data class Result(
        val items: List<Item>
    )

    data class Item(
        @SerializedName("product_id") val productId: Long,
        @SerializedName("product_name") val productName: String
    )
}
