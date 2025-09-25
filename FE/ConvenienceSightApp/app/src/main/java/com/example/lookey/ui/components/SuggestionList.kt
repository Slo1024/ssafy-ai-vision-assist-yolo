package com.example.lookey.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp

@Composable
fun SuggestionList(
    items: List<String>,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.extraLarge
) {
    // 라이트/다크 판별(현재 테마 배경 밝기 기준)
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    // 다크: 마이크 배경(primary)로 채우고 텍스트는 onPrimary
    // 라이트: 투명 배경 + outline 테두리
    val container = if (isDark) MaterialTheme.colorScheme.primary else Color.Transparent
    val content   = if (isDark) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    val border    = if (isDark) null else BorderStroke(1.dp, MaterialTheme.colorScheme.primary)

    Column(modifier) {
        items.forEach { item ->
            OutlinedButton(
                onClick = { onClick(item) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                shape = shape,
                border = border,
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = container,
                    contentColor   = content
                )
            ) {
                Text(
                    text = item,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(vertical = 10.dp)
                )
            }
        }
    }
}
