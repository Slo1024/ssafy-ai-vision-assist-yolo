package com.example.lookey.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.lookey.R

// KoddiUDOnGothic 폰트 패밀리 (팀 전용 폰트)
val KoddiUDOnGothic = FontFamily(
    Font(R.font.koddiudongothic_regular, weight = FontWeight.Normal),
    Font(R.font.koddiudongothic_bold, weight = FontWeight.Bold),
    Font(R.font.koddiudongothic_extrabold, weight = FontWeight.ExtraBold),
)

// 전역 Typography (KoddiUDOnGothic 강제 적용)
val AppTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = KoddiUDOnGothic,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 54.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = KoddiUDOnGothic,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 32.sp
    ),
    titleLarge = TextStyle(
        fontFamily = KoddiUDOnGothic,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp
    ),
    titleMedium = TextStyle(
        fontFamily = KoddiUDOnGothic,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = KoddiUDOnGothic,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = KoddiUDOnGothic,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp
    ),
    labelSmall = TextStyle(
        fontFamily = KoddiUDOnGothic,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp
    )
)
