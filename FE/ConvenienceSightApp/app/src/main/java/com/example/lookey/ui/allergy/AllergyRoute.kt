// com.example.lookey.ui.allergy.AllergyRoute.kt
package com.example.lookey.ui.allergy

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.example.lookey.data.settings.ServiceLocator

@Composable
fun AllergyRoute(
    onMicClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val vm = remember { ServiceLocator.allergyViewModel(context) }  // context 넘김

    AllergyScreen(vm = vm, onMicClick = onMicClick)
}

