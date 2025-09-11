package com.example.lookey.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class CameraViewModel : ViewModel() {
    data class UiState(
        val isScanning: Boolean = false
    )

    private val _ui = MutableStateFlow(UiState())
    val ui: StateFlow<UiState> = _ui

    fun toggleScan() = _ui.update { it.copy(isScanning = !it.isScanning) }
}
