package com.example.lookey.data.model

data class CartListResponse(
    val carts: List<CartItem>
)

data class CartItem(
    val cartId: Int,
    val productId: Long,
    val productName: String
)
