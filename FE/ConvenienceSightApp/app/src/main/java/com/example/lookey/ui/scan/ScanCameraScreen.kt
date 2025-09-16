package com.example.lookey.ui.scan

import androidx.camera.core.CameraSelector
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lookey.BuildConfig              // ★ 추가
import com.example.lookey.core.platform.tts.TtsController
import com.example.lookey.ui.cart.CartPortFromViewModel
import com.example.lookey.ui.components.*
import com.example.lookey.ui.viewmodel.CartViewModel
import com.example.lookey.ui.viewmodel.ScanViewModel
import com.example.lookey.ui.viewmodel.ScanViewModel.Mode
import kotlin.math.max



import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt





@Composable
fun ScanCameraScreen(
    back: () -> Unit
) {
    // ----- TTS 준비 -----
    val context = LocalContext.current
    val tts = remember { TtsController(context) }
    DisposableEffect(Unit) { onDispose { tts.shutdown() } }

    // ----- Cart 포트 준비 -----
    val cartVm: CartViewModel = viewModel()
    val cartPort = remember(cartVm) { CartPortFromViewModel(cartVm) }

    // ----- ScanViewModel: Factory로 의존성 주입 -----
    val scanVm: ScanViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ScanViewModel(
                speak = tts::speak,
                cart = cartPort
            ) as T
        }
    })

    val ui by scanVm.ui.collectAsState()

    // 레이아웃 스펙
    val CAM_WIDTH = 320.dp
    val CAM_HEIGHT = 630.dp
    val CAM_TOP = 16.dp
    val MIC_SIZE = 72
    val MIC_RISE = 32.dp
    val PILL_BOTTOM_INSET = 75.dp
    val micCenterOffsetY = CAM_TOP + CAM_HEIGHT - (MIC_SIZE / 2).dp - MIC_RISE

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
            .background(Color.White)
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
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            // 상단 배너
            ui.banner?.let { b ->
                Box(Modifier.align(Alignment.TopCenter)) {
                    BannerMessage(
                        banner = b,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 20.dp)
                    )
                }
            }

            // 장바구니 안내 여부 모달
            if (ui.showCartGuideModal && ui.cartGuideTargetName != null) {
                Box(Modifier.align(Alignment.TopCenter)) {
                    ConfirmModal(
                        text = "\"${ui.cartGuideTargetName}\" 장바구니에 있습니다. 이걸로 안내할까요?",
                        yesText = "예",
                        noText = "아니요",
                        onYes = scanVm::onCartGuideConfirm,
                        onNo = scanVm::onCartGuideSkip,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 20.dp)
                    )
                }
            }

            // ★ 디버그 전용 미니 패널: 배너/모달 강제 표시
            if (BuildConfig.DEBUG) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(8.dp)
                ) {
                    DebugPanel(
                        onShowBanner = { scanVm.debugShowBannerSample() },
                        onShowModal  = { scanVm.debugShowCartGuideModalSample() }
                    )
                }
            }

            // FeaturePill — 스캔 중엔 “상품 탐색 중”
            if (ui.mode == Mode.SCAN) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(bottom = PILL_BOTTOM_INSET),
                    contentAlignment = Alignment.Center
                ) {
                    val pillText =
                        if (ui.scanning || ui.capturing) "상품 탐색 중" else "상품 탐색 시작"
                    FeaturePill(
                        text = pillText,
                        onClick = { if (!ui.scanning && !ui.capturing) scanVm.startPanorama() },
                        modifier = Modifier.width(CAM_WIDTH * 2 / 3)
                    )
                }
            }
        }

        MicActionButton(
            onClick = { /* TODO: 음성 인식 */ },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = micCenterOffsetY),
            sizeDp = MIC_SIZE
        )

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



// Compose 1.6+ 에서는 change.consume(), 1.5.x 에서는 consumeAllChanges().
// 둘 다 커버하는 작은 호환 함수
private fun PointerInputChange.consumePositionCompat() {
    try {
        // 1.6+
        this.consume()
    } catch (_: Throwable) {
        // 1.5.x
        this.consumeAllChanges()
    }
}

@Composable
private fun DebugPanel(
    onShowBanner: () -> Unit,
    onShowModal: () -> Unit
) {
    var offset by remember { mutableStateOf(Offset.Zero) }

    Surface(
        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.28f),
        contentColor = MaterialTheme.colorScheme.onSecondary,
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 0.dp,
        modifier = Modifier
            .offset { IntOffset(offset.x.roundToInt(), offset.y.roundToInt()) }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consumePositionCompat()
                        offset += dragAmount
                    }
                )
            }
    ) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Text("DEBUG", style = MaterialTheme.typography.labelSmall)
            Spacer(Modifier.height(4.dp))
            TextButton(onClick = onShowBanner) { Text("배너 샘플") }
            TextButton(onClick = onShowModal) { Text("모달 샘플") }
        }
    }
}