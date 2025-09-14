package com.example.lookey.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.text.Normalizer

class AllergyViewModel : ViewModel() {
    // 더미 데이터(원하면 서버/로컬로 교체)
    private val dummyData = listOf(
        "우유", "계란", "밀", "메밀", "땅콩", "호두",
        "대두(콩)", "잣", "돼지고기", "소고기", "닭고기",
        "새우", "게", "오징어", "고등어", "조개류"
    )

    private val _results = MutableStateFlow<List<String>>(emptyList())
    val results: StateFlow<List<String>> = _results

    // 내 알레르기 목록(중복 없이)
    private val _allergies = MutableStateFlow<List<String>>(emptyList())
    val allergies: StateFlow<List<String>> = _allergies

    private fun norm(s: String) =
        Normalizer.normalize(s, Normalizer.Form.NFC)
            .lowercase()
            .replace("\\s+".toRegex(), "")

    fun search(query: String) {
        val q = norm(query)
        _results.value = if (q.isBlank()) emptyList()
        else dummyData.filter { norm(it).contains(q) }
    }

    fun addAllergy(name: String) {
        _allergies.update { list -> if (name in list) list else list + name }
    }

    fun removeAllergy(name: String) {
        _allergies.update { it.filterNot { it == name } }
    }
}
