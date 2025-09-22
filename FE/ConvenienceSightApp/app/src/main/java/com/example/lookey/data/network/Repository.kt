// app/src/main/java/com/example/lookey/data/network/Repository.kt
package com.example.lookey.data.network

import android.graphics.Bitmap
import android.util.Log
import com.example.lookey.data.model.ApiResponse
import com.example.lookey.data.model.LoginResponse
import com.example.lookey.data.remote.dto.navigation.VisionAnalyzeResponse
import com.example.lookey.data.remote.dto.product.LocationSearchResult
import com.example.lookey.data.remote.dto.product.ShelfSearchResult
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

    // ============= 상품 인식 =============
    // 005: 그대로(파일 1장, 800x600, ≤1MB)
    suspend fun productShelfSearch(cacheDir: File, frame: Bitmap)
            : ApiResponse<ShelfSearchResult> {
        val part = buildShelfImagePart(cacheDir, frame)
        return api.searchShelf(part).bodyOrThrow()
    }

    // 006: JSON 우선, 실패 시 멀티파트로 폴백(둘 다 구현돼 있을 때)
    suspend fun productLocation(cacheDir: File, frame: Bitmap, productName: String)
            : ApiResponse<LocationSearchResult> {
        // JSON (Base64)
        val b64 = frame.toBase64Jpeg(800, 600, 80)
        val r1 = api.searchProductLocationJson(productName, mapOf("current_frame" to b64))
        if (r1.isSuccessful) return r1.bodyOrThrow()

        // 혹시 서버가 멀티파트만 허용한다면 폴백
        val img = buildCurrentFramePart(cacheDir, frame)
        val r2 = api.searchProductLocation(img, productName)
        return r2.bodyOrThrow()
    }

    // AI-001:
    suspend fun navGuide(cacheDir: File, bitmap: Bitmap): VisionAnalyzeResponse? {
        // Bitmap → File
        val file = File(cacheDir, "nav_image.jpg").apply {
            outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 50, it) }
        }

        // 📌 파일 크기 확인 (추가 부분)
        val fileSizeInKB = file.length() / 1024
        val fileSizeInMB = fileSizeInKB / 1024
        Log.d("UploadImage", "Image size before upload: ${fileSizeInKB}KB (${fileSizeInMB}MB)")

        // File → MultipartBody.Part
        val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

        // Retrofit 호출
        val response = NoAuthRetrofitClient.apiService.navGuideMultipart(body)
        return response.body()
    }




}

    /* ---------------- 공통 ---------------- */
private fun <T> Response<T>.bodyOrThrow(): T {
    if (isSuccessful) return body() ?: error("Empty body")
    error("HTTP ${code()} ${message()}")
}