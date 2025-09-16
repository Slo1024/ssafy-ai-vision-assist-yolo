package com.example.lookey.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lookey.R

/**
 * 카메라 오버레이용 Pill 버튼.
 * - 폭은 외부에서 Modifier.width(...)로 지정 (현재 ScanCameraScreen 방식 유지)
 * - 기본 높이 44.dp, 텍스트 24sp Bold, 색상은 theme.primary
 * - 배경 SVG/PNG(ic_feature_rec)를 부모 크기에 맞게 채움
 */
@Composable
fun FeaturePill(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 44.dp,
    corner: Dp = 22.dp
) {
    Box(
        modifier = modifier
            .height(height)
            .semantics { role = Role.Button; contentDescription = text }
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.ic_feature_rec),
            contentDescription = null,
            contentScale = ContentScale.FillBounds, // 부모 크기에 맞춰 배경 채움
            modifier = Modifier.matchParentSize()
        )
        Text(
            text = text,
            fontSize = 24.sp,                    // 피그마 크기
            fontWeight = FontWeight.Bold,        // 700
            color = MaterialTheme.colorScheme.primary
        )
    }
}
