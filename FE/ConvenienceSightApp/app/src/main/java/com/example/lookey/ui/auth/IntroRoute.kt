package com.example.lookey.ui.auth

import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.lookey.core.platform.tts.TtsController
import com.example.lookey.util.PrefUtil
import com.example.lookey.ui.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

@Composable
fun IntroRoute(navController: NavHostController) {
    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel()
    val tts = remember { TtsController(context) }

    // ✅ Google 로그인 옵션 설정 (Firebase 없이)
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken("95484213731-5qj9f0guuquq6pprklb8mtvfr41re2i2.apps.googleusercontent.com")
            .build()
    }

    val googleSignInClient = remember {
        GoogleSignIn.getClient(context, gso)
    }

    // ✅ Google 로그인 런처
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        Log.d("GoogleSignIn", "Intent result received")  // 🔽 추가
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken

            Log.d("GoogleSignIn", "account: $account") // 🔽 추가
            Log.d("GoogleSignIn", "id: ${account.id}")
            Log.d("GoogleSignIn", "email: ${account.email}")
            Log.d("GoogleSignIn", "idToken: ${account.idToken}")

            if (!idToken.isNullOrEmpty()) {
                // ✅ idToken 전달
                authViewModel.loginWithGoogleToken(idToken, context) { resultType ->
                    when (resultType) {
                        AuthViewModel.ResultType.EXISTING_USER -> {
                            PrefUtil.saveUserId(context, idToken) // 필요시 userId 따로 추출
                            navController.navigate("main") {
                                popUpTo("intro") { inclusive = true }
                            }
                        }
                        AuthViewModel.ResultType.NEW_USER -> {
                            navController.navigate("signup") {
                                popUpTo("intro") { inclusive = true }
                            }
                        }
                        AuthViewModel.ResultType.ERROR -> {
                            Toast.makeText(context, "로그인 중 오류 발생", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                Toast.makeText(context, "idToken이 비어있습니다.", Toast.LENGTH_SHORT).show()
            }
        } catch (e: ApiException) {
            Log.e("GoogleSignIn", "로그인 실패: ${e.message}")
            Toast.makeText(context, "구글 로그인 실패: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    LoginScreen(
        onSignedIn = {
            launcher.launch(googleSignInClient.signInIntent)
        },
        tts = tts
    )
}
