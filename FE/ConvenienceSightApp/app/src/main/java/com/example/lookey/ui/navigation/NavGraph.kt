package com.example.lookey.ui.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.lookey.core.platform.tts.TtsController
import com.example.lookey.BuildConfig
import com.example.lookey.ui.auth.LoginScreen
import com.example.lookey.ui.cart.CartRoute
import com.example.lookey.ui.home.HomeScreen
import com.example.lookey.ui.scan.ScanCameraScreen
import com.example.lookey.ui.allergy.AllergyRoute
import com.example.lookey.ui.settings.SettingsScreen
import com.example.lookey.ui.storemap.DummyStoreListPage
import com.example.lookey.ui.dev.DevComponentsScreen
import com.example.lookey.util.PrefUtil

@Composable
fun AppNavGraph(
    navController: NavHostController,
    tts: TtsController,
    userNameState: MutableState<String> // ← 추가
) {
    NavHost(
        navController = navController,
        startDestination = if (BuildConfig.USE_AUTH) Routes.Login else Routes.Home
    ) {
        composable(Routes.Home) {
            HomeScreen(
                tts = tts,
                userNameState = userNameState,
                onCart = { navController.navigate(Routes.Cart) }, // 필요한 파라미터에 맞게 호출
                onFindStore = { navController.navigate(Routes.DummyStores) },
                onFindProduct = { navController.navigate(Routes.Scan.Camera) }, // ← 여기!
                onAllergy = {                             // ★ 알레르기 화면 이동
                    navController.navigate(Routes.Allergy) {
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onSettings = { navController.navigate(Routes.Settings) },
                onGuide = { /* TODO: 사용법/가이드 화면 이동 */ },
            )
        }

        composable(Routes.Login) {
            LoginScreen(
                onSignedIn = {
                    navController.navigate(Routes.Home) {
                        popUpTo(Routes.Login) { inclusive = true }
                    }
                },
                tts = tts,
                userNameState = userNameState
            )
        }

        composable(Routes.Cart) {
            CartRoute()
        }

        composable(Routes.Scan.Camera) {
            ScanCameraScreen(
                back = { navController.popBackStack() },

            )
        }
        composable(Routes.Allergy) {          // ★ 추가
            AllergyRoute()
        }
        composable(Routes.Settings) { SettingsScreen() }
        composable(Routes.DummyStores) {
            DummyStoreListPage()
        }



        if (BuildConfig.DEBUG) {
            composable(Routes.Dev) { DevComponentsScreen() }
        }
    }
}
