package com.example.lookey.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.lookey.domain.entity.DetectResult
import com.example.lookey.ui.scan.ResultFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class ScanViewModel(
    private val speak: (String) -> Unit = {}   // TTS 주입(없으면 무음)
) : ViewModel() {

    data class UiState(
        val scanning: Boolean = false,
        val current: DetectResult? = null,
        val banner: ResultFormatter.Banner? = null,
        // 모달 제어
        val showCartModal: Boolean = false,
        val cartTarget: DetectResult? = null
    )

    private val _ui = MutableStateFlow(UiState())
    val ui: StateFlow<UiState> = _ui

    // 간단 장바구니: 상품 id 집합으로 관리
    private val cartIds = mutableSetOf<String>()

    private var lastSpokenId: String? = null

    fun toggleScan() {
        _ui.update { it.copy(scanning = !it.scanning) }
    }

    fun onDetected(result: DetectResult) {
        val inCart = cartIds.contains(result.id)

        // 배너 구성
        val banner = if (inCart) {
            ResultFormatter.toCartBanner(result, inCart = true)
        } else {
            ResultFormatter.toBanner(result)
        }

        // 화면 상태 업데이트
        _ui.update {
            it.copy(
                current       = result,
                banner        = banner,
                showCartModal = inCart,                 // 장바구니에 있으면 모달 띄움
                cartTarget    = if (inCart) result else null
            )
        }

        // 같은 상품 반복 낭독 방지
        if (result.id != lastSpokenId) {
            val voice = if (inCart)
                ResultFormatter.toCartVoice(result, inCart = true)
            else
                ResultFormatter.toVoice(result)

            speak(voice.text)
            lastSpokenId = result.id
        }
    }

    // 장바구니 조작(현재 화면에서 필요 시 사용)
    fun addToCart(result: DetectResult) {
        if (cartIds.add(result.id)) {
            _ui.update { it.copy(banner = ResultFormatter.toCartBanner(result, inCart = false)) }
            speak("${result.name}를 장바구니에 담았습니다.")
        }
    }

    fun removeFromCart(result: DetectResult) {
        if (cartIds.remove(result.id)) {
            _ui.update { it.copy(banner = ResultFormatter.toCartBanner(result, inCart = true)) }
            speak("${result.name}를 장바구니에서 제거했습니다.")
        }
    }

    // 모달 핸들러
    fun onCartModalDismiss() {
        _ui.update { it.copy(showCartModal = false, cartTarget = null) }
    }

    fun onCartRemoveConfirm() {
        val target = _ui.value.cartTarget ?: return
        removeFromCart(target)
        _ui.update { it.copy(showCartModal = false, cartTarget = null) }
    }

    fun clearBanner() {
        _ui.update { it.copy(banner = null) }
    }
}
