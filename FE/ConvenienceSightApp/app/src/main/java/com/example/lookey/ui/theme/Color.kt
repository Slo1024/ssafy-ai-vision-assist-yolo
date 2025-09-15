package com.example.lookey.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

object AppColors {
    val Basic       = Color(0xFFFFFFFF) // 흰색 (텍스트용)
    val Main        = Color(0xFF004AD1) // 라이트용 메인
    val Back        = Color(0xFFFFF10B)
    val BorderBlack = Color(0xFF000000)

    // 다크 전용
    val DarkBg   = Color.Black          // 배경/서피스
    val DarkMain = Color(0xFF535968)    // 버튼/포인트(Primary)
    val DarkLine = Color(0xFF707582)    // 외곽선(가독용 중간톤)
}

// 라이트 그대로 사용 (필요 시 onSurface 등 추가 지정 가능)
val LightScheme = lightColorScheme(
    primary    = AppColors.Main,
    onPrimary  = Color.White,
    secondary  = AppColors.Back,
    onSecondary= Color.Black,
    background = AppColors.Basic,
    surface    = AppColors.Basic,
    outline    = AppColors.BorderBlack
)

// ✅ 다크: 배경/서피스=DarkBg, 버튼/메인=DarkMain, 텍스트=Basic(흰색)
val DarkScheme = darkColorScheme(
    primary     = AppColors.DarkMain,
    onPrimary   = AppColors.Basic,

    // 컨테이너(카드/타일)도 동일 톤을 쓰고 싶으면 아래 유지
    primaryContainer   = AppColors.DarkMain,
    onPrimaryContainer = AppColors.Basic,

    background  = AppColors.DarkBg,
    onBackground= AppColors.Basic,
    surface     = AppColors.DarkBg,
    onSurface   = AppColors.Basic,

    // 카드의 대체 표면/보더 톤 (원하면 더 어둡게 바꿔도 OK)
    surfaceVariant    = Color(0xFF101114),
    onSurfaceVariant  = AppColors.Basic,
    outline     = AppColors.DarkLine,

    // 필요 시 secondary 유지
    secondary   = AppColors.Back,
    onSecondary = Color.Black
)
