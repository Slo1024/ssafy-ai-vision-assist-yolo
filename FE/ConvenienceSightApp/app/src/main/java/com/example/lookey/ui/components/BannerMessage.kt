package com.example.lookey.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.lookey.ui.scan.ResultFormatter
import com.example.lookey.ui.theme.LooKeyTheme
// TalkBack 관련 imports 추가
import androidx.compose.ui.platform.LocalView
import androidx.core.view.ViewCompat
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.liveRegion

@Composable
fun BannerMessage(
    banner: ResultFormatter.Banner,
    modifier: Modifier = Modifier
) {
    val cs = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(16.dp)


    val view = LocalView.current
    LaunchedEffect(banner.text) {
        // 배너 문구가 바뀔 때 정중하게 공지
        view.announceForAccessibility(banner.text)
    }



    Surface(
        color = cs.secondary,            // ← 노랑(Back)
        contentColor = cs.onSecondary,   // ← 검정
        shape = shape,
        tonalElevation = 0.dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp)
            .wrapContentHeight()
            .semantics {
                // TalkBack: 이 영역을 '알림'처럼 취급
                paneTitle = "알림"
                liveRegion = LiveRegionMode.Polite
                contentDescription = banner.text
            }
    ) {
        Row(
            modifier = Modifier.padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = banner.text,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Normal   // ← 굵기만 살짝 낮춤
                ),
                modifier = Modifier.fillMaxWidth()
            )

        }
    }
}




@Preview(showBackground = true)
@Composable
private fun Preview_Banner_Info() {
    LooKeyTheme {
        BannerMessage(
            banner = ResultFormatter.Banner(
                type = ResultFormatter.Banner.Type.INFO,
                text = "코카콜라 제로 500ml | 2,200원\n1+1 행사품입니다."
            ),
//            onDismiss = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun Preview_Banner_Warning() {
    LooKeyTheme {
        BannerMessage(
            banner = ResultFormatter.Banner(
                type = ResultFormatter.Banner.Type.WARNING,
                text = "우유 200ml | 1,200원\n주의: 유당 포함"
            ),
//            onDismiss = {}
        )
    }
}


