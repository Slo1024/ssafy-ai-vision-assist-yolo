// CartViewModel.kt
package com.example.lookey.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.text.Normalizer

class CartViewModel : ViewModel() {

    private val dummyData = listOf(
        "코카콜라 제로 350ml",
        "코카콜라 제로 500ml",
        "펩시 제로 355ml",
        "스프라이트 500ml",
        "칠성사이다 500ml",
        "환타 오렌지 355ml",
    )

    private val _results = MutableStateFlow<List<String>>(emptyList())
    val results: StateFlow<List<String>> = _results

    // 한글/공백/대소문자 정규화
    private fun norm(s: String): String =
        Normalizer.normalize(s, Normalizer.Form.NFC)
            .lowercase()
            .replace("\\s+".toRegex(), "")

    fun search(query: String) {
        val q = norm(query)
        _results.value = if (q.isBlank()) {
            emptyList()
        } else {
            dummyData.filter { norm(it).contains(q) }
        }
    }

    /** 음성 인식 결과도 같은 경로로 태우면 됩니다. */
    fun onMicResult(text: String) = search(text)
}
