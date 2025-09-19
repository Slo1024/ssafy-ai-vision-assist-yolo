// app/src/main/java/com/example/lookey/data/network/Repository.kt
package com.example.lookey.data.network

import android.graphics.Bitmap
import com.example.lookey.data.model.ApiResponse
import com.example.lookey.data.model.LoginResponse
import com.example.lookey.data.remote.dto.navigation.NavResult
import com.example.lookey.data.remote.dto.product.LocationSearchResult
import com.example.lookey.data.remote.dto.product.ShelfSearchResult
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import okhttp3.RequestBody.Companion.toRequestBody


class Repository {
    private val api = RetrofitClient.apiService

    // ============= 회원 =============
    suspend fun googleAuth(idToken: String): Response<LoginResponse> {
        return api.googleLogin("Bearer $idToken")
    }

    // ============= 상품 인식 =============
    /** PRODUCT-005: 1장 업로드 */
    suspend fun productShelfSearch(
        cacheDir: File,
        frame: Bitmap
    ): ApiResponse<ShelfSearchResult> {
        // 명세: JPEG 800x600, 75~80%
        val file = frame.toJpegExact(cacheDir, "shelf_1", 800, 600, 80)
        val part = MultipartBody.Part.createFormData(
            "shelf_images", file.name, file.asRequestBody("image/jpeg".toMediaType())
        )
        return api.searchShelf(part).bodyOrThrow()
    }

    /** PRODUCT-006: 1장 + 상품명 */
    suspend fun productLocation(
        cacheDir: File,
        frame: Bitmap,
        productName: String
    ): ApiResponse<LocationSearchResult> {
        val file = frame.toJpegExact(cacheDir, "current_frame", 800, 600, 80)
        val framePart = MultipartBody.Part.createFormData(
            "current_frame", file.name, file.asRequestBody("image/jpeg".toMediaType())
        )
        val namePart = productName.toRequestBody("text/plain".toMediaType())
        return api.searchProductLocation(framePart, namePart).bodyOrThrow()
    }

    /** NAV-001: 1장 업로드 (경로 변경 반영) */
    suspend fun navGuide(
        cacheDir: File,
        frame: Bitmap
    ): ApiResponse<NavResult> {
        val file = frame.toJpegExact(cacheDir, "vision_image", 800, 600, 80)
        val part = MultipartBody.Part.createFormData(
            "image", file.name, file.asRequestBody("image/jpeg".toMediaType())
        )
        return api.navGuide(part).bodyOrThrow()
    }
}
/* ---------------- 유틸 ---------------- */

private fun <T> Response<T>.bodyOrThrow(): T {
    if (isSuccessful) return body() ?: error("Empty body")
    error("HTTP ${code()} ${message()}")
}

/** 800x600 “채우기-크롭”으로 정확히 맞춰 JPEG 저장 */
private fun Bitmap.toJpegExact(
    dir: File,
    baseName: String,
    dstW: Int,
    dstH: Int,
    quality: Int
): File {
    val scale = maxOf(dstW.toFloat() / width, dstH.toFloat() / height)
    val w = (width * scale).toInt()
    val h = (height * scale).toInt()
    val scaled = Bitmap.createScaledBitmap(this, w, h, true)
    val x = ((w - dstW) / 2).coerceAtLeast(0)
    val y = ((h - dstH) / 2).coerceAtLeast(0)
    val cropped = Bitmap.createBitmap(scaled, x, y, dstW, dstH)

    val bos = ByteArrayOutputStream()
    cropped.compress(Bitmap.CompressFormat.JPEG, quality, bos)
    val bytes = bos.toByteArray()

    return File.createTempFile(baseName, ".jpg", dir).apply {
        outputStream().use { it.write(bytes) }
    }
}