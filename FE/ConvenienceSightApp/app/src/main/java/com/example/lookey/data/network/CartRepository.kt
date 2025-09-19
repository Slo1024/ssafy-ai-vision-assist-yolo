package com.example.lookey.data.network

import android.util.Log
import com.example.lookey.data.model.CartAddRequest
import com.example.lookey.data.model.CartListResponse
import com.example.lookey.data.model.CartRemoveRequest
import com.example.lookey.data.model.ProductSearchResponse
import com.example.lookey.ui.viewmodel.CartLine

class CartRepository(private val apiService: ApiService) {

    suspend fun getCartList(): List<CartLine> {
        val response = apiService.getCartList()
        Log.d("CartRepo", "Response: $response")
        if (response.isSuccessful) {
            val body = response.body()
            Log.d("CartRepo", "Body: $body")
            return body?.result?.items?.map {
                CartLine(cartId = it.cart_id,
                    productId = it.product_id.toInt(),
                    name = it.product_name)
            } ?: emptyList()
        } else {
            Log.e("CartRepo", "getCartList failed: ${response.code()}")
        }
        return emptyList()
    }



    suspend fun searchProducts(keyword: String): List<ProductSearchResponse.Item>? {
        val response = apiService.searchProducts(keyword)
        return if (response.isSuccessful) {
            response.body()?.result?.items
        } else {
            null
        }
    }


    suspend fun addCartItem(productId: Int): Boolean {
        val response = apiService.addToCart(CartAddRequest(productId))
        return response.isSuccessful
    }

    suspend fun removeCartItem(cartId: Int): Boolean {
        val response = apiService.removeFromCart(CartRemoveRequest(cartId))
        return response.isSuccessful
    }
}
