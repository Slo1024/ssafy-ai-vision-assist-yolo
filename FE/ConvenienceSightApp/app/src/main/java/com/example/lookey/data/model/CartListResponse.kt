package com.example.lookey.data.model

data class CartListResponse(
    val result: CartListResult
)

data class CartListResult(
    val items: List<CartItem>
)

data class CartItem(
    val cart_id: Int,
    val product_id: Long,
    val product_name: String
)

