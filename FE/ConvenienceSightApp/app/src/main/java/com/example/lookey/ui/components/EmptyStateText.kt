package com.example.lookey.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp

@Composable
fun EmptyStateText(
    text: String,
    lineHeight: Int = 40,   // sp
    center: Boolean = true
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge.copy(
            lineHeight = lineHeight.sp,
            lineHeightStyle = LineHeightStyle(
                alignment = LineHeightStyle.Alignment.Center,
                trim = LineHeightStyle.Trim.None
            )
        ),
        textAlign = if (center) TextAlign.Center else TextAlign.Start
    )
}
