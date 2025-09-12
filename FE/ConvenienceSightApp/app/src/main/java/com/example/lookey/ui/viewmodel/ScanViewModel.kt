// ui/viewmodel/ScanViewModel.kt
package com.example.lookey.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import com.example.lookey.domain.entity.DetectResult
import com.example.lookey.ui.scan.ResultFormatter

class ScanViewModel(
    private val speak: (String) -> Unit = {}   // TTS 나중에 주입: tts::speak. 지금은 빈 람다
) : ViewModel() {

    data class UiState(
        val scanning: Boolean = false,
        val current: DetectResult? = null,
        val banner: ResultFormatter.Banner? = null
    )

    private val _ui = MutableStateFlow(UiState())
    val ui: StateFlow<UiState> = _ui

    private var lastSpokenId: String? = null   // 같은 상품 반복 낭독 방지

    fun toggleScan() {
        _ui.update { it.copy(scanning = !it.scanning) }
    }

    fun onDetected(result: DetectResult) {
        _ui.update {
            it.copy(
                current = result,
                banner  = ResultFormatter.toBanner(result)
            )
        }
        if (result.id != lastSpokenId) {
            speak(ResultFormatter.toVoice(result).text)
            lastSpokenId = result.id
        }
    }

    fun clearBanner() {
        _ui.update { it.copy(banner = null) }
    }
}
