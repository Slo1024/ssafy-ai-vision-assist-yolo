package com.example.lookey.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

@Composable
fun SuggestionList(
    items: List<String>,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.extraLarge
) {
    Column(modifier) {
        items.forEach { item ->
            OutlinedButton(
                onClick = { onClick(item) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                shape = shape,
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
