// app/src/main/java/com/example/lookey/data/network/Repository.kt
package com.example.lookey.data.network

import android.graphics.Bitmap
import android.util.Log
import com.example.lookey.AppCtx
import com.example.lookey.data.local.TokenProvider
import com.example.lookey.data.model.ApiResponse
import com.example.lookey.data.model.LoginResponse
import com.example.lookey.data.remote.dto.navigation.VisionAnalyzeResponse
import com.example.lookey.data.remote.dto.product.LocationSearchResult
import com.example.lookey.data.remote.dto.product.ShelfSearchResult
import com.example.lookey.util.PrefUtil
import okhttp3.MultipartBody
import retrofit2.Response
import java.io.File
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody



class Repository {
    private val api = RetrofitClient.apiService

    // ============= 회원 =============
    suspend fun googleAuth(idToken: String): Response<LoginResponse> {
        return api.googleLogin("Bearer $idToken")
    }

    // ============= 다른 API로 인증 테스트 =============
    suspend fun testAuthWithOtherAPIs() {
        try {
            Log.d("Repository", "=== TESTING AUTH WITH OTHER APIs ===")

            // 1. 알레르기 API 테스트
            try {
                val allergyResponse = api.getAllergies()
                Log.d("Repository", "Allergy API: ${allergyResponse.code()} - ${allergyResponse.message()}")
                if (allergyResponse.isSuccessful) {
                    Log.d("Repository", "✅ Allergy API SUCCESS - Token is valid!")
                } else {
                    Log.e("Repository", "❌ Allergy API failed: ${allergyResponse.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("Repository", "Allergy API exception", e)
            }

            // 2. 장바구니 API 테스트
            try {
                val cartResponse = api.getCartList()
                if (cartResponse.isSuccessful) {
                } else {
                    Log.e("Repository", "❌ Cart API failed: ${cartResponse.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("Repository", "Cart API exception", e)
            }

            Log.d("Repository", "=== AUTH TEST COMPLETED ===")
        } catch (e: Exception) {
            Log.e("Repository", "Auth test failed", e)
        }
    }

    // ============= 디버그용 토큰 테스트 =============
    suspend fun testTokenValidity() {
        try {
            val token = TokenProvider.token ?: PrefUtil.getJwtToken(AppCtx.app)
            val refreshToken = PrefUtil.getRefreshToken(AppCtx.app)


            if (!token.isNullOrEmpty()) {
                try {
                    val parts = token.split(".")
                    if (parts.size == 3) {
                        val header = String(android.util.Base64.decode(parts[0], android.util.Base64.URL_SAFE or android.util.Base64.NO_PADDING))
                        val payload = String(android.util.Base64.decode(parts[1], android.util.Base64.URL_SAFE or android.util.Base64.NO_PADDING))

                        // exp 필드 확인
                        if (payload.contains("\"exp\":")) {
                            val expPattern = "\"exp\":(\\d+)".toRegex()
                            val expMatch = expPattern.find(payload)
                            val exp = expMatch?.groups?.get(1)?.value?.toLongOrNull()
                            if (exp != null) {
                                val currentTime = System.currentTimeMillis() / 1000
                                val isExpired = currentTime > exp
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("Repository", "Failed to decode JWT", e)
                }
            }
            Log.d("Repository", "=== TOKEN DEBUG END ===")
        } catch (e: Exception) {
            Log.e("Repository", "Token test failed", e)
        }
    }

    // ============= 상품 인식 =============
    // 005: 그대로(파일 1장, 800x600, ≤1MB)
    suspend fun productShelfSearch(cacheDir: File, frame: Bitmap)
            : ApiResponse<ShelfSearchResult> {
        testTokenValidity() // 디버그용 토큰 상태 출력
        testAuthWithOtherAPIs() // 다른 API로 인증 테스트
        ensureValidToken() // 토큰 검증 추가
        val part = buildShelfImagePart(cacheDir, frame)

        // 명시적으로 Authorization 헤더 추가
        val token = TokenProvider.token ?: PrefUtil.getJwtToken(AppCtx.app)
        val authHeader = if (!token.isNullOrEmpty()) "Bearer $token" else null


        // 인증 없이도 시도해보기
        try {
            val noAuthApi = RetrofitClient.noAuthApiService
            val testResponse = noAuthApi.searchShelf(part)
            Log.d("Repository", "No-auth response: ${testResponse.code()} - ${testResponse.message()}")
            if (testResponse.isSuccessful) {
                Log.d("Repository", "🔥 API works WITHOUT authentication! Server doesn't require auth.")
                return testResponse.bodyOrThrow()
            }
        } catch (e: Exception) {
            Log.e("Repository", "No-auth test failed", e)
        }

        // 원래대로 인증 포함하여 시도
        return api.searchShelf(part).bodyOrThrow()
    }

    // 006: Multipart 요청으로 상품 위치 찾기
    suspend fun productLocation(cacheDir: File, frame: Bitmap, productName: String)
            : ApiResponse<LocationSearchResult> {

        ensureValidToken() // 토큰 검증 추가

        // Multipart 요청 직접 사용 (JSON은 서버가 지원하지 않음)

        val img = buildCurrentFramePart(cacheDir, frame)
        val productNameBody = buildTextPart(productName)
        val r2 = api.searchProductLocation(img, productNameBody)

        if (r2.isSuccessful) {
            val result = r2.body()

            return r2.bodyOrThrow()
        } else {
            throw Exception("Location API failed with ${r2.code()}")
        }
    }

    // AI-001:
//    suspend fun navGuide(cacheDir: File, bitmap: Bitmap): VisionAnalyzeResponse? {
//        // Bitmap → File
//        val file = File(cacheDir, "nav_image.jpg").apply {
//            outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 50, it) }
//        }
//
//        // 📌 파일 크기 확인 (추가 부분)
//        val fileSizeInKB = file.length() / 1024
//        val fileSizeInMB = fileSizeInKB / 1024
//
//
//        // File → MultipartBody.Part
//        val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
//        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
//
//        // Retrofit 호출
//        val response = NoAuthRetrofitClient.apiService.navGuideMultipart(body)
//        return response.body()
//    }

    // Repository.kt

    suspend fun navGuide(cacheDir: File, bitmap: Bitmap): VisionAnalyzeResponse? {
        // ✅ 1280×720 + 용량 제한 적용 + 필드명 "file"
        val part = buildNavFilePart(cacheDir, bitmap)

        // 인증 필요 없으면 NoAuth 그대로, 필요하면 RetrofitClient.apiService로 바꾸세요.
        val response = NoAuthRetrofitClient.apiService.navGuideMultipart(part)
        return response.body()
    }





    // 토큰 검증 메서드 추가
    private suspend fun ensureValidToken() {
        val token = TokenProvider.token ?: PrefUtil.getJwtToken(AppCtx.app)
        val refreshToken = PrefUtil.getRefreshToken(AppCtx.app)

        if (token.isNullOrEmpty()) {


            // 사용자에게 더 명확한 에러 메시지
            val errorMsg = when {
                token.isNullOrEmpty() && refreshToken.isNullOrEmpty() ->
                    "로그인이 필요합니다. 구글 계정으로 다시 로그인해주세요."
                token.isNullOrEmpty() && !refreshToken.isNullOrEmpty() ->
                    "세션이 만료되었습니다. 잠시 후 다시 시도해주세요."
                else -> "인증 오류가 발생했습니다."
            }

            RetrofitClient.authListener?.onLogout()
            throw IllegalStateException(errorMsg)
        } else {

        }
    }

}

    /* ---------------- 공통 ---------------- */
private fun <T> Response<T>.bodyOrThrow(): T {
    if (isSuccessful) return body() ?: error("Empty body")
    error("HTTP ${code()} ${message()}")
}
