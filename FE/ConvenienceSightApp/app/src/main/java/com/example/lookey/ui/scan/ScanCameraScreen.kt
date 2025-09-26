// app/src/main/java/com/example/lookey/ui/scan/ScanCameraScreen.kt
package com.example.lookey.ui.scan

import android.view.accessibility.AccessibilityManager
import androidx.camera.core.CameraSelector
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lookey.BuildConfig
import com.example.lookey.core.platform.tts.TtsController
import com.example.lookey.ui.cart.CartPortFromViewModel
import com.example.lookey.ui.components.*
import com.example.lookey.ui.viewmodel.CartViewModel
import com.example.lookey.ui.viewmodel.ScanViewModel
import com.example.lookey.ui.viewmodel.ScanViewModel.Mode
import kotlin.math.max
import kotlin.math.roundToInt
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusTarget
import androidx.camera.view.PreviewView
import android.graphics.Bitmap
import com.example.lookey.data.network.Repository
import com.example.lookey.data.network.CartRepository
import com.example.lookey.data.network.RetrofitClient
import com.example.lookey.ui.viewmodel.CartViewModelFactory


@Composable
fun ScanCameraScreen(
    back: () -> Unit
) {
    // ----- TTS 준비 -----
    val context = LocalContext.current
    val tts = remember { TtsController(context) }
    DisposableEffect(Unit) { onDispose { tts.shutdown() } }

    // ----- 접근성 상태 -----
    val view = LocalView.current
    val am = context.getSystemService(AccessibilityManager::class.java)
    val screenReaderOn = remember(am) {
        (am?.isEnabled == true) && (am?.isTouchExplorationEnabled == true)
    }

    // ----- Cart 포트 -----
    val cartVm: CartViewModel = viewModel(
        factory = remember {
            // CartRepository 생성자에 맞춰 한 줄 선택해서 쓰세요.
            CartViewModelFactory(CartRepository(RetrofitClient.apiService))
            // CartViewModelFactory(CartRepository(RetrofitClient.apiService))
        }
    )
    val cartPort = remember(cartVm) { CartPortFromViewModel(cartVm) }


    // 프리뷰 참조 저장
    var previewRef by remember { mutableStateOf<PreviewView?>(null) }



    // ----- ScanViewModel DI -----
    val scanVm: ScanViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val safeSpeak: (String) -> Unit = if (screenReaderOn) ({ _ -> }) else tts::speak
            // 팩토리 안에서
            val cacheDir = context.cacheDir
            val provider = { previewRef?.bitmap }

            return ScanViewModel(
                speak = safeSpeak,
                cart = cartPort,
                repoNet = Repository(),
                cacheDir = cacheDir,
                frameProvider = provider
            ) as T

        }
    })
    val ui by scanVm.ui.collectAsState()

    // ----- 포커스 요청자 (배너/모달) -----
    val bannerFocus = remember { FocusRequester() }
    val modalFocus  = remember { FocusRequester() }

    // 배너/모달 등장 시 자동 낭독
    LaunchedEffect(ui.banner?.text) {
        ui.banner?.text?.let { text ->
            bannerFocus.requestFocus()
            view.announceForAccessibility(text)
        }
    }
    LaunchedEffect(ui.showCartGuideModal, ui.cartGuideTargetName) {
        if (ui.showCartGuideModal && ui.cartGuideTargetName != null) {
            val msg = "\"${ui.cartGuideTargetName}\" 장바구니에 있습니다. 이걸로 안내할까요?"
            modalFocus.requestFocus()
            view.announceForAccessibility(msg)
        }
    }

    // ----- 레이아웃 스펙 -----
    val CAM_WIDTH = 320.dp
    val CAM_HEIGHT = 630.dp
    val CAM_TOP = 16.dp


    // 줌 capability
    var minZoom by remember { mutableStateOf(1.0f) }
    var maxZoom by remember { mutableStateOf(1.0f) }
    val requestedZoom = remember(ui.mode, ui.scanning, ui.capturing) {
        if (ui.mode == Mode.SCAN && (ui.scanning || ui.capturing)) 0.5f else 1.0f
    }
    val effectiveZoom = max(requestedZoom, minZoom)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        CameraPreviewBox(
            width = CAM_WIDTH,
            height = CAM_HEIGHT,
            topPadding = CAM_TOP,
            corner = 12.dp,
            cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA,
            zoomRatio = effectiveZoom,
            onZoomCapabilities = { min, max ->
                minZoom = min
                maxZoom = max
            },
            onPreviewReady = { previewRef = it },
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            // 배너
            ui.banner?.let { b ->
                Box(Modifier.align(Alignment.TopCenter)) {
                    BannerMessage(
                        banner = b,
                        modifier = Modifier
                            .width(CAM_WIDTH)
                            .padding(vertical = 20.dp)
                            .focusRequester(bannerFocus)
                            .focusTarget() // focusable 대신
                            .semantics { liveRegion = LiveRegionMode.Assertive }
                    )
                }
            }

            // 장바구니 안내 모달
            if (ui.showCartGuideModal && ui.cartGuideTargetName != null) {
                Box(Modifier.align(Alignment.TopCenter)) {
                    ConfirmModal(
                        text = "\"${ui.cartGuideTargetName}\" 장바구니에 있습니다. 이걸로 안내할까요?",
                        yesText = "예",
                        noText = "아니요",
                        onYes = scanVm::onCartGuideConfirm,
                        onNo = scanVm::onCartGuideSkip,
                        modifier = Modifier
                            .width(CAM_WIDTH)
                            .padding(vertical = 20.dp)
                            .focusRequester(modalFocus)
                            .focusTarget()
                            .semantics { liveRegion = LiveRegionMode.Assertive }
                    )
                }
            }

//            // 디버그 패널 (TalkBack 켜지면 숨김)
//            if (BuildConfig.DEBUG) {
//                Box(
//                    modifier = Modifier
//                        .align(Alignment.Center)
//                        .padding(8.dp)
//                ) {
//                    DebugPanel(
//                        onShowBanner = { scanVm.debugShowBannerSample() },
//                        onShowModal  = { scanVm.debugShowCartGuideModalSample() }
//                    )
//                }
//            }

            // === FeaturePill: 프리뷰 박스 "안" 하단 중앙 ===
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                if (ui.mode == Mode.SCAN) {
                    val pillText = if (ui.scanning || ui.capturing) "상품 탐색 중" else "상품 탐색 시작"
                    FeaturePill(
                        text = pillText,
                        onClick = { if (!ui.scanning && !ui.capturing) scanVm.startPanorama() },
                        modifier = Modifier.width(CAM_WIDTH * 2 / 3)
                    )
                } else { // Mode.GUIDE
                    val guideText = if (ui.navBusy) "길 안내 중" else "길 탐색"
                    FeaturePill(
                        text = guideText,
                        onClick = { if (!ui.navBusy) scanVm.navGuideOnce() }, // 처리 중엔 중복 클릭 방지
                        modifier = Modifier.width(CAM_WIDTH * 2 / 3)
                    )
                }
            }
        }

            TwoOptionToggle(
            leftText = "길 안내",
            rightText = "상품 인식",
            selectedLeft = ui.mode == Mode.GUIDE,
            onLeft = { scanVm.setMode(Mode.GUIDE) },
            onRight = { scanVm.setMode(Mode.SCAN) },
            height = 56.dp,
            elevation = 12.dp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .width(CAM_WIDTH - 60.dp)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(bottom = 8.dp)
        )
    }
}

// Compose 1.6+에서는 change.consume(), 1.5.x에선 consumeAllChanges()
// 둘 다 커버하는 작은 호환 함수
private fun PointerInputChange.consumePositionCompat() {
    try {
        this.consume()
    } catch (_: Throwable) {
        this.consumeAllChanges()
    }
}
//
//@Composable
//private fun DebugPanel(
//    onShowBanner: () -> Unit,
//    onShowModal: () -> Unit
//) {
//    var offset by remember { mutableStateOf(Offset.Zero) }
//
//    Surface(
//        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.28f),
//        contentColor = MaterialTheme.colorScheme.onSecondary,
//        shape = MaterialTheme.shapes.medium,
//        tonalElevation = 0.dp,
//        modifier = Modifier
//            .offset { IntOffset(offset.x.roundToInt(), offset.y.roundToInt()) }
//            .pointerInput(Unit) {
//                detectDragGestures(
//                    onDrag = { change, dragAmount ->
//                        change.consumePositionCompat()
//                        offset += dragAmount
//                    }
//                )
//            }
//    ) {
//        Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
//            Text("DEBUG", style = MaterialTheme.typography.labelSmall)
//            Spacer(Modifier.height(4.dp))
//            TextButton(onClick = onShowBanner) { Text("배너 샘플") }
//            TextButton(onClick = onShowModal) { Text("모달 샘플") }
//        }
//    }
//}
