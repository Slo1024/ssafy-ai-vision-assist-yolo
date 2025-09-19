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
    /** PRODUCT-005: 매대 사진 1장 업로드 */
    suspend fun productShelfSearch(
        cacheDir: File,
        frame: Bitmap
    ): ApiResponse<ShelfSearchResult> {
        // ImageParts.kt의 빌더 사용 (JPEG 800x600, 품질 80)
        val part: MultipartBody.Part = buildShelfImagePart(cacheDir, frame)
        return api.searchShelf(part).bodyOrThrow()
    }

    /** PRODUCT-006: 현재 화면 1장 + 상품명 */
    suspend fun productLocation(
        cacheDir: File,
        frame: Bitmap,
        productName: String
    ): ApiResponse<LocationSearchResult> {
        val img  = buildCurrentFramePart(cacheDir, frame) // JPEG 800x600, 품질 80
        val name = buildTextPart(productName)
        return api.searchProductLocation(img, name).bodyOrThrow()
    }

    // ============= NAV-001 =============
    /** NAV-001: 현재 위치 이미지 1장 분석 (/api/v1/vision/ai/analyze) */
    suspend fun navGuide(
        cacheDir: File,
        frame: Bitmap
    ): VisionAnalyzeResponse {
        // 권장 1280x960, 품질 80 (4MB 이하로 업로드)
        val part: MultipartBody.Part = buildNavImagePart(cacheDir, frame)
        return api.navGuide(part).bodyOrThrow()
    }
}

/* ---------------- 공통 ---------------- */
private fun <T> Response<T>.bodyOrThrow(): T {
    if (isSuccessful) return body() ?: error("Empty body")
    error("HTTP ${code()} ${message()}")
}