package com.example.lookey

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.lookey.core.platform.tts.TtsController
import com.example.lookey.data.remote.ApiClient
import com.example.lookey.ui.navigation.AppNavGraph
import com.example.lookey.ui.theme.LooKeyTheme


class MainActivity : ComponentActivity() {
    private lateinit var tts: TtsController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        tts = TtsController(this) // 구현에 맞게 생성자 조정
        
        // API 환경 정보 출력 (개발 환경에서만)
        ApiClient.printEnvironmentInfo()

        setContent {
            LooKeyTheme {
                val navController = rememberNavController()
                AppNavGraph(navController = navController, tts = tts)
            }
        }
    }

    override fun onDestroy() {
        if (this::tts.isInitialized) tts.shutdown()
        super.onDestroy()
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    LooKeyTheme {
        Greeting("Android")
    }
}