package com.example.lookey.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lookey.domain.entity.Allergy
import com.example.lookey.domain.repo.AllergyRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AllergyUiState(
    val loading: Boolean = false,
    val myAllergies: List<Allergy> = emptyList(),
    val suggestions: List<Allergy> = emptyList(),
    val query: String = "",
    val message: String? = null
)

class AllergyViewModel(
    private val repo: AllergyRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AllergyUiState())
    val state: StateFlow<AllergyUiState> = _state

    private var searchJob: Job? = null
    private var inFlight = false                 // ✅ 진행중 가드
    private var lastQuery: String? = null        // ✅ 같은 쿼리 중복 방지

    fun load() = viewModelScope.launch {
        Log.d("AllergyVM", "load() called")
        _state.update { it.copy(loading = true, message = null) }
        runCatching { repo.list() }
            .onSuccess { list ->
                Log.d("AllergyVM", "load() success: ${list.size} items")
                list.forEach { Log.d("AllergyVM", "Item: ${it.name} (id=${it.id}, listId=${it.allergyListId})") }
                _state.update { it.copy(loading = false, myAllergies = list) }
            }
            .onFailure { e ->
                Log.e("AllergyVM", "load() failed: ${e.message}")
                _state.update { it.copy(loading = false, message = cleanMsg(e)) }
            }
    }

    fun updateQuery(q: String) {
        _state.update { it.copy(query = q) }
    }

    fun doSearch(q: String? = null) {
        val query = (q ?: _state.value.query).trim()
        _state.update { it.copy(query = query) }

        if (query.isEmpty()) {
            searchJob?.cancel()
            lastQuery = null
            _state.update { it.copy(suggestions = emptyList()) }
            return
        }

        // 🔑 이전과 동일 쿼리로 요청 중이면 무시 (isActive 기준)
        if (searchJob?.isActive == true && lastQuery == query) return

        lastQuery = query
        // 새 검색을 위해 이전 Job 취소
        searchJob?.cancel()
        _state.update { it.copy(loading = true, message = null) }
        searchJob = viewModelScope.launch {
            try {
                // (원하면) 아주 짧은 디바운스
//                delay(120)
                val list = repo.search(query)
                _state.update { it.copy(loading = false, suggestions = list, message = null) }
            } catch (e: Throwable) {
                _state.update { it.copy(loading = false, suggestions = emptyList(), message = cleanMsg(e)) }
            }
        }
    }



    fun add(allergyListId: Long) = viewModelScope.launch {
        Log.d("AllergyVM", "add() called with allergyListId: $allergyListId")
        _state.update { it.copy(loading = true, message = null) }
        try {
            repo.add(allergyListId) // allergyListId 사용
            Log.d("AllergyVM", "add() API call successful")
            // 성공 시 즉시 검색 결과 초기화하고 내 알러지 목록 새로고침
            _state.update { it.copy(suggestions = emptyList(), query = "") }

            // 리스트 다시 로드
            Log.d("AllergyVM", "Reloading list after add...")
            runCatching { repo.list() }
                .onSuccess { list ->
                    Log.d("AllergyVM", "Reload after add success: ${list.size} items")
                    _state.update { it.copy(loading = false, myAllergies = list) }
                }
                .onFailure { e ->
                    Log.e("AllergyVM", "Reload after add failed: ${e.message}")
                    _state.update { it.copy(loading = false, message = cleanMsg(e)) }
                }
        } catch (e: Exception) {
            Log.e("AllergyVM", "add() failed: ${e.message}")
            _state.update { it.copy(loading = false, message = cleanMsg(e)) }
        }
    }

    fun delete(allergyListId: Long) = viewModelScope.launch {
        Log.d("AllergyVM", "delete() called with allergyListId: $allergyListId")
        _state.update { it.copy(loading = true, message = null) }
        try {
            repo.delete(allergyListId) // allergyListId 사용
            Log.d("AllergyVM", "delete() API call successful")

            // 리스트 다시 로드
            Log.d("AllergyVM", "Reloading list after delete...")
            runCatching { repo.list() }
                .onSuccess { list ->
                    Log.d("AllergyVM", "Reload after delete success: ${list.size} items")
                    _state.update { it.copy(loading = false, myAllergies = list) }
                }
                .onFailure { e ->
                    Log.e("AllergyVM", "Reload after delete failed: ${e.message}")
                    _state.update { it.copy(loading = false, message = cleanMsg(e)) }
                }
        } catch (e: Exception) {
            Log.e("AllergyVM", "delete() failed: ${e.message}")
            _state.update { it.copy(loading = false, message = cleanMsg(e)) }
        }
    }

    fun consumeMessage() { _state.update { it.copy(message = null) } }

    private fun cleanMsg(e: Throwable): String {
        // 5xx HTML 덩어리 정리
        val m = e.message.orEmpty()
        return if (m.contains("HTTP 5")) "서버가 잠시 불안정해요. 잠시 후 다시 시도해주세요."
        else m
    }
}
