// ui/home/HomeScreen.kt
package com.example.lookey.ui.home

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import com.example.lookey.R
import com.example.lookey.core.platform.tts.TtsController
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp

// ✅ 추가 import
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.ViewCompat
import com.example.lookey.core.platform.accessibility.A11y
import kotlinx.coroutines.delay
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalAccessibilityManager


@Composable
fun HomeScreen(
    tts: TtsController,
    userNameState: MutableState<String>,
    onAllergy: () -> Unit = {},
    onFindStore: () -> Unit = {},
    onFindProduct: () -> Unit = {},
    onCart: () -> Unit = {},
    onSettings: () -> Unit = {},
    onGuide: () -> Unit = {},
) {
    // ⛔ 겹침 유발: 기존 LaunchedEffect 제거
    // LaunchedEffect(Unit) {
    //     tts.speak("LooKey 홈입니다 ...")
    // }

    // ✅ 겹침 방지 + TalkBack 우선 처리
    SpeakIntroOncePerEntry(
        tts = tts,
        userName = userNameState.value
    )

    val tiles = listOf(
        Action("편의점\n찾기", R.drawable.ic_map, onFindStore),
        Action("상품 찾기", R.drawable.ic_scan, onFindProduct),
        Action("장바구니", R.drawable.ic_cart, onCart),
        Action("알레르기\n정보 입력", R.drawable.ic_pill, onAllergy),
        Action("설정", R.drawable.ic_settings, onSettings),
        Action("사용법", R.drawable.ic_help, onGuide),
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        // 로고 헤더 (2칸 차지)
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column(Modifier.fillMaxWidth()) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(R.drawable.lookey),
                        contentDescription = "LooKey 로고",
                        modifier = Modifier.size(55.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "LooKey",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                DropShadowDivider()
            }
        }

        // 인사 문구 (2칸 차지)
        item(span = { GridItemSpan(maxLineSpan) }) {
            Text(
                text = "${userNameState.value}님, \n안녕하세요",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                lineHeight = 45.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            )
            Spacer(Modifier.height(10.dp))
        }

        // 타일 2열 그리드
        items(tiles) { action ->
            ActionTile(
                label = action.label,
                iconRes = action.iconRes,
                onClick = action.onClick
            )
        }
    }
}

@Composable
private fun SpeakIntroOncePerEntry(
    tts: TtsController,
    userName: String
) {
    val context = LocalContext.current
    val view = LocalView.current
    val a11y = LocalAccessibilityManager.current   // ✅ 추가
    var spoken by rememberSaveable { mutableStateOf(false) }

    val message = "${userName}님, LooKey 홈입니다. 편의점 찾기, 상품 찾기, 장바구니, 알레르기, 설정, 사용법 버튼이 있습니다. 화면을 아래로 스크롤할 수 있습니다."

    DisposableEffect(Unit) { onDispose { tts.stop() } }

    LaunchedEffect(Unit) {
        if (spoken) return@LaunchedEffect
        spoken = true

        if (A11y.isScreenReaderOn(context)) {
            delay(400)
            // 300~600ms 권장
            view.announceForAccessibility(message)     // ✅ Compose 방식
        } else {
            delay(150)
            tts.speak(message)
        }
    }
}

private data class Action(
    val label: String,
    @DrawableRes val iconRes: Int,
    val onClick: () -> Unit
)

@Composable
fun DropShadowDivider(
    offsetY: Dp = 3.dp,
    blur: Dp = 4.dp,
    color: Color = Color.Black.copy(alpha = 0.25f)
) {
    val height = offsetY + blur
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .drawBehind {
                val startY = offsetY.toPx()
                val endY = startY + blur.toPx()
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(color, Color.Transparent),
                        startY = startY,
                        endY = endY
                    )
                )
            }
    )
}

private val ICON_SLOT_HEIGHT = 120.dp

@Composable
private fun ActionTile(
    label: String,
    @DrawableRes iconRes: Int,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        shape = MaterialTheme.shapes.extraLarge,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ICON_SLOT_HEIGHT),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(iconRes),
                    contentDescription = label,
                    modifier = Modifier.size(65.dp)
                )
            }
            Spacer(Modifier.height(0.dp))
            Text(
                text = label,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                lineHeight = 40.sp
            )
        }
    }
}
