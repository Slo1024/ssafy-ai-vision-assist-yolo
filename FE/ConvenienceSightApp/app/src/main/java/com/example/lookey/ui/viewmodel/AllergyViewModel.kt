package com.example.lookey.ui.viewmodel


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
        _state.update { it.copy(loading = true, message = null) }
        runCatching { repo.list() }
            .onSuccess { list -> _state.update { it.copy(loading = false, myAllergies = list) } }
            .onFailure { e -> _state.update { it.copy(loading = false, message = cleanMsg(e)) } }
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



    fun add(allergyId: Long) = viewModelScope.launch {
        runCatching { repo.add(allergyId) }
            .onSuccess { load() }
            .onFailure { e -> _state.update { it.copy(message = cleanMsg(e)) } }
    }

    fun delete(allergyId: Long) = viewModelScope.launch {
        val before = _state.value.myAllergies
        _state.update { it.copy(myAllergies = before.filterNot { a -> a.id == allergyId }) }
        runCatching { repo.delete(allergyId) }
            .onFailure { e -> _state.update { it.copy(myAllergies = before, message = cleanMsg(e)) } }
    }

    fun consumeMessage() { _state.update { it.copy(message = null) } }

    private fun cleanMsg(e: Throwable): String {
        // 5xx HTML 덩어리 정리
        val m = e.message.orEmpty()
        return if (m.contains("HTTP 5")) "서버가 잠시 불안정해요. 잠시 후 다시 시도해주세요."
        else m
    }
}
