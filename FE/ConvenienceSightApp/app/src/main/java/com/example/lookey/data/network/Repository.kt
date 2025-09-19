// app/src/main/java/com/example/lookey/data/network/Repository.kt
package com.example.lookey.data.network

import android.graphics.Bitmap
import com.example.lookey.data.model.ApiResponse
import com.example.lookey.data.model.LoginResponse
import com.example.lookey.data.remote.dto.navigation.VisionAnalyzeResponse
import com.example.lookey.data.remote.dto.product.LocationSearchResult
import com.example.lookey.data.remote.dto.product.ShelfSearchResult
import okhttp3.MultipartBody
import retrofit2.Response
import java.io.File


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

    // AI-001: JSON 우선
    suspend fun navGuide(cacheDir: File, frame: Bitmap): VisionAnalyzeResponse {
        val b64 = frame.toBase64Jpeg(800, 600, 80)  // 서버가 해상도 제한 없다면 800x600 유지로 충분
        val r1 = api.navGuideJson(mapOf("file" to b64))
        if (r1.isSuccessful) return r1.bodyOrThrow()

        // 서버가 멀티파트 허용할 때를 대비한 폴백
        val part = buildNavImagePart(cacheDir, frame)
        val r2 = api.navGuide(part)
        return r2.bodyOrThrow()
    }

}

    /* ---------------- 공통 ---------------- */
private fun <T> Response<T>.bodyOrThrow(): T {
    if (isSuccessful) return body() ?: error("Empty body")
    error("HTTP ${code()} ${message()}")
}