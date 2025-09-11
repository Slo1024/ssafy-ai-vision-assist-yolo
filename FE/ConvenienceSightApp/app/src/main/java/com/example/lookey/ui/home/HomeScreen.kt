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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.Dp


@Composable
fun HomeScreen(
    tts: TtsController,
    userName: String? = null,
    onAllergy: () -> Unit = {},
    onFindStore: () -> Unit = {},
    onFindProduct: () -> Unit = {},
    onCart: () -> Unit = {},
    onSettings: () -> Unit = {},
    onGuide: () -> Unit = {},
) {
    LaunchedEffect(Unit) {
        tts.speak("LooKey 홈입니다. 편의점 찾기, 상품 찾기, 장바구니, 알레르기, 설정, 사용법 버튼이 있습니다. 화면을 아래로 스크롤할 수 있습니다.")
    }

    val tiles = listOf(
        Action("편의점 찾기", R.drawable.ic_map, onFindStore),
        Action("상품 찾기",   R.drawable.ic_scan, onFindProduct),
        Action("장바구니",    R.drawable.ic_cart, onCart),
        Action("알레르기 정보 입력", R.drawable.ic_pill, onAllergy),
        Action("설정",        R.drawable.ic_settings, onSettings),
        Action("사용법",      R.drawable.ic_help, onGuide),
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
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "LooKey",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary   // #004AD1 (AppColors.Main)
                    )
                }

                DropShadowDivider() // 아래 그림자 구분선
            }
        }



        // 인사 문구 (2칸 차지)
        item(span = { GridItemSpan(maxLineSpan) }) {
            Text(
                text = "${userName ?: "사용자"}님,\n안녕하세요!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp)
            )
            Spacer(Modifier.height(4.dp))
        }

        // 타일 2열 그리드 (스크롤됨)
        items(tiles) { action ->
            ActionTile(
                label = action.label,
                iconRes = action.iconRes,
                onClick = action.onClick
            )
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
    offsetY: Dp = 3.dp,                                    // Figma의 Y 오프셋 느낌
    blur: Dp = 4.dp,                                       // Figma의 Blur 느낌
    color: Color = Color.Black.copy(alpha = 0.25f)         // #000 25%
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
            .height(207.dp)     // ✅ 고정 높이 207dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(iconRes),
                contentDescription = label,           // 접근성
                modifier = Modifier.size(60.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
