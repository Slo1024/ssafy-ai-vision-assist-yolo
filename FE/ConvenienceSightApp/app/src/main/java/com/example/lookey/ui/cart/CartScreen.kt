package com.example.lookey.ui.cart

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lookey.ui.viewmodel.CartViewModel
import com.example.lookey.ui.components.*
import com.example.lookey.data.model.ProductSearchResponse
import com.example.lookey.ui.viewmodel.CartLine

@Composable
fun CartScreen(
    viewModel: CartViewModel = viewModel(),
    onMicClick: (() -> Unit)? = null
) {
    val pill = MaterialTheme.shapes.extraLarge

    LaunchedEffect(Unit) {
        viewModel.loadCart()
    }

    var query by rememberSaveable { mutableStateOf("") }
    val results by viewModel.results.collectAsState()
    val cart by viewModel.cart.collectAsState()
    var pendingItem by remember { mutableStateOf<ProductSearchResponse.Item?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TitleHeader("장바구니")

        SearchInput(
            query = query,
            onQueryChange = { q -> query = q; viewModel.searchProducts(q) },
            onSearch = { viewModel.searchProducts(query) },
            placeholder = "상품 이름을 검색해주세요",
            modifier = Modifier.fillMaxWidth(),
            shape = pill
        )


        Spacer(Modifier.height(28.dp))
        MicActionButton(onClick = { onMicClick?.invoke() }, sizeDp = 120)
        Spacer(Modifier.height(28.dp))

        when {
            query.isBlank() && cart.isNotEmpty() -> {
                Text(
                    "내 장바구니",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                cart.forEach { line ->
                    PillListItem(
                        title = line.name ?: "이름 없음",
                        onDelete = { line.cartId?.let {  } },
                        shape = pill
                    )
                }
            }

            query.isBlank() && cart.isEmpty() -> {
                EmptyStateText("장바구니가 비어 있어요.\n검색해서 추가해보세요.")
            }

            results.isNotEmpty() -> {
                // results를 그냥 String 리스트가 아니라 CartLine 리스트로 변환해서 전달
                SuggestionList(
                    items = results.map { it.productName },
                    onClick = { clickedName ->
                        pendingItem = results.find { it.productName == clickedName }
                    },
                    shape = pill
                )
            }

            else -> {
                Text("검색 결과가 없어요", style = MaterialTheme.typography.labelLarge)
            }
        }
    }

    if (pendingItem != null) {
        ConfirmDialog(
            message = "${pendingItem?.productName}를\n장바구니에 추가하시겠습니까?",
            onConfirm = {
                pendingItem?.let {
                    viewModel.addToCart(it.productId!!.toInt(), it.productName)
                }
                pendingItem = null
                query = ""
                viewModel.searchProducts("")
            },
            onDismiss = { pendingItem = null }
        )
    }
}
