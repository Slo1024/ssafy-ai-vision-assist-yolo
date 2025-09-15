package com.example.lookey.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.lookey.data.settings.ThemePreference
import com.example.lookey.ui.theme.ThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppSettingsViewModel(app: Application) : AndroidViewModel(app) {
    private val pref = ThemePreference(app)

    val themeMode = pref.themeFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, ThemeMode.SYSTEM)

    fun setTheme(mode: ThemeMode) = viewModelScope.launch {
        pref.setTheme(mode)
    }
}