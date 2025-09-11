package com.example.lookey.ui.test

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.lookey.BuildConfig
import com.example.lookey.R
import com.example.lookey.data.network.RetrofitClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OAuthTestActivity : AppCompatActivity() {
    
    private lateinit var tvApiUrl: TextView
    private lateinit var tvResult: TextView
    private lateinit var tvLogs: TextView
    private lateinit var btnGoogleLogin: Button
    
    private val logs = mutableListOf<String>()
    
    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken
            
            addLog("✅ Google 로그인 성공")
            addLog("📧 이메일: ${account.email}")
            addLog("👤 이름: ${account.displayName}")
            addLog("🎫 ID Token: ${idToken?.take(50)}...")
            
            if (!idToken.isNullOrEmpty()) {
                sendTokenToServer(idToken)
            } else {
                updateResult("❌ ID Token을 가져올 수 없습니다", false)
            }
            
        } catch (e: ApiException) {
            addLog("❌ Google 로그인 실패: ${e.statusCode}")
            updateResult("Google 로그인 실패: ${e.message}", false)
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_oauth_test)
        
        initViews()
        setupGoogleSignIn()
        
        // API URL 표시
        tvApiUrl.text = "API URL: ${BuildConfig.API_BASE_URL}"
        addLog("🚀 OAuth 테스트 시작")
        addLog("🌐 API URL: ${BuildConfig.API_BASE_URL}")
    }
    
    private fun initViews() {
        tvApiUrl = findViewById(R.id.tvApiUrl)
        tvResult = findViewById(R.id.tvResult)
        tvLogs = findViewById(R.id.tvLogs)
        btnGoogleLogin = findViewById(R.id.btnGoogleLogin)
        
        btnGoogleLogin.setOnClickListener {
            performGoogleLogin()
        }
    }
    
    private fun setupGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken("95484213731-5qj9f0guuquq6pprklb8mtvfr41re2i2.apps.googleusercontent.com")
            .build()
        
        // 이미 로그인된 계정이 있는지 확인
        val account = GoogleSignIn.getLastSignedInAccount(this)
        if (account != null) {
            addLog("ℹ️ 이미 로그인된 계정: ${account.email}")
        }
    }
    
    private fun performGoogleLogin() {
        addLog("🔵 Google 로그인 시작...")
        updateResult("Google 로그인 진행 중...", null)
        
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken("95484213731-5qj9f0guuquq6pprklb8mtvfr41re2i2.apps.googleusercontent.com")
            .build()
        
        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        googleSignInLauncher.launch(googleSignInClient.signInIntent)
    }
    
    private fun sendTokenToServer(idToken: String) {
        addLog("📤 서버로 토큰 전송 중...")
        updateResult("서버 인증 진행 중...", null)
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.apiService.googleLogin("Bearer $idToken")
                
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        val jwtToken = body?.result?.jwtToken
                        val userId = body?.result?.userId
                        
                        addLog("✅ 서버 인증 성공!")
                        addLog("🎫 JWT Token: ${jwtToken?.take(50)}...")
                        addLog("🆔 User ID: $userId")
                        updateResult("🎉 OAuth 로그인 성공!\n JWT 토큰 발급 완료", true)
                        
                    } else {
                        val errorBody = response.errorBody()?.string()
                        addLog("❌ 서버 인증 실패: ${response.code()}")
                        addLog("📄 에러 내용: $errorBody")
                        updateResult("서버 인증 실패: ${response.code()}\n${response.message()}", false)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    addLog("🚨 네트워크 오류: ${e.message}")
                    updateResult("네트워크 오류: ${e.message}", false)
                }
            }
        }
    }
    
    private fun addLog(message: String) {
        val timestamp = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
            .format(java.util.Date())
        logs.add("[$timestamp] $message")
        
        runOnUiThread {
            tvLogs.text = logs.takeLast(20).joinToString("\n")
        }
        
        Log.d("OAuthTest", message)
    }
    
    private fun updateResult(message: String, success: Boolean?) {
        runOnUiThread {
            tvResult.text = message
            tvResult.setTextColor(when (success) {
                true -> android.graphics.Color.parseColor("#4CAF50")  // Green
                false -> android.graphics.Color.parseColor("#F44336") // Red
                null -> android.graphics.Color.parseColor("#FF9800")  // Orange
            })
        }
    }
}