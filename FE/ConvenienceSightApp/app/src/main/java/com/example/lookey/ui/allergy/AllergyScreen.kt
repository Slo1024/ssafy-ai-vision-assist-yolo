package com.example.lookey.ui.allergy

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lookey.ui.viewmodel.AllergyViewModel
import com.example.lookey.ui.components.*

@Composable
fun AllergyScreen(
    viewModel: AllergyViewModel = viewModel(),
    onMicClick: (() -> Unit)? = null
) {
    val pill = MaterialTheme.shapes.extraLarge

    var query by rememberSaveable { mutableStateOf("") }
    val results by viewModel.results.collectAsState()
    val allergies by viewModel.allergies.collectAsState()
    var pendingItem by rememberSaveable { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TitleHeader("알레르기 정보")

        SearchInput(
            query = query,
            onQueryChange = { q -> query = q; viewModel.search(q) },
            onSearch = { viewModel.search(query) },
            placeholder = "알레르기 이름을 검색해주세요",
            modifier = Modifier.fillMaxWidth(),
            shape = pill
        )

        Spacer(Modifier.height(28.dp))
        MicActionButton(onClick = { onMicClick?.invoke() }, sizeDp = 120)
        Spacer(Modifier.height(28.dp))

        when {
            query.isBlank() && allergies.isNotEmpty() -> {
                Text("내 알레르기", style = MaterialTheme.typography.titleLarge, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                allergies.forEach { name ->
                    PillListItem(
                        title = name,
                        onDelete = { viewModel.removeAllergy(name) },
                        shape = pill
                    )
                }
            }
            query.isBlank() && allergies.isEmpty() -> {
                EmptyStateText("등록된 알레르기가\n없어요.\n검색해서 추가해보세요.")
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
            message = "${pendingItem}를\n내 알레르기에\n추가하시겠습니까?",
            onConfirm = {
                viewModel.addAllergy(pendingItem!!)
                pendingItem = null
                query = ""
                viewModel.search("")
            },
            onDismiss = { pendingItem = null }
        )
    }
}
