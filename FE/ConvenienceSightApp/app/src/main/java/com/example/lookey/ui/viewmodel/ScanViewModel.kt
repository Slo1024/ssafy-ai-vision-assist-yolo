package com.example.lookey.ui.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lookey.domain.entity.DetectResult
import com.example.lookey.ui.cart.CartPort
import com.example.lookey.ui.scan.ResultFormatter
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

class ScanViewModel(
    private val speak: (String) -> Unit = {},
    private val cart: CartPort? = null   // ✅ CartPort 하나만 주입
) : ViewModel() {

    enum class Mode { SCAN, GUIDE }

    /** 9방향 버킷 */
    enum class DirectionBucket(val label: String) {
        LEFT_UP("왼쪽 위"),
        UP("위"),
        RIGHT_UP("오른쪽 위"),
        LEFT("왼쪽"),
        CENTER("가운데"),
        RIGHT("오른쪽"),
        LEFT_DOWN("왼쪽 아래"),
        DOWN("아래"),
        RIGHT_DOWN("오른쪽 아래")
    }

    data class UiState(
        val mode: Mode = Mode.SCAN,                 // 하단 토글 상태
        val scanning: Boolean = false,              // “탐색 중”(광각 유지)
        val capturing: Boolean = false,             // 3초/4장 촬영 중
        val current: DetectResult? = null,          // 화면에서 감지된 top-1(옵션)
        val banner: ResultFormatter.Banner? = null,

        // 파노라마 캡처 결과(005용) — 지금은 보관만 (API 미연동)
        val capturedFrames: List<Bitmap> = emptyList(),

        // 장바구니 순차 안내 큐
        val cartGuideQueue: List<String> = emptyList(), // 매대에서 확인된 장바구니 상품명들
        val cartGuideTargetName: String? = null,        // 현재 안내 대상
        val showCartGuideModal: Boolean = false,        // “안내할까요?” 모달

        // 위치 안내(006 흐름)
        val guiding: Boolean = false,                   // 1초 루프 On
        val guideDirection: DirectionBucket? = null,    // 최근 방향 버킷

        // 길 안내(별개 축)
        val guideMsg: String? = null,
        val guideTicking: Boolean = false
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

    /** FeaturePill: “상품 탐색 시작” → 3초간 4장 캡처 후 종료 + 매대 확인 큐 구성(스텁) */
    fun startPanorama() {
        if (_ui.value.mode != Mode.SCAN) return

        viewModelScope.launch {
            // 초기화
            _ui.update {
                it.copy(
                    scanning = true,
                    capturing = true,
                    capturedFrames = emptyList(),
                    banner = null,
                    cartGuideQueue = emptyList(),
                    cartGuideTargetName = null,
                    showCartGuideModal = false
                )
            }

            // 0/1/2/3초 캡처 (스텁 비트맵)
            repeat(4) { idx ->
                delay(if (idx == 0) 0 else 1000)
                captureFrame(idx)
            }

            // 촬영 종료 + 스캔 종료
            _ui.update { it.copy(capturing = false, scanning = false) }

            // (005 스텁) 매대에서 장바구니 상품 매칭 → 큐 구성
            val matched = stubCheckShelfForCartItems(_ui.value.capturedFrames)
            val next = matched.firstOrNull()

            // 종료 배너
            _ui.update {
                it.copy(
                    banner = ResultFormatter.Banner(
                        type = ResultFormatter.Banner.Type.SUCCESS,
                        text = "상품 인식이 종료되었습니다."
                    ),
                    cartGuideQueue = matched,
                    cartGuideTargetName = next,
                    showCartGuideModal = (next != null)
                )
            }
        }
    }

    /** 스텁: 005 응답 대체 — 장바구니 목록 일부를 ‘매칭’된 것으로 간주 */
    private fun stubCheckShelfForCartItems(frames: List<Bitmap>): List<String> {
        val names = cart?.namesSnapshot().orEmpty()
        if (frames.isEmpty() || names.isEmpty()) return emptyList()
        // 데모: 1~3개 랜덤 매칭
        val count = Random.nextInt(1, minOf(3, names.size) + 1)
        return names.shuffled().take(count)
    }

    /** 모달: “예” → 006 흐름 시작(방향→단일 인식→정보 배너→장바구니 제거→다음으로) */
    fun onCartGuideConfirm() {
        val target = _ui.value.cartGuideTargetName ?: return
        _ui.update { it.copy(showCartGuideModal = false, guiding = true, guideDirection = null) }
        start006StubLoop(target)
    }

    /** 모달: “아니요” → 이번 상품은 스킵하고 다음으로 */
    fun onCartGuideSkip() {
        proceedToNextCartTarget()
    }

    private fun start006StubLoop(targetName: String) {
        viewModelScope.launch {
            // 1~2초 동안 방향만 안내 → 그 후 단일 인식 ‘정보’ 도착 스텁
            val directionTicks = Random.nextInt(1, 3) // 1~2번
            repeat(directionTicks) {
                delay(1000)
                val dir = DirectionBucket.values().random()
                _ui.update { it.copy(guideDirection = dir) }
                speak("$targetName 이(가) ${dir.label}에 있습니다.")
            }

            // 단일 인식 완료(정보 도착) 스텁
            delay(500)
            val info = DetectResult(
                id = targetName,                 // 구현부에서 name=ID로 매핑 처리
                name = targetName,
                price = listOf(1500, 1700, 2000, 2200, 2500).random(),
                promo = listOf("1+1", "2+1", null).random(),
                hasAllergy = listOf(true, false).random(),
                allergyNote = "유당 포함",
                confidence = 0.95f
            )
            val banner = ResultFormatter.toBanner(info)

            _ui.update {
                it.copy(
                    banner = banner,
                    guiding = false,
                    guideDirection = null
                )
            }

            // 장바구니에서 제거
            cart?.remove(info.id)

            // 다음 타겟으로 진행
            proceedToNextCartTarget()

            // (선택) 음성 안내
            speak(ResultFormatter.toVoice(info).text)
        }
    }

    private fun proceedToNextCartTarget() {
        val q = _ui.value.cartGuideQueue
        if (q.isEmpty()) {
            _ui.update { it.copy(cartGuideTargetName = null, showCartGuideModal = false) }
            return
        }
        val rest = q.drop(1)
        val next = rest.firstOrNull()
        _ui.update {
            it.copy(
                cartGuideQueue = rest,
                cartGuideTargetName = next,
                showCartGuideModal = (next != null)
            )
        }
    }

    /** 사진 캡처 (Stub) — 실제론 CameraX ImageCapture로 교체 예정 */
    private fun captureFrame(index: Int) {
        val placeholder = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        _ui.update { it.copy(capturedFrames = it.capturedFrames + placeholder) }
        println("📷 ${index + 1}번째 사진 촬영됨 (placeholder)")
    }

    fun clearCapturedFrames() {
        _ui.update { it.copy(capturedFrames = emptyList()) }
    }

    /** (옵션) 단일 감지 배너 — 기존 더미 로직 (필요하면 유지) */
    fun onDetected(result: DetectResult) {
        val banner = ResultFormatter.toBanner(result)
        _ui.update { it.copy(current = result, banner = banner) }

        if (result.id != lastSpokenId) {
            speak(ResultFormatter.toVoice(result).text)
            lastSpokenId = result.id
        }
    }

    fun clearBanner() {
        _ui.update { it.copy(banner = null) }
    }


    fun debugShowBannerSample() {
        _ui.update {
            it.copy(
                banner = ResultFormatter.Banner(
                    type = ResultFormatter.Banner.Type.INFO,
                    text = "먹태깡 청양마요 맛 | 1,700원 | 2+1 행사품입니다."
                )
            )
        }
    }

    /** 장바구니 여부와 무관하게 모달만 강제로 띄우기 */
    fun debugShowCartGuideModalSample(name: String = "코카콜라 제로 500ml") {
        _ui.update {
            it.copy(
                cartGuideTargetName = name,
                showCartGuideModal = true
            )
        }
    }
}
