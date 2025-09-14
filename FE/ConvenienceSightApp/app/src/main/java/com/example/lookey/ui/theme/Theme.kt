package com.example.lookey.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// 다크 스킴 (필요하면 색상 조정)
private val DarkScheme = darkColorScheme(
    primary     = AppColors.Main,
    onPrimary   = Color.White,
    secondary   = AppColors.Back,
    onSecondary = Color.Black,
    background  = Color.White,   // 완전 흰색 유지
    surface     = Color.White,   // 완전 흰색 유지
    outline     = AppColors.BorderBlack
)

@Composable
fun LooKeyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // 라이트 스킴은 Color.kt의 LightScheme 사용
    val scheme = if (darkTheme) DarkScheme else LightScheme

    MaterialTheme(
        colorScheme = scheme,        // ❗ AppColors(팔레트 아님), ColorScheme을 넣어야 함
        typography  = AppTypography, // KoddiUDOnGothic 강제 반영
        content     = content
    )
}
