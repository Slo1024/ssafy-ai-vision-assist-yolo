package com.example.lookey.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.lookey.ui.scan.ResultFormatter

@Composable
fun BannerMessage(
    banner: ResultFormatter.Banner,
    modifier: Modifier = Modifier
) {
    val cs = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(16.dp)

    Surface(
        color = cs.secondary,            // ← 노랑(Back)
        contentColor = cs.onSecondary,   // ← 검정
        shape = shape,
        tonalElevation = 0.dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp)
            .semantics { contentDescription = banner.text }
    ) {
        Row(
            modifier = Modifier.padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = banner.text,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Normal   // ← 굵기만 살짝 낮춤
                ),
                modifier = Modifier.fillMaxWidth(),
                maxLines = 2
            )

        }
    }
}
