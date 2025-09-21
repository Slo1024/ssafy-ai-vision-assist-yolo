package com.example.lookey.ui.allergy

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import com.example.lookey.ui.viewmodel.AllergyViewModel
import com.example.lookey.ui.components.*
import com.example.lookey.core.platform.accessibility.A11y
import kotlinx.coroutines.delay

@Composable
fun AllergyScreen(
    vm: AllergyViewModel,
    onMicClick: (() -> Unit)? = null
) {
    val state by vm.state.collectAsState()
    val pill = MaterialTheme.shapes.extraLarge
    var pendingItem by remember { mutableStateOf<Long?>(null) }  // allergyId 임시 저장

    // ✅ 스크린리더용 공지 람다(폴백)
    val announce = rememberA11yAnnounce()

    // 화면 초기화 시 알러지 목록 로드 + 초기 안내
    LaunchedEffect("load") { vm.load() }
    LaunchedEffect("intro") {
        delay(400) // TalkBack 초기 낭독 끝나도록 약간 지연
        announce("알레르기 정보 화면입니다. 검색창과 음성 버튼이 있습니다.")
    }

    // 로딩 상태 변화 공지
    LaunchedEffect(state.loading) {
        if (state.loading) announce("불러오는 중입니다.")
    }

    // 검색 결과 공지
    LaunchedEffect(state.query, state.suggestions) {
        if (state.query.isNotBlank()) {
            val n = state.suggestions.size
            if (n > 0) announce("검색 결과 ${n}개가 있습니다.")
            else announce("검색 결과가 없습니다.")
        }
    }

    // 내 알레르기 목록 존재 공지 (검색이 비어 있을 때만)
    LaunchedEffect(state.query, state.myAllergies.size) {
        if (state.query.isBlank() && state.myAllergies.isNotEmpty()) {
            announce("내 알레르기 ${state.myAllergies.size}개가 있습니다.")
        }
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
            onSearch = { q -> vm.doSearch(q) },
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
            // 다이얼로그 열릴 때 공지
            LaunchedEffect(pendingItem) {
                announce("${item.name}를 내 알레르기에 추가하시겠습니까? 확인 또는 취소 버튼이 있습니다.")
            }
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
        // 다이얼로그 노출 시 공지
        LaunchedEffect(msg) { announce("오류가 발생했습니다. $msg") }

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

/** 스크린리더 켜졌을 때만 공지하는 람다 (Compose 버전 이슈 없이 동작하는 폴백) */
@Composable
private fun rememberA11yAnnounce(): (String) -> Unit {
    val context = LocalContext.current
    val view = LocalView.current
    return remember {
        { text ->
            if (A11y.isScreenReaderOn(context)) {
                try {
                    @Suppress("DEPRECATION")
                    view.announceForAccessibility(text)
                } catch (_: Throwable) { /* no-op */ }
            }
        }
    }
}
