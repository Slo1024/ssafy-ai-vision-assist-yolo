package com.example.lookey.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import com.example.lookey.R

/**
 * 마이크 버튼 – 파란 원(ellipse) + 마이크 아이콘을 겹쳐서 표시
 * ic_ellipse_for_mic.svg, ic_mic.svg 를 drawable에 넣어두면 됨.
 */
@Composable
fun MicActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    sizeDp: Int = 64
) {
    Box(
        modifier = modifier
            .size(sizeDp.dp)
            .semantics {
                role = Role.Button
                contentDescription = "음성 인식"
            }
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        // 배경 원 (파란색 ellipse)
        Image(
            painter = painterResource(R.drawable.ic_ellipse_for_mic),
            contentDescription = null, // 배경은 읽지 않음
            modifier = Modifier.matchParentSize(),
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
        )
        // 마이크 아이콘 (흰색)
        Image(
            painter = painterResource(R.drawable.ic_mic),
            contentDescription = null,
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimary),
            modifier = Modifier.size((sizeDp * 0.6f).dp)
        )
    }
}
