// app/src/main/java/com/example/lookey/ui/viewmodel/ScanViewModel.kt
package com.example.lookey.ui.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lookey.domain.entity.DetectResult
import com.example.lookey.ui.cart.CartPort
import com.example.lookey.ui.scan.ResultFormatter
import com.example.lookey.data.network.Repository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

class ScanViewModel(
    private val speak: (String) -> Unit = {},
    private val cart: CartPort? = null,
    private val repoNet: Repository = Repository(),
    private val cacheDir: File,
    /** 현재 화면 프레임 공급자(PreviewView.bitmap 등). 없으면 006은 스텁 */
    private val frameProvider: (() -> Bitmap?)? = null
) : ViewModel() {

    enum class Mode { SCAN, GUIDE }

    /** 9방향 버킷 */
    enum class DirectionBucket(val label: String) {
        LEFT_UP("왼쪽 위"), UP("위"), RIGHT_UP("오른쪽 위"),
        LEFT("왼쪽"), CENTER("가운데"), RIGHT("오른쪽"),
        LEFT_DOWN("왼쪽 아래"), DOWN("아래"), RIGHT_DOWN("오른쪽 아래")
    }

    data class UiState(
        val mode: Mode = Mode.SCAN,
        val scanning: Boolean = false,
        val capturing: Boolean = false,
        val current: DetectResult? = null,
        val banner: ResultFormatter.Banner? = null,

        // 005 파노라마 캡처
        val capturedFrames: List<Bitmap> = emptyList(),

        // 장바구니 순차 안내
        val cartGuideQueue: List<String> = emptyList(),
        val cartGuideTargetName: String? = null,
        val showCartGuideModal: Boolean = false,

        // 006 위치 안내
        val guiding: Boolean = false,
        val guideDirection: DirectionBucket? = null,

        // 길 안내 표시용
        val navSummary: String? = null,
        val navActions: List<String> = emptyList()
    )

    private val _ui = MutableStateFlow(UiState())
    val ui: StateFlow<UiState> = _ui

    private var lastSpokenId: String? = null
    private var guideJob: Job? = null
    private var lastNavHint: String? = null

    fun setMode(mode: Mode) {
        _ui.update {
            it.copy(
                mode = mode,
                scanning = if (mode == Mode.SCAN) it.scanning else false,
                capturing = false
            )
        }
        if (mode == Mode.GUIDE) startGuideLoop() else stopGuideLoop()
    }



    /** NAV-001: 1초 폴링 루프 */
    private fun startGuideLoop() {
        if (guideJob?.isActive == true) return
        guideJob = viewModelScope.launch {
            speak("길 안내를 시작합니다. 카메라를 천천히 움직여 주세요.")
            while (isActive && _ui.value.mode == Mode.GUIDE) {
                val frame = frameProvider?.invoke()
                if (frame != null) {
                    val res = runCatching { repoNet.navGuide(cacheDir, frame) }.getOrNull()
                    val result = res?.result
                    val hint = result?.ttsHint ?: result?.summary
                    // 화면 갱신
                    _ui.update {
                        it.copy(
                            navSummary = result?.summary,
                            navActions = result?.actions ?: emptyList()
                        )
                    }
                    // 바뀌었을 때만 읽기
                    if (!hint.isNullOrBlank() && hint != lastNavHint) {
                        speak(hint)
                        lastNavHint = hint
                    }
                }
                delay(1000)
            }
        }
    }

    private fun stopGuideLoop() {
        guideJob?.cancel()
        lastNavHint = null
        _ui.update { it.copy(navSummary = null, navActions = emptyList()) }
    }


    // 005: 1장 캡처 → 서버 호출 → 결과 반영
    fun startPanorama() {
        if (_ui.value.mode != Mode.SCAN) return

        viewModelScope.launch {
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

            val frame = frameProvider?.invoke()
            if (frame == null) {
                _ui.update { it.copy(capturing = false, scanning = false) }
                return@launch
            }

            val res = runCatching { repoNet.productShelfSearch(cacheDir, frame) }.getOrNull()

            // 3초 뒤 일반 모드로 복귀 (UI 연출)
            kotlinx.coroutines.delay(3000)
            _ui.update { it.copy(capturing = false, scanning = false) }

            res?.let {
                val matched = it.result.matchedNames.orEmpty()
                val next = matched.firstOrNull()
                _ui.update { s ->
                    s.copy(
                        banner = ResultFormatter.Banner(
                            type = ResultFormatter.Banner.Type.SUCCESS,
                            text = "상품 인식이 종료되었습니다."
                        ),
                        cartGuideQueue = matched,
                        cartGuideTargetName = next,
                        showCartGuideModal = (next != null)
                    )
                }
            } ?: kotlin.run {
                println("PRODUCT-005 failed or null response")
            }
        }
    }


    /** 모달: “예” → 006 시작 */
    fun onCartGuideConfirm() {
        val target = _ui.value.cartGuideTargetName ?: return
        _ui.update { it.copy(showCartGuideModal = false, guiding = true, guideDirection = null) }
        start006Loop(target)
    }

    /** 모달: “아니요” → 다음 타겟 */
    fun onCartGuideSkip() {
        proceedToNextCartTarget()
    }

    /** 006: 프레임 전송 폴링 → 방향 or 단일 인식 */
    private fun start006Loop(targetName: String) {
        viewModelScope.launch {
            if (frameProvider == null) return@launch start006StubOnce(targetName)

            repeat(4) {
                val frame = frameProvider.invoke() ?: return@repeat
                val res = runCatching {
                    repoNet.productLocation(cacheDir, frame, targetName) // ✅ 실제 호출
                }.getOrNull()

                when (res?.result?.caseType) {                          // ✅ dto의 필드명에 맞춤
                    "DIRECTION" -> {
                        val dir = res.result.target?.directionBucket?.toDirectionBucketOrNull()
                        _ui.update { it.copy(guideDirection = dir) }
                        if (dir != null) speak("$targetName 이(가) ${dir.label}에 있습니다.")
                        delay(800)
                    }
                    "SINGLE_RECOGNIZED" -> {
                        val info = res.result.info
                        val banner = ResultFormatter.toBanner(
                            DetectResult(
                                id = info?.name ?: targetName,
                                name = info?.name ?: targetName,
                                price = info?.price,
                                promo = info?.event,
                                hasAllergy = info?.allergy == true,
                                allergyNote = if (info?.allergy == true) "알레르기 주의" else null,
                                confidence = 0.95f
                            )
                        )
                        _ui.update { it.copy(banner = banner, guiding = false, guideDirection = null) }
                        cart?.remove(info?.name ?: targetName)
                        proceedToNextCartTarget()
                        speak(ResultFormatter.toVoice(
                            DetectResult(
                                id = info?.name ?: targetName,
                                name = info?.name ?: targetName,
                                price = info?.price,
                                promo = info?.event,
                                hasAllergy = info?.allergy == true,
                                allergyNote = if (info?.allergy == true) "알레르기 주의" else null,
                                confidence = 0.95f
                            )
                        ).text)
                        return@launch
                    }
                    else -> delay(600)
                }
            }
            _ui.update { it.copy(guiding = false, guideDirection = null) }
        }

}

    /** (프레임 공급자 없을 때) 스텁 1회 */
    private fun start006StubOnce(targetName: String) {
        viewModelScope.launch {
            val dir = DirectionBucket.values().random()
            _ui.update { it.copy(guideDirection = dir) }
            speak("$targetName 이(가) ${dir.label}에 있습니다.")
            delay(500)
            val info = DetectResult(
                id = targetName, name = targetName,
                price = listOf(1500,1700,2000,2200,2500).random(),
                promo = listOf("1+1", "2+1", null).random(),
                hasAllergy = listOf(true,false).random(),
                allergyNote = "유당 포함", confidence = 0.95f
            )
            _ui.update { it.copy(banner = ResultFormatter.toBanner(info), guiding = false, guideDirection = null) }
            cart?.remove(info.id)
            proceedToNextCartTarget()
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

    /** 임시 캡처(placeholder) */
    private fun captureFrame(index: Int) {
        val placeholder = Bitmap.createBitmap(800, 600, Bitmap.Config.ARGB_8888)
        _ui.update { it.copy(capturedFrames = it.capturedFrames + placeholder) }
    }



    fun clearCapturedFrames() { _ui.update { it.copy(capturedFrames = emptyList()) } }

    fun onDetected(result: DetectResult) {
        val banner = ResultFormatter.toBanner(result)
        _ui.update { it.copy(current = result, banner = banner) }
        if (result.id != lastSpokenId) {
            speak(ResultFormatter.toVoice(result).text)
            lastSpokenId = result.id
        }
    }

    fun clearBanner() { _ui.update { it.copy(banner = null) } }

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

    fun debugShowCartGuideModalSample(name: String = "코카콜라 제로 500ml") {
        _ui.update { it.copy(cartGuideTargetName = name, showCartGuideModal = true) }
    }

    // === util ===
    private fun String.toDirectionBucketOrNull(): DirectionBucket? = when (this) {
        "왼쪽위" -> DirectionBucket.LEFT_UP
        "위" -> DirectionBucket.UP
        "오른쪽위" -> DirectionBucket.RIGHT_UP
        "왼쪽" -> DirectionBucket.LEFT
        "가운데", "중간" -> DirectionBucket.CENTER
        "오른쪽" -> DirectionBucket.RIGHT
        "왼쪽아래" -> DirectionBucket.LEFT_DOWN
        "아래" -> DirectionBucket.DOWN
        "오른쪽아래" -> DirectionBucket.RIGHT_DOWN
        else -> null
    }
}
