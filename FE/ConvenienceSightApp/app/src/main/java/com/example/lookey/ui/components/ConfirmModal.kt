// app/src/main/java/com/example/lookey/ui/components/ConfirmModal.kt
package com.example.lookey.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * 상단 노란 박스 형태의 확인 모달
 * - 배경: theme.secondary(노란색), 글자: onSecondary(검정)
 * - 좌/우 버튼: “예”, “아니요”
 * - 어디에 둔든 박스로 렌더되므로, 위치는 호출하는 쪽에서 align/offset로 잡아주면 됨.
 */
@Composable
fun ConfirmModal(
    text: String,
    yesText: String = "예",
    noText: String = "아니요",
    onYes: () -> Unit,
    onNo: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = MaterialTheme.colorScheme.secondary
    val fg = MaterialTheme.colorScheme.onSecondary
    val shape = RoundedCornerShape(16.dp)

    Surface(
        color = bg,
        contentColor = fg,
        shape = shape,
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = text }
    ) {
        Column(Modifier.padding(vertical = 40.dp)) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = yesText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .semantics { role = Role.Button }
                        .clickable(onClick = onYes)
                )
                Text(
                    text = noText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .semantics { role = Role.Button }
                        .clickable(onClick = onNo)
                )
            }
        }
    }
}
