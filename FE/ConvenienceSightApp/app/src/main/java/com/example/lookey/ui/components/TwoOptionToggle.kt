package com.example.lookey.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable

/**
 * 하단 “길 안내 / 상품 인식” 토글 – 두 옵션 중 하나 선택
 */
@Composable
fun TwoOptionToggle(
    leftText: String,
    rightText: String,
    selectedLeft: Boolean,
    onLeft: () -> Unit,
    onRight: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(28.dp)
    val border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)

    Surface(shape = shape, border = border, color = Color.Transparent, modifier = modifier) {
        Row {
            Segment(
                text = leftText,
                selected = selectedLeft,
                onClick = onLeft,
                leftRounded = true,
                rightRounded = false
            )
            Segment(
                text = rightText,
                selected = !selectedLeft,
                onClick = onRight,
                leftRounded = false,
                rightRounded = true
            )
        }
    }
}

@Composable
private fun Segment(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    leftRounded: Boolean,
    rightRounded: Boolean
) {
    val bg = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
    val fg = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
    val shape = RoundedCornerShape(
        topStart = if (leftRounded) 28.dp else 0.dp,
        bottomStart = if (leftRounded) 28.dp else 0.dp,
        topEnd = if (rightRounded) 28.dp else 0.dp,
        bottomEnd = if (rightRounded) 28.dp else 0.dp
    )
    Surface(
        color = bg,
        contentColor = fg,
        shape = shape,
        modifier = Modifier
            .height(48.dp)
            .widthIn(min = 96.dp)
            .semantics { role = Role.Button; contentDescription = text }
            .clickable(onClick = onClick)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )
    }
}
