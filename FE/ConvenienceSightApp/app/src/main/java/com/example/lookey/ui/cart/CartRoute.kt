// CartRoute.kt
package com.example.lookey.ui.cart

import androidx.compose.runtime.Composable

@Composable
fun CartRoute(
    onMicClick: () -> Unit = {}
) {
    // CartScreen 내부에서 viewModel() 생성 + collect 처리
    CartScreen(onMicClick = onMicClick)
}
