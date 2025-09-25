package com.example.lookey.ui.storemap

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun StoreListScreen(
    stores: List<StoreUiModel>,
    onStoreClick: (StoreUiModel) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(30.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            Text(
                text = "편의점 찾기",
                style = MaterialTheme.typography.labelLarge, // 그대로 유지
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp),
                textAlign = TextAlign.Center
            )
        }
        item { Spacer(Modifier.height(12.dp)) }

        items(stores) { s ->
            StoreButton(
                name = s.name,
                distanceMeters = s.distanceMeters,
                onClick = { onStoreClick(s) }
            )
        }
    }
}

/** "세븐일레븐 녹산공단점" -> ("세븐일레븐", "녹산공단점") */
private fun splitStoreName(name: String): Pair<String, String> {
    val parts = name.trim().split(Regex("\\s+"), limit = 2)
    return if (parts.size >= 2) parts[0] to parts[1] else name to ""
}

@Composable
private fun StoreButton(
    name: String,
    distanceMeters: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    // 브랜드/지점 2줄로 분리 (긴 이름 깔끔 처리)
    val (brand, branch) = remember(name) { splitStoreName(name) }
    val cardHeight = if (branch.isNotEmpty()) 148.dp else 128.dp // 지점 줄 있으면 살짝 더 높게

    if (isDark) {
        // 다크: 회색 배경 + 흰 글자
        Card(
            modifier = modifier
                .fillMaxWidth()
                .height(cardHeight)
                .clickable { onClick() },
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor   = MaterialTheme.colorScheme.onPrimaryContainer
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = brand,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                    if (branch.isNotEmpty()) {
                        Text(
                            text = branch,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                    }
                    Text(
                        text = formatDistance(distanceMeters),
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    } else {
        // 라이트: 테두리만 메인색
        OutlinedCard(
            modifier = modifier
                .fillMaxWidth()
                .height(cardHeight)
                .clickable { onClick() },
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
            colors = CardDefaults.outlinedCardColors(
                containerColor = Color.Transparent,
                contentColor   = MaterialTheme.colorScheme.primary
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = brand,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                    if (branch.isNotEmpty()) {
                        Text(
                            text = branch,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                    }
                    Text(
                        text = formatDistance(distanceMeters),
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private fun formatDistance(meters: Int): String =
    if (meters >= 1000) String.format("%.1f km", meters / 1000f) else "${meters}m"

// 카카오맵 길안내 열기
fun openKakaoRoute(ctx: Context, lat: Double, lng: Double, name: String) {
    val appUri = Uri.parse("kakaomap://route?sp=&ep=$lat,$lng&by=FOOT")
    val appIntent = Intent(Intent.ACTION_VIEW, appUri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    try {
        ctx.startActivity(appIntent)
    } catch (_: ActivityNotFoundException) {
        runCatching {
            val web = Uri.parse("https://map.kakao.com/link/to/${Uri.encode(name)},$lat,$lng")
            ctx.startActivity(Intent(Intent.ACTION_VIEW, web).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }.onFailure {
            val market = Uri.parse("market://details?id=net.daum.android.map")
            ctx.startActivity(Intent(Intent.ACTION_VIEW, market).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }
}

//@Composable
//fun DummyStoreListPage(context: Context = LocalContext.current) {
//    StoreListScreen(
//        stores = dummyStores,
//        onStoreClick = { s -> openKakaoRoute(context, s.lat, s.lng, s.name) }
//    )
//}
//
//@Preview(showBackground = true, widthDp = 360, heightDp = 740)
//@Composable
//private fun PreviewStoreList() {
//    StoreListScreen(stores = dummyStores, onStoreClick = {})
//}
