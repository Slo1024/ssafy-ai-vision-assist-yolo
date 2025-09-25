package com.example.lookey.ui.components

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Shape
import com.example.lookey.R
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardCapitalization

@Composable
fun SearchInput(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.extraLarge
) {
    val focus = LocalFocusManager.current
    OutlinedTextField(
        value = query,
        onValueChange = { text ->
            // ❗여기서는 검색 호출하지 말기
            onQueryChange(text)
        },
        placeholder = { Text(placeholder, style = MaterialTheme.typography.titleLarge) },
        textStyle = MaterialTheme.typography.titleLarge,
        singleLine = true,
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.None,imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(
            onSearch = {  onSearch(query.trim()); focus.clearFocus() },
//            onDone   = { onSearch(query); focus.clearFocus() }
        ),
        trailingIcon = {
            IconButton(onClick = { onSearch(query.trim()); focus.clearFocus() }) {
                Icon(painter = painterResource(R.drawable.ic_search), contentDescription = "검색")
            }
        },
        modifier = modifier
            .height(65.dp)
            .padding(horizontal = 8.dp),
        shape = shape,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.primary
        )
    )
}
