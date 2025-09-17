package com.example.lookey

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.lookey.core.platform.tts.TtsController
import com.example.lookey.ui.navigation.AppNavGraph
import com.example.lookey.ui.theme.LooKeyTheme
import com.example.lookey.util.PrefUtil


class MainActivity : ComponentActivity() {
    private lateinit var tts: TtsController

    // Compose에서 공유할 상태
    private val userNameState = mutableStateOf("사용자") // 초기값 "사용자"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        tts = TtsController(this)

        // 앱 시작 시 저장된 값이 있으면 초기값으로 반영
        PrefUtil.getUserName(this)?.let {
            userNameState.value = it
        }

        setContent {
            LooKeyTheme {
                val navController = rememberNavController()
                AppNavGraph(
                    navController = navController,
                    tts = tts,
                    userNameState = userNameState // State 전달
                )
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