package com.example.lookey.ui.components

import android.Manifest
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.lookey.ui.scan.overlay.GridOverlay

/**
 * 4:3 카메라 프리뷰 + GridOverlay + 오버레이 슬롯.
 * - 권한 요청/CameraX 바인딩 내부 처리
 * - zoomRatio로 0.5x/1.0x 등 전환 (기기 지원 범위에 맞춰 클램프)
 * - onZoomCapabilities로 기기 min/max 줌 전달
 */
@Composable
fun CameraPreviewBox(
    width: Dp,
    height: Dp,
    topPadding: Dp = 0.dp,
    corner: Dp = 12.dp,
    cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA,
    zoomRatio: Float = 1.0f,
    onZoomCapabilities: ((minZoom: Float, maxZoom: Float) -> Unit)? = null,  // ✅ 추가
    modifier: Modifier = Modifier,
    overlay: @Composable (BoxScope.() -> Unit) = {}
) {
    val ctx = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasPermission by remember { mutableStateOf(false) }

    // Preview
    val preview = remember {
        Preview.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .build()
    }
    val previewView = remember {
        PreviewView(ctx).apply {
            setBackgroundColor(android.graphics.Color.TRANSPARENT)
            scaleType = PreviewView.ScaleType.FILL_CENTER
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }

    var camera by remember { mutableStateOf<androidx.camera.core.Camera?>(null) }

    // 권한 요청
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { res -> hasPermission = (res[Manifest.permission.CAMERA] == true) }

    LaunchedEffect(Unit) {
        launcher.launch(arrayOf(Manifest.permission.CAMERA))
    }

    // CameraX 바인딩 (selector 바뀌면 재바인드)
    LaunchedEffect(hasPermission, cameraSelector) {
        if (!hasPermission) return@LaunchedEffect
        val future = ProcessCameraProvider.getInstance(ctx)
        future.addListener({
            val provider = future.get()
            try {
                provider.unbindAll()
                preview.setSurfaceProvider(previewView.surfaceProvider)
                // 반환된 Camera 저장 → 이후 줌 적용 가능
                camera = provider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview
                )
                // 바인딩 직후 지원 배율 한 번 알려주기
                camera?.cameraInfo?.zoomState?.value?.let { zs ->
                    onZoomCapabilities?.invoke(zs.minZoomRatio, zs.maxZoomRatio)
                }
            } catch (e: Exception) {
                Log.e("CameraPreviewBox", "bind failed", e)
            }
        }, ContextCompat.getMainExecutor(ctx))
    }

    // 줌 적용 + 지원 범위 갱신
    LaunchedEffect(zoomRatio, camera) {
        camera?.cameraInfo?.zoomState?.value?.let { state ->
            val clamped = zoomRatio.coerceIn(state.minZoomRatio, state.maxZoomRatio)
            camera?.cameraControl?.setZoomRatio(clamped)
            // 매 변경 시 현재 지원 범위 알려주기(선택)
            onZoomCapabilities?.invoke(state.minZoomRatio, state.maxZoomRatio)
        }
    }

    // 언바인드 정리
    DisposableEffect(Unit) {
        val future = ProcessCameraProvider.getInstance(ctx)
        onDispose { runCatching { future.get().unbindAll() } }
    }

    // 레이아웃
    Box(
        modifier = modifier
            .width(width)
            .height(height)
            .padding(top = topPadding)
            .clip(RoundedCornerShape(corner)),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(factory = { previewView }, modifier = Modifier.matchParentSize())
        GridOverlay(Modifier.matchParentSize())
        overlay()
    }
}
