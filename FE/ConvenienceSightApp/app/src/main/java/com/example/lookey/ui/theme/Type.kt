package com.example.lookey.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.lookey.R

val KoddiUDOnGothic = FontFamily(
    Font(R.font.koddiudongothic_regular,   weight = FontWeight.Normal),
    Font(R.font.koddiudongothic_bold,      weight = FontWeight.Bold),
    Font(R.font.koddiudongothic_extrabold, weight = FontWeight.ExtraBold),
)

// Typography 확장: 전 스타일에 폰트 패밀리 적용
private fun Typography.withFont(font: FontFamily) = copy(
    displayLarge   = displayLarge.copy(fontFamily = font),
    displayMedium  = displayMedium.copy(fontFamily = font),
    displaySmall   = displaySmall.copy(fontFamily = font),
    headlineLarge  = headlineLarge.copy(fontFamily = font),
    headlineMedium = headlineMedium.copy(fontFamily = font),
    headlineSmall  = headlineSmall.copy(fontFamily = font),
    titleLarge     = titleLarge.copy(fontFamily = font),
    titleMedium    = titleMedium.copy(fontFamily = font),
    titleSmall     = titleSmall.copy(fontFamily = font),
    bodyLarge      = bodyLarge.copy(fontFamily = font),
    bodyMedium     = bodyMedium.copy(fontFamily = font),
    bodySmall      = bodySmall.copy(fontFamily = font),
    labelLarge     = labelLarge.copy(fontFamily = font),
    labelMedium    = labelMedium.copy(fontFamily = font),
    labelSmall     = labelSmall.copy(fontFamily = font),
)

private val Base = Typography()

// 피그마 스펙 반영 커스텀
val AppTypography: Typography = Base.withFont(KoddiUDOnGothic).copy(
    // Bold 22 / 20 / 16
    titleLarge   = Base.titleLarge.copy(fontSize = 22.sp, fontWeight = FontWeight.Bold),
    titleMedium  = Base.titleMedium.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold),
    bodyLarge    = Base.bodyLarge.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold),

    // Regular 14
    bodyMedium   = Base.bodyMedium.copy(fontSize = 14.sp, fontWeight = FontWeight.Normal),

    // Small 12
    labelSmall   = Base.labelSmall.copy(fontSize = 12.sp, fontWeight = FontWeight.Medium),

    // Hero 54
    displayLarge   = TextStyle(fontFamily = KoddiUDOnGothic, fontWeight = FontWeight.Bold,      fontSize = 54.sp),
    headlineLarge  = TextStyle(fontFamily = KoddiUDOnGothic, fontWeight = FontWeight.ExtraBold, fontSize = 54.sp),
)
