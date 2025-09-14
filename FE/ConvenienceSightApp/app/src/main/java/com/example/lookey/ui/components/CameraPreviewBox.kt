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
 * 4:3 카메라 프리뷰 + GridOverlay + 오버레이 슬롯을 제공하는 컨테이너.
 * - 권한 요청/CameraX 바인딩을 내부에서 처리
 * - width/height/topPadding/round corner 외부 조절
 * - overlay 슬롯에 FeaturePill 같은 오버레이를 올려 사용
 */
@Composable
fun CameraPreviewBox(
    width: Dp,
    height: Dp,
    topPadding: Dp = 0.dp,
    corner: Dp = 12.dp,
    modifier: Modifier = Modifier,
    overlay: @Composable (BoxScope.() -> Unit) = {}
) {
    val ctx = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasPermission by remember { mutableStateOf(false) }

    // 4:3 Preview 세팅
    val preview = remember {
        Preview.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .build()
    }
    val previewView = remember {
        PreviewView(ctx).apply {
            setBackgroundColor(android.graphics.Color.TRANSPARENT)
            scaleType = PreviewView.ScaleType.FILL_CENTER // 중앙 크롭
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }

    // 권한 요청
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { res -> hasPermission = (res[Manifest.permission.CAMERA] == true) }

    LaunchedEffect(Unit) {
        launcher.launch(arrayOf(Manifest.permission.CAMERA))
    }

    // CameraX 바인딩
    LaunchedEffect(hasPermission) {
        if (!hasPermission) return@LaunchedEffect
        val pf = ProcessCameraProvider.getInstance(ctx)
        pf.addListener({
            val provider = pf.get()
            try {
                preview.setSurfaceProvider(previewView.surfaceProvider)
                provider.unbindAll()
                provider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview
                )
            } catch (e: Exception) {
                Log.e("CameraPreviewBox", "bind failed", e)
            }
        }, ContextCompat.getMainExecutor(ctx))
    }

    // 언바인드 정리
    DisposableEffect(Unit) {
        val pf = ProcessCameraProvider.getInstance(ctx)
        onDispose { runCatching { pf.get().unbindAll() } }
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
        overlay() // 외부에서 오는 오버레이 삽입 (FeaturePill 등)
    }
}
