package com.example.lookey.ui.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lookey.domain.entity.DetectResult
import com.example.lookey.ui.scan.ResultFormatter
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ScanViewModel(
    private val speak: (String) -> Unit = {},
    private val isInCart: (String) -> Boolean = { false },
    private val removeFromCart: (String) -> Unit = {}
) : ViewModel() {

    enum class Mode { SCAN, GUIDE }

    data class UiState(
        val mode: Mode = Mode.SCAN,                 // 하단 토글 상태
        val scanning: Boolean = false,              // “탐색 중” 플래그(초광각 모드 유지)
        val capturing: Boolean = false,             // 촬영 중
        val current: DetectResult? = null,
        val banner: ResultFormatter.Banner? = null,
        val capturedFrames: List<Bitmap> = emptyList(), // ← 촬영된 프레임들(4장)
        // 장바구니 관련 모달
        val showCartModal: Boolean = false,
        val cartTarget: DetectResult? = null
    )

    private val _ui = MutableStateFlow(UiState())
    val ui: StateFlow<UiState> = _ui

    private var lastSpokenId: String? = null

    fun setMode(mode: Mode) {
        _ui.update {
            it.copy(
                mode = mode,
                scanning = if (mode == Mode.SCAN) it.scanning else false,
                capturing = false
            )
        }
    }

    /** “상품 탐색 시작” 클릭 → 초광각 모드로 전환, 3초간 4장 캡처 후 자동 종료 */
    fun startPanorama() {
        if (_ui.value.mode != Mode.SCAN) return

        viewModelScope.launch {
            // 시작 시 이전 촬영본 비우고 시작
            _ui.update { it.copy(scanning = true, capturing = true, capturedFrames = emptyList()) }

            // 0초(즉시), 1초, 2초, 3초 → 총 4장 촬영
            repeat(4) { idx ->
                delay(if (idx == 0) 0 else 1000)
                captureFrame(idx)
            }

            // 촬영/스캔 종료
            _ui.update { it.copy(capturing = false, scanning = false) }

            // 종료 배너
            _ui.update {
                it.copy(
                    banner = ResultFormatter.Banner(
                        type = ResultFormatter.Banner.Type.SUCCESS,
                        text = "상품 인식이 종료되었습니다."
                    )
                )
            }
        }
    }

    /** 사진 캡처 (Stub) — 나중에 CameraX ImageCapture + API 업로드로 교체 */
    private fun captureFrame(index: Int) {
        // TODO: 실제 구현: CameraX ImageCapture로 Bitmap/파일을 획득
        // 지금은 더미 1x1 비트맵을 넣어두기 (컴파일/런타임 안전)
        val placeholder = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        _ui.update { it.copy(capturedFrames = it.capturedFrames + placeholder) }

        println("📷 ${index + 1}번째 사진 촬영됨 (placeholder)")
    }

    /** 필요 시 외부에서 촬영본 비우기 */
    fun clearCapturedFrames() {
        _ui.update { it.copy(capturedFrames = emptyList()) }
    }

    /** 더미 감지 결과 수신(실 서비스에선 실제 인식 결과 콜백에서 호출) */
    fun onDetected(result: DetectResult) {
        val inCart = isInCart(result.id)
        val banner = if (inCart) {
            ResultFormatter.toCartBanner(result, inCart = true)
        } else {
            ResultFormatter.toBanner(result)
        }

        _ui.update {
            it.copy(
                current = result,
                banner = banner,
                showCartModal = inCart,
                cartTarget = if (inCart) result else null
            )
        }

        if (result.id != lastSpokenId) {
            val voice = if (inCart)
                ResultFormatter.toCartVoice(result, inCart = true)
            else
                ResultFormatter.toVoice(result)
            speak(voice.text)
            lastSpokenId = result.id
        }
    }

    fun onCartModalDismiss() {
        _ui.update { it.copy(showCartModal = false, cartTarget = null) }
    }

    fun onCartRemoveConfirm() {
        val target = _ui.value.cartTarget ?: return
        removeFromCart(target.id)
        speak("${target.name}를 장바구니에서 제거했습니다.")
        _ui.update { it.copy(showCartModal = false, cartTarget = null) }
    }

    fun clearBanner() {
        _ui.update { it.copy(banner = null) }
    }
}
