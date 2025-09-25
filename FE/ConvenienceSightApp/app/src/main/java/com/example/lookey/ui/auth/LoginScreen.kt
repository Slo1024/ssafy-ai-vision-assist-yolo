package com.example.lookey.ui.auth

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.example.lookey.BuildConfig
import com.example.lookey.R
import com.example.lookey.core.platform.tts.TtsController
import com.example.lookey.data.network.RetrofitClient
import com.example.lookey.data.local.TokenProvider
import com.example.lookey.ui.components.GoogleSignInButton
import com.example.lookey.util.PrefUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf


@Composable
fun LoginScreen(
    onSignedIn: () -> Unit,
    tts: TtsController,
    userNameState: MutableState<String>,
    @DrawableRes logoResId: Int = R.drawable.lookey,
    @DrawableRes googleIconResId: Int = R.drawable.ic_google_logo,
    showSkip: Boolean = BuildConfig.SHOW_LOGIN_SKIP
) {
    val context = LocalContext.current

    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken("95484213731-5qj9f0guuquq6pprklb8mtvfr41re2i2.apps.googleusercontent.com")
            .build()
    }
    val googleClient = remember { GoogleSignIn.getClient(context, gso) }

    val signInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken
            Log.d("GoogleLogin", "idToken: $idToken")

            if (!idToken.isNullOrEmpty()) {
                val displayName = account.displayName ?: "사용자"
                tts.speak("$displayName 님, 로그인되었습니다.")
                sendIdTokenToServer(context, idToken, displayName, userNameState, onSignedIn)
            } else {
                tts.speak("idToken을 가져오지 못했습니다.")
            }
        } catch (e: ApiException) {
            tts.speak("로그인에 실패했습니다: ${e.statusCode}")
            Log.e("GoogleLogin", "로그인 실패", e)
        }
    }

    // 자동 로그인 체크
    LaunchedEffect(Unit) {
        val appContext = context.applicationContext
        val jwt = PrefUtil.getJwtToken(appContext)
        val userId = PrefUtil.getUserId(appContext)

        if (!jwt.isNullOrEmpty() && userId != null) {
            tts.speak("이미 로그인되어 있습니다.")
            onSignedIn()
        } else {
            tts.speak("로그인 화면입니다. 구글로 시작하기 버튼을 누르세요.")
            // 필요하면 GoogleSignIn.getLastSignedInAccount(context)?.signOut()도 호출
        }
    }


    // UI
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "저시력자를 위한\n편의점 쇼핑 도우미",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(16.dp))

            Image(
                painter = painterResource(logoResId),
                contentDescription = "LooKey 로고",
                modifier = Modifier.size(120.dp)
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = "LooKey",
                style = MaterialTheme.typography.headlineMedium,
                color = Color(0xFF1877F2),
                fontWeight = FontWeight.Bold
            )
        }

        if (showSkip) {
            TextButton(
                onClick = {
                    tts.speak("로그인을 건너뛰고 홈으로 이동합니다.")
                    onSignedIn()
                },
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 76.dp)
            ) { Text("건너뛰기") }
        }

        GoogleSignInButton(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp),
            text = "구글로 시작하기",
            iconResId = googleIconResId,
            onClick = { signInLauncher.launch(googleClient.signInIntent) }
        )
    }
}


private fun sendIdTokenToServer(
    context: Context,
    idToken: String,
    displayName: String,
    userNameState: MutableState<String>,
    onSignedIn: () -> Unit
) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = RetrofitClient.apiService.googleLogin("Bearer $idToken")

            Log.d("LoginScreen", "Server Response code=${response.code()}, message=${response.message()}")
            Log.d("LoginScreen", "Response body=${response.body()}")

            if (response.isSuccessful) {
                val data = response.body()?.data

                Log.d("LoginScreen", "server data: $data")
                Log.d("LoginScreen", "server userName: ${data?.userName}") // ✅ userName만 사용

                val jwt = data?.jwtToken
                val userId = data?.userId
                val serverName = data?.userName

                // 서버가 userName을 안 보내면 Google 계정 이름 사용
                val finalName = serverName ?: displayName

                if (!jwt.isNullOrEmpty() && userId != null) {
                    val appContext = context.applicationContext
                    TokenProvider.token = jwt
                    PrefUtil.saveUserId(appContext, userId.toString())
                    PrefUtil.saveUserName(context, finalName)
                    Log.d("PrefDebug", "Saved userName: ${PrefUtil.getUserName(context)}")
                    PrefUtil.saveJwtToken(appContext, jwt)

                    // userNameState 업데이트
                    CoroutineScope(Dispatchers.Main).launch {
                        userNameState.value = finalName
                        onSignedIn()
                    }
                }
                else {
                    Log.e("LoginScreen", "JWT 또는 userId가 비어있습니다.")
                }
            } else {
                Log.e("LoginScreen", "서버 로그인 실패: ${response.code()} ${response.message()}")
            }
        } catch (e: Exception) {
            Log.e("LoginScreen", "서버 로그인 중 예외 발생", e)
        }
    }
}

