package com.example.lookey.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object AppColors {
    val Basic       = Color(0xFFFFFFFF) // 흰색
    val Main        = Color(0xFF004AD1)
    val Back        = Color(0xFFFFF10B)
    val BorderBlack = Color(0xFF000000)

    // 다크 전용
    val DarkBg   = Color.Black
    val DarkMain = Color(0xFF535968)
    val DarkLine = Color(0xFF707582)
    val DarkCard = Color(0xFF1E1F23) // 배너/모달용 진회색

    /** 배너/모달 배경색 자동 선택 */
    @Composable
    fun bannerModalBackground(): Color =
        if (isSystemInDarkTheme()) DarkCard else Basic

    /** 배너/모달 텍스트색 자동 선택 */
    @Composable
    fun bannerModalContent(): Color =
        if (isSystemInDarkTheme()) Basic else BorderBlack
}

// 라이트
val LightScheme = lightColorScheme(
    primary     = AppColors.Main,
    onPrimary   = Color.White,
    secondary   = AppColors.Back,
    onSecondary = Color.Black,
    background  = AppColors.Basic,
    surface     = AppColors.Basic,
    outline     = AppColors.BorderBlack
)

// 다크
val DarkScheme = darkColorScheme(
    primary     = AppColors.DarkMain,
    onPrimary   = AppColors.Basic,

    primaryContainer   = AppColors.DarkMain,
    onPrimaryContainer = AppColors.Basic,

    background  = AppColors.DarkBg,
    onBackground= AppColors.Basic,
    surface     = AppColors.DarkBg,
    onSurface   = AppColors.Basic,

    surfaceVariant    = Color(0xFF101114),
    onSurfaceVariant  = AppColors.Basic,
    outline     = AppColors.DarkLine,

    // secondary는 노랑 유지 (배너/모달은 헬퍼로 별도 제어)
    secondary   = AppColors.Back,
    onSecondary = Color.Black
)
