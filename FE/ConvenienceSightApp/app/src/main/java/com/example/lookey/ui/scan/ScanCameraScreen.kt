// app/src/main/java/com/example/lookey/ui/scan/ScanCameraScreen.kt
package com.example.lookey.ui.scan

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lookey.domain.entity.DetectResult
import com.example.lookey.ui.components.*
import com.example.lookey.ui.viewmodel.ScanViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.random.Random

@Composable
fun ScanCameraScreen(
    back: () -> Unit,
    vm: ScanViewModel = viewModel()
) {
    val ui by vm.ui.collectAsState()
    var isGuideMode by remember { mutableStateOf(false) }

    // 피그마/화면 스펙
    val CAM_WIDTH = 320.dp
    val CAM_HEIGHT = 630.dp
    val CAM_TOP = 16.dp

    val MIC_SIZE = 72                 // 마이크 지름(dp)
    val MIC_RISE = 32.dp              // 하단 경계선에서 더 위로 올리는 양
    val PILL_BOTTOM_INSET = 75.dp     // Pill이 카메라 내부 하단 경계선과 겹치지 않도록 띄우는 값

    // 화면 루트(TopCenter) 기준 y 오프셋:
    // 카메라 상단 여백 + 카메라 높이 - (마이크 반지름) - (추가 상승량)
    val micCenterOffsetY = CAM_TOP + CAM_HEIGHT - (MIC_SIZE / 2).dp - MIC_RISE

    // 더미 인식 루프 (스캔 중일 때 2초마다 임의 감지)
    LaunchedEffect(ui.scanning) {
        if (ui.scanning) {
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
        // 카메라 + 격자 + 오버레이(Pill + 배너 + 모달)
        CameraPreviewBox(
            width = CAM_WIDTH,
            height = CAM_HEIGHT,
            topPadding = CAM_TOP,
            corner = 12.dp,
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            // ✅ FeaturePill: 3열 "가운데" 영역 중앙
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = PILL_BOTTOM_INSET),
                contentAlignment = Alignment.Center
            ) {
                FeaturePill(
                    text = if (ui.scanning) "상품 탐색 중" else "상품 탐색 시작",
                    onClick = { vm.toggleScan() },
                    modifier = Modifier.width(CAM_WIDTH * 2 / 3)
                )
            }

            // ✅ 상단 배너 (카메라 박스 안에서만 보임)
            ui.banner?.let { b ->
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(12.dp)
                ) {
                    BannerMessage(banner = b, onDismiss = { vm.clearBanner() })
                }
            }

            // ✅ ConfirmModal (카메라 박스 안에서만 보임)
            val cartTarget = ui.cartTarget
            if (ui.showCartModal && cartTarget != null) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { vm.onCartModalDismiss() }
                )
                // 모달 박스
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .padding(horizontal = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    ConfirmModal(
                        text = "\"${cartTarget.name}\"가 장바구니에 있습니다. 제거할까요?",
                        yesText = "예",
                        noText = "아니요",
                        onYes = { vm.onCartRemoveConfirm() },
                        onNo = { vm.onCartModalDismiss() }
                    )
                }
            }
        }

        // 🎙 마이크 버튼: 카메라 하단 테두리에 겹치게 중앙 배치
        MicActionButton(
            onClick = { /* TODO: 음성 인식 */ },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = micCenterOffsetY),
            sizeDp = MIC_SIZE
        )

        // ⬜ 하단 토글: 카메라 폭보다 살짝 좁게
        TwoOptionToggle(
            leftText = "길 안내",
            rightText = "상품 인식",
            selectedLeft = isGuideMode,
            onLeft = { isGuideMode = true /* TODO: Guide 모드 전환 */ },
            onRight = { isGuideMode = false /* TODO: Scan 모드 전환 */ },
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
