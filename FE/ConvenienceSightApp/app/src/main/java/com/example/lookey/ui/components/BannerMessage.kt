package com.example.lookey.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.lookey.ui.scan.ResultFormatter

@Composable
fun BannerMessage(
    banner: ResultFormatter.Banner,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (bg, fg) = bannerColorsForType(banner.type)

    Surface(
        color = bg,
        contentColor = fg,
        shape = RoundedCornerShape(30.dp),
        tonalElevation = 0.dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp)
            .semantics { contentDescription = banner.text }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = banner.text,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
                maxLines = 2
            )
            TextButton(onClick = onDismiss) {
                Text(text = "닫기", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun bannerColorsForType(
    type: ResultFormatter.Banner.Type
): Pair<Color, Color> {
    val cs = MaterialTheme.colorScheme
    return when (type) {
        ResultFormatter.Banner.Type.WARNING ->
            cs.secondary to cs.onSecondary
        ResultFormatter.Banner.Type.INFO ->
            cs.primary.copy(alpha = 0.12f) to cs.primary
        ResultFormatter.Banner.Type.SUCCESS ->
            cs.surfaceVariant to cs.onSurface
    }
}
