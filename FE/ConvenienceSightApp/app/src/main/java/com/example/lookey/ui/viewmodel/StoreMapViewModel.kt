package com.example.lookey.ui.storemap

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lookey.data.network.PathRepository
import com.example.lookey.domain.location.LocationProvider
import com.example.lookey.network.dto.PlaceItemDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class StoreUiModel(
    val name: String,
    val distanceMeters: Int,
    val lat: Double,
    val lng: Double
)

data class FinderState(
    val isLoading: Boolean = false,
    val stores: List<StoreUiModel> = emptyList(),
    val error: String? = null,
    val myLat: Double? = null,
    val myLng: Double? = null
)

class StoreFinderViewModel(
    private val repo: PathRepository,
    private val locationProvider: LocationProvider
) : ViewModel() {

    private val _ui = MutableStateFlow(FinderState())
    val ui: StateFlow<FinderState> = _ui

    fun findNearby(context: Context) {
        viewModelScope.launch {
            _ui.value = _ui.value.copy(isLoading = true, error = null)
            try {
                val loc = locationProvider.getCurrentLocation()
                if (loc == null) {
                    _ui.value = _ui.value.copy(isLoading = false, error = "현재 위치를 가져올 수 없습니다.")
                    return@launch
                }
                val items = repo.nearby(loc.latitude, loc.longitude)
                val stores = items.map { dto -> mapToUi(dto) }.take(3)
                _ui.value = FinderState(isLoading = false, stores = stores, myLat = loc.latitude, myLng = loc.longitude)
            } catch (e: Exception) {
                _ui.value = FinderState(isLoading = false, error = "가까운 편의점 조회 실패: ${e.message}")
            }
        }
    }

    private fun mapToUi(dto: PlaceItemDto) : StoreUiModel {
        val dist = dto.distance ?: run {
            // distance 가 없으면 대략 0으로
            0
        }
        return StoreUiModel(dto.name, dist, dto.lat, dto.lng)
    }

    // 사용자가 편의점 클릭했을 때 호출해서 카카오맵 열기 처리는 액티비티/컴포저블에서 context로 함
}
