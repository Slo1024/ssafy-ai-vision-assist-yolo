// app/src/main/java/com/example/lookey/ui/viewmodel/ScanViewModel.kt
package com.example.lookey.ui.viewmodel

import android.graphics.Bitmap
import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lookey.domain.entity.DetectResult
import com.example.lookey.ui.cart.CartPort
import com.example.lookey.ui.scan.ResultFormatter
import com.example.lookey.data.network.Repository
import com.example.lookey.data.remote.dto.navigation.VisionAnalyzeResponse
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

    /** 9방향 버킷 (006용 읽어주기 문구) */
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

        // 005
        val capturedFrames: List<Bitmap> = emptyList(),

        // 장바구니 순차 안내
        val cartGuideQueue: List<String> = emptyList(),
        val cartGuideTargetName: String? = null,
        val showCartGuideModal: Boolean = false,

        // 006
        val guiding: Boolean = false,
        val guideDirection: DirectionBucket? = null,

        // NAV-001 (길 안내)
        val navSummary: String? = null,
        val navActions: List<String> = emptyList()
    )

    private val _ui = MutableStateFlow(UiState())
    val ui: StateFlow<UiState> = _ui

    private var lastSpokenId: String? = null
    private var guideJob: Job? = null
    private var lastNavHint: String? = null

    // 006 API 호출 지연용(TTS가 끝났다고 가정 후 1.2초 쿨다운)
    private var ttsCooldownUntilMs: Long = 0L

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

    // ----------------------------------------
    // NAV-001: 1초 폴링 루프 (새 스펙 data 매핑)
    // ----------------------------------------
    private fun startGuideLoop() {
        if (guideJob?.isActive == true) return
        guideJob = viewModelScope.launch {
            speak("길 안내를 시작합니다. 카메라를 천천히 움직여 주세요.")
            while (isActive && _ui.value.mode == Mode.GUIDE) {
                val frame = frameProvider?.invoke()
                if (frame != null) {
                    val resp = runCatching { repoNet.navGuide(cacheDir, frame) }.getOrNull()
                    val ui = resp?.toNavUi()

                    _ui.update {
                        it.copy(
                            navSummary = ui?.summary,
                            navActions = ui?.actions ?: emptyList()
                        )
                    }

                    val hint = ui?.ttsHint
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

    /** NAV 응답 → UI용 요약/액션/음성 힌트 매핑 */
    private data class NavUi(val summary: String?, val actions: List<String>, val ttsHint: String?)

    private fun VisionAnalyzeResponse.toNavUi(): NavUi? {
        val d = data ?: return NavUi(null, emptyList(), null)

        // 이동 가능 방향
        val goList = buildList {
            if (d.directions.left) add("왼쪽")
            if (d.directions.front) add("정면")
            if (d.directions.right) add("오른쪽")
        }
        val goSummary = if (goList.isEmpty()) "이동 가능한 방향이 없습니다."
        else "이동 가능: ${goList.joinToString(", ")}"

        fun tri(label: String, l: Boolean, f: Boolean, r: Boolean): String? {
            val where = buildList {
                if (l) add("왼쪽")
                if (f) add("정면")
                if (r) add("오른쪽")
            }
            return if (where.isEmpty()) null else "$label: ${where.joinToString(", ")}"
        }

        val peopleMsg = tri("사람 감지", d.people.left, d.people.front, d.people.right)
        val obsMsg    = tri("장애물", d.obstacles.left, d.obstacles.front, d.obstacles.right)

        val actions = buildList {
            if (d.directions.left) add("왼쪽으로 이동")
            if (d.directions.front) add("앞으로 이동")
            if (d.directions.right) add("오른쪽으로 이동")
            if (d.counter) add("계산대 방향")
            if (!d.category.isNullOrBlank()) add("현재 구역: ${d.category}")
            if (peopleMsg != null) add(peopleMsg)
            if (obsMsg != null) add(obsMsg)
        }

        val caution = when {
            d.people.front || d.obstacles.front -> "정면 주의"
            else -> null
        }
        val goTts = when {
            d.directions.front -> "앞으로 이동 가능합니다"
            d.directions.right -> "오른쪽으로 이동 가능합니다"
            d.directions.left  -> "왼쪽으로 이동 가능합니다"
            else               -> "이동 가능한 방향이 없습니다"
        }
        val tts = listOfNotNull(caution, goTts).joinToString(". ")

        val summary = listOfNotNull(goSummary, if (d.counter) "계산대 감지" else null).joinToString(" | ")
        return NavUi(summary = summary, actions = actions, ttsHint = tts)
    }

    // ----------------------------------------
    // PRODUCT-005: 1장 업로드 → 서버 호출 → 큐/모달
    // ----------------------------------------
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

            // UI 연출: 광각 → 일반 복귀
            delay(3000)
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
            } ?: run {
                println("PRODUCT-005 failed or null response")
            }
        }
    }

    // ----------------------------------------
    // PRODUCT-006: 상대 위치 → 단일 인식
    //  - 음성 안내가 나갈 때는 API 호출 금지 (TTS 후 1.2초 대기)
    // ----------------------------------------
    fun onCartGuideConfirm() {
        val target = _ui.value.cartGuideTargetName ?: return
        _ui.update { it.copy(showCartGuideModal = false, guiding = true, guideDirection = null) }
        start006Loop(target)
    }

    fun onCartGuideSkip() {
        proceedToNextCartTarget()
    }

    private fun start006Loop(targetName: String) {
        viewModelScope.launch {
            if (frameProvider == null) return@launch start006StubOnce(targetName)

            repeat(4) {
                // 🔒 TTS 쿨다운 동안은 호출 지연
                val now = SystemClock.elapsedRealtime()
                if (now < ttsCooldownUntilMs) {
                    delay(ttsCooldownUntilMs - now + 50)
                }

                val frame = frameProvider.invoke() ?: return@repeat
                val res = runCatching {
                    repoNet.productLocation(cacheDir, frame, targetName)
                }.getOrNull()

                when (res?.result?.caseType) {
                    "DIRECTION" -> {
                        val dir = res.result.target?.directionBucket?.toDirectionBucketOrNull()
                        _ui.update { it.copy(guideDirection = dir) }
                        if (dir != null) {
                            speak("$targetName 이(가) ${dir.label}에 있습니다.")
                            // 🕒 안내 음성 후 1.2초 동안 추가 호출 금지
                            ttsCooldownUntilMs = SystemClock.elapsedRealtime() + 1200L
                        }
                        delay(200) // 살짝 텀
                    }
                    "SINGLE_RECOGNIZED" -> {
                        val info = res.result.info
                        val det = DetectResult(
                            id = info?.name ?: targetName,
                            name = info?.name ?: targetName,
                            price = info?.price,
                            promo = info?.event,
                            hasAllergy = info?.allergy == true,
                            allergyNote = if (info?.allergy == true) "알레르기 주의" else null,
                            confidence = 0.95f
                        )
                        val banner = ResultFormatter.toBanner(det)
                        _ui.update { it.copy(banner = banner, guiding = false, guideDirection = null) }
                        cart?.remove(CartLine(name = det.name))   // CartLine 생성자 필드명은 프로젝트 정의에 맞춰 주세요
                        proceedToNextCartTarget()
                        speak(ResultFormatter.toVoice(det).text)
                        return@launch
                    }
                    else -> {
                        // 서버에서 아직 못 찾음 → 잠시 후 재시도
                        delay(600)
                    }
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
                price = listOf(1500, 1700, 2000, 2200, 2500).random(),
                promo = listOf("1+1", "2+1", null).random(),
                hasAllergy = listOf(true, false).random(),
                allergyNote = "유당 포함", confidence = 0.95f
            )
            _ui.update { it.copy(banner = ResultFormatter.toBanner(info), guiding = false, guideDirection = null) }
            cart?.remove(CartLine(name = info.name))
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

    /** 임시 캡처(placeholder) — 필요 시 테스트용으로 사용 */
    private fun captureFrame(@Suppress("UNUSED_PARAMETER") index: Int) {
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
