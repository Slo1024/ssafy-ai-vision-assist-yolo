// app/src/main/java/com/example/lookey/ui/components/ConfirmModal.kt
package com.example.lookey.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.example.lookey.ui.theme.LooKeyTheme
// TalkBack 관련 imports 추가
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.liveRegion
import com.example.lookey.ui.theme.AppColors


@Composable
fun ConfirmModal(
    text: String,
    yesText: String = "예",
    noText: String = "아니요",
    onYes: () -> Unit,
    onNo: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 라이트=흰색, 다크=진회색(텍스트는 반대로) 자동 적용
    val bg = AppColors.bannerModalBackground()
    val fg = AppColors.bannerModalContent()

    // TalkBack 공지 + 포커스 이동
    val view = LocalView.current
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(text) {
        view.announceForAccessibility(text)
        focusRequester.requestFocus()
    }

    Surface(
        color = bg,
        contentColor = fg,
        shape = RoundedCornerShape(24.dp),           // ⬅︎ 둥근 정도 업
        tonalElevation = 0.dp,
        modifier = modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)   // ⬅️ 포커스 받을 수 있게
            .focusable()                      // ⬅️ 접근성 포커스 타깃화
            .semantics {
                // 대화상자 성격을 알려주고 즉시 읽게 설정
                paneTitle = "확인"
                liveRegion = LiveRegionMode.Assertive
                contentDescription = text
            }
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ⬅︎ 본문 텍스트 크게 & 가운데 정렬
            Text(
                text = text,
                style = MaterialTheme.typography.titleLarge, // 22sp Bold (Type.kt 기준)
                textAlign = TextAlign.Start,
            )

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ModalChoiceText(yesText, onYes)
                Spacer(Modifier.width(24.dp))
                ModalChoiceText(noText, onNo)
            }
        }
    }
}

@Composable
private fun RowScope.ModalChoiceText(
    label: String,
    onClick: () -> Unit
) {
    Text(
        text = label,
        style = MaterialTheme.typography.titleLarge, // 20sp Bold
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .weight(1f)                                // ⬅︎ 양쪽 균등 폭
            .padding(vertical = 8.dp)                  // ⬅︎ 터치 영역 확대
            .semantics { role = Role.Button }
            .clickable(onClick = onClick)
    )
}


@Preview(showBackground = true)
@Composable
private fun Preview_ConfirmModal() {
    LooKeyTheme {
        ConfirmModal(
            text = "\"코카콜라 제로 500ml\" 장바구니에 있습니다. 이걸로 안내할까요?",
            onYes = {},
            onNo = {}
        )
    }
}

