package com.example.lookey

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.lookey.core.platform.tts.TtsController
import com.example.lookey.ui.navigation.AppNavGraph
import com.example.lookey.ui.theme.LookeyTheme
import com.example.lookey.ui.viewmodel.AppSettingsViewModel

class MainActivity : ComponentActivity() {
    private lateinit var tts: TtsController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        tts = TtsController(this)

        setContent {
            val settingsVm: AppSettingsViewModel = viewModel()
            val mode by settingsVm.themeMode.collectAsState()

            LookeyTheme(mode = mode) {
                val navController = rememberNavController()

                // 🔥 루트에서 배경 칠하기
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavGraph(navController = navController, tts = tts)
                }
            }
        }
    }

    override fun onDestroy() {
        if (this::tts.isInitialized) tts.shutdown()
        super.onDestroy()
    }
}
