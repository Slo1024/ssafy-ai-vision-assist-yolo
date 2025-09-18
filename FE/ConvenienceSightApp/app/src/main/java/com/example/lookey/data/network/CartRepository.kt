package com.example.lookey.data.network

import com.example.lookey.data.model.CartAddRequest
import com.example.lookey.data.model.CartListResponse
import com.example.lookey.data.model.ProductSearchResponse

class CartRepository(private val apiService: ApiService) {

//    suspend fun getCartList(): List<CartListResponse.Item>? {
//        val response = apiService.getCartList()
//        return if (response.isSuccessful) response.body()?.items else null
//    }

    suspend fun searchProducts(keyword: String): List<ProductSearchResponse.Item>? {
        val response = apiService.searchProducts(keyword)
        return if (response.isSuccessful) {
            response.body()?.result?.items
        } else {
            null
        }
    }


    suspend fun addToCart(productId: Int, quantity: Int): Boolean {
        val response = apiService.addToCart(CartAddRequest(productId, quantity))
        return response.isSuccessful
    }

//    suspend fun removeFromCart(cartId: Int): Boolean {
//        val response = apiService.removeFromCart(CartRemoveRequest(cartId))
//        return response.isSuccessful
//    }
}
