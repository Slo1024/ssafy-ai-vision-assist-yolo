package com.example.lookey.ui.scan

import android.Manifest
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lookey.R
import com.example.lookey.ui.scan.overlay.GridOverlay
// ✅ 더미 루프용
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.random.Random

import com.example.lookey.ui.viewmodel.ScanViewModel
import com.example.lookey.domain.entity.DetectResult
//import com.example.lookey.ui.components.BannerMessage


@Composable
fun ScanCameraScreen(
    back: () -> Unit,
    vm: ScanViewModel = viewModel()
) {
    val ctx = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val ui by vm.ui.collectAsState()

    var hasPermission by remember { mutableStateOf(false) }

    val preview = remember { Preview.Builder().build() }
    val previewView = remember { PreviewView(ctx) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { res ->
        hasPermission = (res[Manifest.permission.CAMERA] == true)
    }

    LaunchedEffect(Unit) {
        launcher.launch(arrayOf(Manifest.permission.CAMERA))
        // 필요 시 음성까지:
        // launcher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))
    }

    // ✅ 취소 안전한 더미 감지 루프
    LaunchedEffect(ui.scanning) {
        if (ui.scanning) {
            while (isActive && ui.scanning) {
                delay(2000)
                vm.onDetected(
                    DetectResult(
                        id = listOf("coke","pepsi","latte").random(),
                        name = "코카콜라 제로 500ml",
                        price = 2200, promo = "1+1",
                        hasAllergy = Random.nextBoolean(),
                        allergyNote = "유당 포함",
                        confidence = 0.92f
                    )
                )
            }
        }
    }

    LaunchedEffect(hasPermission) {
        if (!hasPermission) return@LaunchedEffect
        val pf = ProcessCameraProvider.getInstance(ctx)
        pf.addListener({
            val provider = pf.get()
            try {
                preview.setSurfaceProvider(previewView.surfaceProvider)
                provider.unbindAll()
                provider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview)
            } catch (e: Exception) {
                Log.e("Camera", "bind failed", e)
            }
        }, ContextCompat.getMainExecutor(ctx))
    }

    // (선택) 바인딩 해제
    DisposableEffect(Unit) {
        val pf = ProcessCameraProvider.getInstance(ctx)
        onDispose { runCatching { pf.get().unbindAll() } }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(WindowInsets.safeDrawing.asPaddingValues())
    ) {
        // 카메라 프리뷰
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        // 격자
        GridOverlay(modifier = Modifier.fillMaxSize())

        // 중앙 흰 pill 버튼 (마이크 위)
        FeaturePill(
            text = if (ui.scanning) "상품 탐색중" else "상품 탐색 시작",
            onClick = { vm.toggleScan() },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = (-140).dp)
        )

        // ✅ 상단 배너 (이름 변경 반영)
        ui.banner?.let { b ->
            Box(Modifier.align(Alignment.TopCenter)) {
                BannerMessage(banner = b, onDismiss = { vm.clearBanner() })
            }
        }

        // 마이크 버튼
        MicButton(
            onClick = { /* TODO 음성인식 */ },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = (-64).dp)
        )

        // 길안내/상품 인식 토글 (맨 아래)
        BottomToggle(
            left = "길 안내",
            right = "상품 인식",
            selectedRight = true,
            onLeft = back,
            onRight = {},
            modifier = Modifier.align(Alignment.BottomCenter) // ✅ 정렬 보장
        )
    }
}

@Composable
private fun FeaturePill(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .wrapContentSize()
            .semantics { role = Role.Button; contentDescription = text }
            .clickable(onClick = onClick)
    ) {
        Image(
            painter = painterResource(R.drawable.ic_feature_rec),
            contentDescription = null
        )
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
private fun MicButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Int = 72
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .semantics { role = Role.Button; contentDescription = "음성 인식" }
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.ic_ellipse_for_mic),
            contentDescription = null,
            modifier = Modifier.matchParentSize()
        )
        Image(
            painter = painterResource(R.drawable.ic_mic),
            contentDescription = null,
            colorFilter = ColorFilter.tint(Color.White),
            modifier = Modifier.size((size * 0.45f).dp)
        )
    }
}

@Composable
private fun BottomToggle(
    left: String,
    right: String,
    selectedRight: Boolean,
    onLeft: () -> Unit,
    onRight: () -> Unit,
    modifier: Modifier = Modifier
) {
    val h = 48.dp
    Row(
        modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Surface(
            color = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
            shape = MaterialTheme.shapes.extraLarge,
            modifier = Modifier
                .height(h)
                .clickable(onClick = onLeft)
                .semantics { role = Role.Button; contentDescription = left }
        ) {
            Text(
                left,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.width(8.dp))
        Surface(
            color = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = MaterialTheme.shapes.extraLarge,
            modifier = Modifier
                .height(h)
                .clickable(onClick = onRight)
                .semantics { role = Role.Button; contentDescription = right }
        ) {
            Text(
                right,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                fontWeight = FontWeight.Bold
            )
        }
    }
}