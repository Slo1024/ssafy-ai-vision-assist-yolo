package com.example.lookey.ui.util

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.layout.padding

/** 네비게이션 바 높이(dp) */
@Composable
fun navigationBarHeight(): Dp =
    WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

/** 상태 바 높이(dp) */
@Composable
fun statusBarHeight(): Dp =
    WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

/** 네비게이션 바 영역만큼 자동 패딩 */
@Composable
fun Modifier.navigationBarPadding(): Modifier =
    this.padding(WindowInsets.navigationBars.asPaddingValues())
