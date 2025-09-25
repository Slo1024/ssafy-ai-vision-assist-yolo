package com.example.lookey.ui.allergy

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.lookey.ui.viewmodel.AllergyViewModel
import com.example.lookey.ui.components.*

@Composable
fun AllergyScreen(
    vm: AllergyViewModel,
    onMicClick: (() -> Unit)? = null
) {
    val state by vm.state.collectAsState()
    val pill = MaterialTheme.shapes.extraLarge
    var pendingItem by remember { mutableStateOf<Long?>(null) }  // allergyId 임시 저장

    // 화면 초기화 시 알러지 목록 로드
    LaunchedEffect(Unit) {
        vm.load()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TitleHeader("알레르기 정보")

        // 🔎 검색창
        SearchInput(
            query = state.query,
            onQueryChange = vm::updateQuery,
            onSearch = {  q -> vm.doSearch(q) },
            placeholder = "알레르기 이름을 검색해주세요",
            modifier = Modifier.fillMaxWidth(),
            shape = pill
        )

        Spacer(Modifier.height(28.dp))
        MicActionButton(onClick = { onMicClick?.invoke() }, sizeDp = 120)
        Spacer(Modifier.height(28.dp))

        // 📋 상태별 UI
        when {
            state.loading -> {
                CircularProgressIndicator()
            }

            state.query.isBlank() && state.myAllergies.isNotEmpty() -> {
                Text(
                    "내 알레르기",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                state.myAllergies.forEach { a ->
                    PillListItem(
                        title = a.name,
                        onDelete = { vm.delete(a.allergyListId) },
                        shape = pill
                    )
                }
            }

            state.query.isBlank() && state.myAllergies.isEmpty() -> {
                EmptyStateText("등록된 알레르기가\n없어요.\n검색해서 추가해보세요.")
            }

            state.suggestions.isNotEmpty() -> {
                SuggestionList(
                    items = state.suggestions.map { it.name },
                    onClick = { name ->
                        val item = state.suggestions.find { it.name == name }
                        pendingItem = item?.allergyListId
                    },
                    shape = pill
                )
            }

            else -> {
                Text("검색 결과가 없어요", style = MaterialTheme.typography.labelLarge)
            }
        }
    }

    // ✅ 추가 확인 다이얼로그
    if (pendingItem != null) {
        val item = state.suggestions.find { it.allergyListId == pendingItem }
        if (item != null) {
            ConfirmDialog(
                message = "${item.name}를\n내 알레르기에\n추가하시겠습니까?",
                onConfirm = {
                    pendingItem = null  // 먼저 모달 닫기
                    vm.add(item.allergyListId)
                },
                onDismiss = { pendingItem = null }
            )
        }
    }

    // 검색 결과가 비워지면 pendingItem도 초기화 (추가 후 자동으로 모달 닫힘)
    LaunchedEffect(state.suggestions) {
        if (state.suggestions.isEmpty() && state.query.isEmpty()) {
            pendingItem = null
        }
    }

    // ✅ 에러 메시지
    state.message?.let { msg ->
        AlertDialog(
            onDismissRequest = { vm.consumeMessage() },
            title = { Text("오류") },
            text = { Text(msg) },
            confirmButton = {
                TextButton(onClick = { vm.consumeMessage() }) {
                    Text("확인")
                }
            }
        )
    }
}