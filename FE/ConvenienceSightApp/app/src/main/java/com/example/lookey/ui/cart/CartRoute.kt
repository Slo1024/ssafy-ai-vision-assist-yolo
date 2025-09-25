// CartRoute.kt
package com.example.lookey.ui.cart

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lookey.data.network.ApiService
import com.example.lookey.data.network.CartRepository
import com.example.lookey.data.network.RetrofitClient
import com.example.lookey.ui.viewmodel.CartViewModel

@Composable
fun CartRoute(
    apiService: ApiService = RetrofitClient.apiService
) {
    // Repository 생성 시 apiService 주입
    val repository = CartRepository(apiService)

    // ViewModel 생성 (factory 이용)
    val viewModel: CartViewModel = viewModel(
        factory = CartViewModelFactory(repository)
    )

    // 실제 화면 호출
    CartScreen(viewModel = viewModel)
}
