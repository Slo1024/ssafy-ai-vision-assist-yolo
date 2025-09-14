// CartScreen.kt
package com.example.lookey.ui.cart

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lookey.R
import com.example.lookey.ui.viewmodel.CartLine
import com.example.lookey.ui.viewmodel.CartViewModel
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.LineHeightStyle


@Composable
fun CartScreen(
    viewModel: CartViewModel = viewModel(),
    onMicClick: (() -> Unit)? = null,
) {
    val focusManager = LocalFocusManager.current
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
        Text(
            text = "장바구니",
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp, bottom = 20.dp)
        )

        OutlinedTextField(
            value = query,
            onValueChange = { query = it; viewModel.search(query) },
            placeholder = { Text("상품 이름을 검색해주세요", style = MaterialTheme.typography.titleLarge) },
            textStyle = MaterialTheme.typography.titleLarge,
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = { viewModel.search(query); focusManager.clearFocus() },
                onDone = { viewModel.search(query); focusManager.clearFocus() }
            ),
            trailingIcon = {
                IconButton(onClick = { viewModel.search(query); focusManager.clearFocus() }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_search),
                        contentDescription = "검색",
                        modifier = Modifier.size(28.dp)
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(65.dp)
                .padding(horizontal = 8.dp),
            shape = pill,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.primary
            )
        )

        Spacer(Modifier.height(28.dp))

        Button(
            onClick = { onMicClick?.invoke() },
            shape = CircleShape,
            modifier = Modifier.size(120.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_mic),
                contentDescription = "음성 검색",
                modifier = Modifier.size(44.dp)
            )
        }

        Spacer(Modifier.height(28.dp))

        // 검색어가 비어있으면 장바구니, 아니면 검색결과
        when {
            query.isBlank() -> {
                if (cart.isNotEmpty()) {
                    Text(
                        "내 장바구니",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    Column(Modifier.fillMaxWidth()) {
                        cart.forEach { line ->
                            CartItemRow(
                                line = line,
                                onRemove = { viewModel.removeFromCart(line.name) }, // ← 삭제만
                                pill = pill
                            )
                        }
                    }
                } else {
                    Text(
                        text = "장바구니가 비어 있어요.\n검색해서 추가해보세요.",
                        style = MaterialTheme.typography.labelLarge.copy(
                            lineHeight = 40.sp,
                            lineHeightStyle = LineHeightStyle(
                                alignment = LineHeightStyle.Alignment.Center,
                                trim = LineHeightStyle.Trim.None
                            )
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }

            results.isNotEmpty() -> {
                Column(Modifier.fillMaxWidth()) {
                    results.forEach { item ->
                        OutlinedButton(
                            onClick = { pendingItem = item }, // 결과 탭 → 모달
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            shape = pill,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                        ) {
                            Text(
                                text = item,
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            else -> {
                Text("검색 결과가 없어요", style = MaterialTheme.typography.labelLarge)
            }
        }
    }

    // 확인 모달
    if (pendingItem != null) {
        ConfirmAddDialog(
            itemName = pendingItem!!,
            onConfirm = {
                viewModel.addToCart(pendingItem!!)
                pendingItem = null
                // 장바구니 화면으로 전환
                query = ""
                viewModel.search("")
                focusManager.clearFocus()
            },
            onDismiss = { pendingItem = null }
        )
    }
}

@Composable
private fun CartItemRow(
    line: CartLine,
    onRemove: () -> Unit,
    pill: Shape
) {
    Surface(
        shape = pill,
        tonalElevation = 0.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = line.name, // 수량 표시 제거
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onRemove) {
                Text("삭제", style = MaterialTheme.typography.titleLarge)
            }
        }
    }
}

@Composable
private fun ConfirmAddDialog(
    itemName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 18.dp)
                    .widthIn(min = 260.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${itemName}를\n장바구니에 추가하시겠습니까?",
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TextButton(onClick = onConfirm) {
                        Text(
                            "예",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                    TextButton(onClick = onDismiss) {
                        Text(
                            "아니요",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }
            }
        }
    }
}
