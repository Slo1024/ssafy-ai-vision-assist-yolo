package com.example.lookey.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lookey.ui.components.TitleHeader
import com.example.lookey.ui.theme.ThemeMode
import com.example.lookey.ui.viewmodel.AppSettingsViewModel

@Composable
fun SettingsScreen(vm: AppSettingsViewModel = viewModel()) {
    val mode by vm.themeMode.collectAsState()
    var open by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 20.dp)
    ) {
        TitleHeader("설정")

        ListItem(
            headlineContent = {
                Text(
                    "테마",
                    style = MaterialTheme.typography.labelLarge.copy( // ⬅️ 제목 글씨 키움
                        lineHeight = 26.sp,
                        fontWeight = FontWeight.Bold
                    ),
                )
            },
            supportingContent = {
                Text(
                    modeLabel(mode),
                    style = MaterialTheme.typography.titleLarge.copy(
                        lineHeight = 22.sp
                    ),
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { open = true }
        )
        Divider()
    }

    if (open) ThemeSelectDialog(
        current = mode,
        onSelect = { vm.setTheme(it); open = false },
        onDismiss = { open = false }
    )
}

private fun modeLabel(m: ThemeMode) = when (m) {
    ThemeMode.SYSTEM -> "시스템 기본"
    ThemeMode.LIGHT -> "라이트"
    ThemeMode.DARK -> "다크"
}

@Composable
private fun ThemeSelectDialog(
    current: ThemeMode,
    onSelect: (ThemeMode) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("테마 선택", style = MaterialTheme.typography.labelLarge.copy(
            lineHeight = 22.sp
        ),) },
        text = {
            Column {
                ThemeMode.values().forEach { m ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(m) }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            modeLabel(m),
                            style = MaterialTheme.typography.titleLarge.copy(
                                lineHeight = 22.sp
                            ),
                        )
                        RadioButton(selected = current == m, onClick = { onSelect(m) })
                    }
                }
            }
        },
        confirmButton = {}
    )
}
