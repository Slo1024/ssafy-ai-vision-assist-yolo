package com.example.lookey.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color

@Composable
fun GoogleSignInButton(
    modifier: Modifier = Modifier,
    text: String = "구글로 시작하기",
    enabled: Boolean = true,
    isLoading: Boolean = false,
    @DrawableRes iconResId: Int? = null,       // ex) R.drawable.ic_google_log
    // ── Figma 토큰(기본값: 피그마 수치) ───────────────────────────────
    width: Dp = 288.dp,                        // Dimensions W
    height: Dp = 72.dp,                        // Dimensions H
    cornerRadius: Dp = 10.dp,                  // Corner radius
    iconTextGap: Dp = 10.dp,                   // Auto layout Gap
    paddingStart: Dp = 20.dp,                  // Padding left
    paddingEnd: Dp = 12.dp,                    // Padding right
    borderWidth: Dp = 1.dp,                    // Stroke 1
    containerColor: Color = MaterialTheme.colorScheme.primary,    // Fill = main
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,    // Text/Icon color
    borderColor: Color = MaterialTheme.colorScheme.outlineVariant,// Stroke = back 대체
    // ─────────────────────────────────────────────────────────────
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = modifier
            .width(width)
            .height(height)
            .semantics {
                contentDescription = text
                role = Role.Button
            },
        shape = RoundedCornerShape(cornerRadius),
        contentPadding = PaddingValues(start = paddingStart, end = paddingEnd),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        border = BorderStroke(borderWidth, borderColor)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(iconTextGap)
        ) {
            if (iconResId != null && iconResId != 0) {
                Image(
                    painter = painterResource(iconResId),
                    contentDescription = null // 버튼 전체에 CD 있음
                )
            }
            Text(
                text = text,
                fontSize = 18.sp,                     // 피그마 스타일에 맞게 조절
                fontWeight = FontWeight.SemiBold
            )
            if (isLoading) {
                CircularProgressIndicator(
                    strokeWidth = 2.dp
                )
            }
        }
    }
}
