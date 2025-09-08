package com.example.lookey.ui.theme

import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

object AppColors {
    val Basic        = Color(0xFFFFFFFF) // basic
    val Main         = Color(0xFF004AD1) // main
    val Back         = Color(0xFFFFF10B) // back
    val BorderBlack  = Color(0xFF000000) // border_black
}

// 필요 시 Theme.kt에서 사용
val LightScheme = lightColorScheme(
    primary    = AppColors.Main,
    onPrimary  = Color.White,
    secondary  = AppColors.Back,
    onSecondary= Color.Black,
    background = AppColors.Basic,
    surface    = AppColors.Basic,
    outline    = AppColors.BorderBlack
)
