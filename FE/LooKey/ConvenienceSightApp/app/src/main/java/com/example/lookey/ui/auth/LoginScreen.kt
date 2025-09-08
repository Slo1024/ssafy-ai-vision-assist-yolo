package com.example.lookey.ui.auth

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.lookey.R
import com.example.lookey.core.platform.tts.TtsController
import com.example.lookey.ui.components.GoogleSignInButton
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.example.lookey.BuildConfig
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight


@Composable
fun LoginScreen(
    onSignedIn: () -> Unit,
    tts: TtsController,
    @DrawableRes logoResId: Int = R.drawable.lookey,
    @DrawableRes googleIconResId: Int = R.drawable.ic_google_logo,
    showSkip: Boolean = BuildConfig.SHOW_LOGIN_SKIP
) {
    val context = LocalContext.current

    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
    }
    val googleClient = remember { GoogleSignIn.getClient(context, gso) }

    LaunchedEffect(Unit) {
        val last = GoogleSignIn.getLastSignedInAccount(context)
        if (last != null) {
            tts.speak("이미 로그인되어 있습니다.")
            onSignedIn()
        } else {
            tts.speak("로그인 화면입니다. 구글로 시작하기 버튼을 누르세요.")
        }
    }

    val signInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data as Intent?)
        try {
            val account = task.getResult(ApiException::class.java)
            if (account != null) {
                tts.speak("${account.displayName ?: "사용자"}님, 로그인되었습니다.")
                onSignedIn()
            } else {
                tts.speak("로그인에 실패했습니다.")
            }
        } catch (_: ApiException) {
            tts.speak("로그인에 실패했습니다.")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(bottom = 48.dp), // 버튼과 간격
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 상단 설명 문구
            Text(
                text = "저시력자를 위한\n편의점 쇼핑 도우미",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(16.dp))

            // 로고 (VectorDrawable)
            Image(
                painter = painterResource(logoResId),
                contentDescription = "LooKey 로고",
                modifier = Modifier.size(120.dp)
            )

            Spacer(Modifier.height(12.dp))
            // 앱명
            Text(
                text = "LooKey",
                style = MaterialTheme.typography.headlineMedium,
                color = Color(0xFF1877F2), // 피그마 파랑
                fontWeight = FontWeight.Bold
            )
        }

        if (showSkip) {
            TextButton(
                onClick = { tts.speak("로그인을 건너뛰고 홈으로 이동합니다."); onSignedIn() },
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

