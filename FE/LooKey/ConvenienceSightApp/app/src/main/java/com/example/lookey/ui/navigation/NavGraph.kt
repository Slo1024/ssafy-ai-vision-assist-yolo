package com.example.lookey.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.lookey.core.platform.tts.TtsController
import com.example.lookey.BuildConfig
import com.example.lookey.ui.auth.LoginScreen
import com.example.lookey.ui.home.HomeScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    tts: TtsController
) {
    NavHost(
        navController = navController,
        startDestination = if (BuildConfig.USE_AUTH) Routes.Login else Routes.Home // ★ 토글 한 줄
    ) {
        composable(Routes.Home) {
            HomeScreen(tts = tts) // 필요한 파라미터에 맞게 호출
        }
        composable(Routes.Login) {
            LoginScreen(
                onSignedIn = {
                    navController.navigate(Routes.Home) {
                        popUpTo(Routes.Login) { inclusive = true }
                    }
                },
                tts = tts
            )
        }
    }
}