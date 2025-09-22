package com.example.lookey.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lookey.data.local.TokenProvider
import com.example.lookey.data.network.Repository
import com.example.lookey.data.model.LoginResponse
import com.example.lookey.data.model.LoginData

import kotlinx.coroutines.launch

import com.example.lookey.util.PrefUtil

class AuthViewModel : ViewModel() {
    private val repository = Repository()

    enum class ResultType {
        EXISTING_USER, NEW_USER, ERROR
    }

    fun loginWithGoogleToken(
        idToken: String,
        context: Context,
        onResult: (ResultType) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val response = repository.googleAuth(idToken)
                if (response.isSuccessful) {
                    // 전체 응답 로깅하여 refresh token 필드 확인
                    Log.d("AuthViewModel", "Full response body: ${response.body()}")
                    Log.d("AuthViewModel", "Response data: ${response.body()?.data}")
                    Log.d("API", "성공: ${response.body()}")

                    val body = response.body()
                    val jwt = body?.data?.jwtToken
                    val refreshToken = body?.data?.refreshToken
                    val userId = body?.data?.userId

                    // 토큰 저장
                    if (jwt != null) {
                        PrefUtil.saveJwtToken(context = context, token = jwt)
                        TokenProvider.token = jwt // RetrofitClient에서 사용할 수 있게 저장
                        Log.d("AuthViewModel", "JWT Token saved: ${jwt.take(20)}...")
                    }

                    // Refresh Token 저장 (중요!)
                    if (refreshToken != null) {
                        PrefUtil.saveRefreshToken(context = context, token = refreshToken)
                        Log.d("AuthViewModel", "Refresh Token saved: ${refreshToken.take(20)}...")
                    } else {
                        Log.w("AuthViewModel", "No refresh token in response - server might not use refresh tokens")
                        // 일부 서버는 JWT token을 refresh에도 사용하므로 JWT를 refresh token으로도 저장
                        if (jwt != null) {
                            Log.d("AuthViewModel", "Saving JWT as refresh token fallback")
                            PrefUtil.saveRefreshToken(context = context, token = jwt)
                        }
                    }

                    if (userId != null) {
                        PrefUtil.saveUserId(context, userId.toString())
                    }

                    onResult(ResultType.EXISTING_USER)
                } else {
                    Log.e("API", "실패: ${response.code()}, ${response.errorBody()?.string()}")
                    if (response.code() == 400 || response.code() == 403) {
                        onResult(ResultType.NEW_USER)
                    } else {
                        onResult(ResultType.ERROR)
                    }
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "서버 통신 실패", e)
                onResult(ResultType.ERROR)
            }
        }
    }
}

