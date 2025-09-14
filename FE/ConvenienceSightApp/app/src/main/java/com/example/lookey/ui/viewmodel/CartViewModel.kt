// CartViewModel.kt
package com.example.lookey.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.text.Normalizer

// qty는 안 쓰지만, 추후 확장 대비해 남겨둠 (항상 1)
data class CartLine(val name: String, val qty: Int = 1)

class CartViewModel : ViewModel() {

    private val dummyData = listOf(
        "코카콜라 제로 350ml",
        "코카콜라 제로 500ml",
        "펩시 제로 355ml",
        "스프라이트 500ml"
    )

    private val _results = MutableStateFlow<List<String>>(emptyList())
    val results: StateFlow<List<String>> = _results

    // 장바구니: 중복 없이 한 줄만 유지
    private val _cart = MutableStateFlow<List<CartLine>>(emptyList())
    val cart: StateFlow<List<CartLine>> = _cart

    private fun norm(s: String) =
        Normalizer.normalize(s, Normalizer.Form.NFC)
            .lowercase()
            .replace("\\s+".toRegex(), "")

    fun search(query: String) {
        val q = norm(query)
        _results.value = if (q.isBlank()) emptyList()
        else dummyData.filter { norm(it).contains(q) }
    }

    /** 담기: 이미 있으면 그대로(중복 X), 없으면 추가 */
    fun addToCart(name: String) {
        _cart.update { list ->
            if (list.any { it.name == name }) list
            else list + CartLine(name)
        }
    }

    /** 삭제: 해당 항목 한 번에 제거 */
    fun removeFromCart(name: String) {
        _cart.update { list -> list.filterNot { it.name == name } }
    }
}
