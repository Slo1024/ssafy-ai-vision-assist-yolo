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

@Composable
fun CartScreen(
    viewModel: CartViewModel = viewModel(),
    onMicClick: (() -> Unit)? = null
) {
    val pill = MaterialTheme.shapes.extraLarge

    var query by rememberSaveable { mutableStateOf("") }
    val results by viewModel.results.collectAsState()
    val cart by viewModel.cart.collectAsState()
    var pendingItem by rememberSaveable { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TitleHeader("장바구니")

        SearchInput(
            query = query,
            onQueryChange = { q -> query = q; viewModel.search(q) },
            onSearch = { viewModel.search(query) },
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
                        title = line.name,
                        onDelete = { viewModel.removeFromCart(line.name) },
                        shape = pill
                    )
                }
            }

            query.isBlank() && cart.isEmpty() -> {
                EmptyStateText("장바구니가 비어 있어요.\n검색해서 추가해보세요.")
            }

            results.isNotEmpty() -> {
                SuggestionList(
                    items = results,
                    onClick = { pendingItem = it },
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
            message = "${pendingItem}를\n장바구니에 추가하시겠습니까?",
            onConfirm = {
                viewModel.addToCart(pendingItem!!)
                pendingItem = null
                // 장바구니 화면으로 전환
                query = ""
                viewModel.search("")
            },
            onDismiss = { pendingItem = null }
        )
    }
}
