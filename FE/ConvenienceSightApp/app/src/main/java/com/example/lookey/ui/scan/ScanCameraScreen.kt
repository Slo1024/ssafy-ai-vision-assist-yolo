package com.example.lookey.ui.scan

import androidx.camera.core.CameraSelector
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lookey.domain.entity.DetectResult
import com.example.lookey.ui.components.*
import com.example.lookey.ui.viewmodel.ScanViewModel
import com.example.lookey.ui.viewmodel.ScanViewModel.Mode
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.max
import kotlin.random.Random

@Composable
fun ScanCameraScreen(
    back: () -> Unit,
    vm: ScanViewModel = viewModel()
) {
    val ui by vm.ui.collectAsState()

    val CAM_WIDTH = 320.dp
    val CAM_HEIGHT = 630.dp
    val CAM_TOP = 16.dp
    val MIC_SIZE = 72
    val MIC_RISE = 32.dp
    val PILL_BOTTOM_INSET = 75.dp

    val micCenterOffsetY = CAM_TOP + CAM_HEIGHT - (MIC_SIZE / 2).dp - MIC_RISE

    var minZoom by remember { mutableStateOf(1.0f) }
    var maxZoom by remember { mutableStateOf(1.0f) }

    val requestedZoom = remember(ui.mode, ui.scanning, ui.capturing) {
        if (ui.mode == Mode.SCAN && (ui.scanning || ui.capturing)) 0.5f else 1.0f
    }
    val effectiveZoom = max(requestedZoom, minZoom)

    // (선택) 더미 감지 루프 — 필요 없으면 제거
    LaunchedEffect(ui.mode, ui.scanning) {
        if (ui.mode == Mode.SCAN && ui.scanning) {
            while (isActive && ui.scanning) {
                delay(2000)
                vm.onDetected(
                    DetectResult(
                        id = listOf("coke", "pepsi", "latte").random(),
                        name = "코카콜라 제로 500ml",
                        price = 2200,
                        promo = "1+1",
                        hasAllergy = Random.nextBoolean(),
                        allergyNote = "유당 포함",
                        confidence = 0.92f
                    )
                )
            }
        }
    }

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

            // 장바구니 안내 여부 모달 (큐 기반)
            if (ui.showCartGuideModal && ui.cartGuideTargetName != null) {
                Box(Modifier.align(Alignment.TopCenter)) {
                    ConfirmModal(
                        text = "\"${ui.cartGuideTargetName}\" 장바구니에 있습니다. 안내할까요?",
                        yesText = "예",
                        noText = "아니요",
                        onYes = vm::onCartGuideConfirm,
                        onNo = vm::onCartGuideSkip,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 20.dp)
                    )
                }
            }

            // FeaturePill — 스캔 중엔 항상 "상품 탐색 중"
            if (ui.mode == Mode.SCAN) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(bottom = PILL_BOTTOM_INSET),
                    contentAlignment = Alignment.Center
                ) {
                    val pillText = if (ui.scanning || ui.capturing) "상품 탐색 중" else "상품 탐색 시작"
                    FeaturePill(
                        text = pillText,
                        onClick = { if (!ui.scanning && !ui.capturing) vm.startPanorama() },
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
            onLeft = { vm.setMode(Mode.GUIDE) },
            onRight = { vm.setMode(Mode.SCAN) },
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
