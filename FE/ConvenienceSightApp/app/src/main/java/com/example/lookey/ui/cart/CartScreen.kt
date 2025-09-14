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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lookey.R
import com.example.lookey.ui.viewmodel.CartViewModel

@Composable
fun CartScreen(
    viewModel: CartViewModel = viewModel(),
    onSuggestionClick: (String) -> Unit = {},
    onMicClick: (() -> Unit)? = null,
) {
    val focusManager = LocalFocusManager.current
    val pill = MaterialTheme.shapes.extraLarge

    var query by rememberSaveable { mutableStateOf("") }
    // WithLifecycle 없을 때는 collectAsState() 사용
    val results by viewModel.results.collectAsState()

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
            onValueChange = {
                query = it
                viewModel.search(query) // 입력 즉시 필터링
            },
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

        if (results.isNotEmpty()) {
            Column(Modifier.fillMaxWidth()) {
                results.forEach { item ->
                    OutlinedButton(
                        onClick = { onSuggestionClick(item) },
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
        } else if (query.isNotBlank()) {
            Text("검색 결과가 없어요", style = MaterialTheme.typography.bodyLarge)
        }
    }
}
