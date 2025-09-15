// ui/theme/Theme.kt
package com.example.lookey.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun LookeyTheme(
    mode: ThemeMode,                 // SYSTEM / LIGHT / DARK
    content: @Composable () -> Unit
) {
    val isDark = when (mode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.DARK   -> true
        ThemeMode.LIGHT  -> false
    }

    // ⬇️ Color.kt에 정의된 LightScheme/DarkScheme만 선택해서 사용
    val scheme = if (isDark) DarkScheme else LightScheme

    MaterialTheme(
        colorScheme = scheme,
        typography  = AppTypography,
        content     = content
    )
}

/** (선택) 프리뷰/레거시 호환용: 불리언으로도 사용할 수 있게 */
@Composable
fun LooKeyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val scheme = if (darkTheme) DarkScheme else LightScheme
    MaterialTheme(colorScheme = scheme, typography = AppTypography, content = content)
}
