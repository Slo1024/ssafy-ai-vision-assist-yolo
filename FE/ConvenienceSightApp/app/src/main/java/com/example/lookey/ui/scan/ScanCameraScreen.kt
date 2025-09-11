package com.example.lookey.ui.scan

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lookey.ui.scan.overlay.GridOverlay
import com.example.lookey.ui.viewmodel.CameraViewModel

@Composable
fun ScanCameraScreen(
    back: () -> Unit,
    vm: CameraViewModel = viewModel()
) {
    val ctx = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val ui by vm.ui.collectAsState()

    // 권한 런처
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { /* 필요시 granted map 처리 */ }

    // CameraX 프리뷰 준비
    val preview = remember { Preview.Builder().build() }
    val previewView = remember { PreviewView(ctx) }

    LaunchedEffect(Unit) {
        // 카메라/오디오 권한 요청
        permissionLauncher.launch(arrayOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.RECORD_AUDIO
        ))

        // 미리보기 바인딩
        val providerFuture = ProcessCameraProvider.getInstance(ctx)
        providerFuture.addListener({
            val provider = providerFuture.get()
            try {
                val selector = androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA
                preview.setSurfaceProvider(previewView.surfaceProvider)
                provider.unbindAll()
                provider.bindToLifecycle(lifecycleOwner, selector, preview)
            } catch (e: Exception) {
                Log.e("Camera", "bind failed", e)
            }
        }, ContextCompat.getMainExecutor(ctx))
    }

    Scaffold(
        bottomBar = {
            BottomControls(
                leftText = "길 안내",
                rightText = "상품 인식",
                onLeft = back,
                onRight = { /* 이후 상세/리스트로 전환 연결 예정 */ }
            )
        }
    ) { pad ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .background(Color.Black)
        ) {
            // 카메라 프리뷰
            AndroidView(
                factory = { previewView },
                modifier = Modifier.fillMaxSize()
            )

            // 격자 오버레이
            GridOverlay(modifier = Modifier.fillMaxSize())

            // 중앙 상태 칩
            AssistChip(
                onClick = { vm.toggleScan() },
                label = {
                    Text(
                        if (ui.isScanning) "상품 탐색중" else "상품 탐색 시작",
                        fontWeight = FontWeight.Bold
                    )
                },
                modifier = Modifier
                    .align(Alignment.Center)
                    .semantics { contentDescription = if (ui.isScanning) "상품 탐색 중" else "상품 탐색 시작" }
            )

            // 마이크 원형 버튼 (FAB)
            FloatingActionButton(
                onClick = { /* 음성 인식 연결 예정 */ },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 84.dp) // 하단바 위로 띄우기
                    .size(64.dp)
                    .semantics { contentDescription = "음성 인식" }
            ) {
                Text("🎤")
            }
        }
    }
}

@Composable
private fun BottomControls(
    leftText: String,
    rightText: String,
    onLeft: () -> Unit,
    onRight: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onLeft,
            modifier = Modifier
                .weight(1f)
                .height(56.dp)
                .semantics { contentDescription = "$leftText 버튼" }
        ) { Text(leftText) }
        Button(
            onClick = onRight,
            modifier = Modifier
                .weight(1f)
                .height(56.dp)
                .semantics { contentDescription = "$rightText 버튼" }
        ) { Text(rightText) }
    }
}
