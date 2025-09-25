// CartViewModel.kt
package com.example.lookey.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.lookey.data.network.CartRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.text.Normalizer
import androidx.lifecycle.viewModelScope
import com.example.lookey.data.model.ProductSearchResponse
import kotlinx.coroutines.launch


// qty는 안 쓰지만, 추후 확장 대비해 남겨둠 (항상 1)
data class CartLine(val cartId: Int? = null, val productId: Int? = null, val name: String? = null, val qty: Int = 1)

class CartViewModel(private val repository: CartRepository) : ViewModel() {

    private val _results = MutableStateFlow<List<ProductSearchResponse.Item>>(emptyList())
    val results: StateFlow<List<ProductSearchResponse.Item>> = _results


    // 장바구니: 중복 없이 한 줄만 유지
    private val _cart = MutableStateFlow<List<CartLine>>(emptyList())
    val cart: StateFlow<List<CartLine>> = _cart

    private fun norm(s: String) =
        Normalizer.normalize(s, Normalizer.Form.NFC)
            .lowercase()
            .replace("\\s+".toRegex(), "")

    fun loadCart() {
        viewModelScope.launch {
            try {
                val list = repository.getCartList()
                Log.d("CartViewModel", "Loaded cart: $list")
                _cart.value = list
            } catch (e: Exception) {
                Log.e("CartViewModel", "장바구니 조회 실패", e)
                _cart.value = emptyList()
            }
        }
    }


    fun searchProducts(keyword: String) {
        viewModelScope.launch {
            try {
                val items = repository.searchProducts(keyword) ?: emptyList()
                _results.value = items
            } catch (e: Exception) {
                Log.e("CartViewModel", "검색 실패", e)
                _results.value = emptyList()
            }
        }
    }

    /** 담기: 이미 있으면 그대로(중복 X), 없으면 추가 */
    fun addToCart(productId: Int, name: String) {
        viewModelScope.launch {
            try {
                val success = repository.addCartItem(productId)
                if (success) {
                    // 서버에서 최신 장바구니 가져오기
                    val latestCart = repository.getCartList()
                    _cart.value = latestCart
                }
            } catch (e: Exception) {
                Log.e("CartViewModel", "장바구니 추가 실패", e)
            }
        }
    }



    /** 삭제: 해당 항목 한 번에 제거 */
    fun removeFromCart(cartId: Int) {
        viewModelScope.launch {
            try {
                val success = repository.removeCartItem(cartId)
                if (success) {
                    // 화면에서 해당 항목 제거
                    _cart.update { list -> list.filterNot { it.cartId == cartId } }
                }
            } catch (e: Exception) {
                Log.e("CartViewModel", "장바구니 삭제 실패", e)
            }
        }
    }

}
