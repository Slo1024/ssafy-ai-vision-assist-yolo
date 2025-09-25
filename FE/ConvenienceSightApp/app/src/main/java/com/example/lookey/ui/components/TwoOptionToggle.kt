// components/TwoOptionToggle.kt
package com.example.lookey.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 길 안내 / 상품 인식 토글
 * - 선택된 쪽: 파란 배경 알약
 * - 미선택: 흰 배경, 파란 테두리
 * - 컨테이너: 테두리 + 그림자(조절 가능), 틈 없음
 */
@Composable
fun TwoOptionToggle(
    leftText: String,
    rightText: String,
    selectedLeft: Boolean,
    onLeft: () -> Unit,
    onRight: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 52.dp,           // ✅ 높이 조절
    elevation: Dp = 10.dp,        // ✅ 그림자 조절
    corner: Dp = 28.dp
) {
    val capsule = RoundedCornerShape(corner)

    Surface(
        modifier = modifier.height(height),
        shape = capsule,
        color = MaterialTheme.colorScheme.background,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
        tonalElevation = 0.dp,
        shadowElevation = elevation        // ✅ 그림자 세기
    ) {
        Box(Modifier.fillMaxSize()) {
            val selAlign = if (selectedLeft) Alignment.CenterStart else Alignment.CenterEnd
            // 선택 영역(절반)을 파란색으로 칠해 ‘알약’처럼 보이게
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.5f)
                    .align(selAlign),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = capsule,
                    color = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) { Spacer(Modifier.fillMaxSize()) }
            }

            Row(Modifier.fillMaxSize()) {
                Segment(
                    text = leftText,
                    selected = selectedLeft,
                    onClick = onLeft,
                    modifier = Modifier.weight(1f)
                )
                Segment(
                    text = rightText,
                    selected = !selectedLeft,
                    onClick = onRight,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun Segment(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .semantics { role = Role.Button; contentDescription = text }
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (selected) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.primary
        )
    }
}
