// app/src/main/java/com/example/lookey/data/network/Repository.kt
package com.example.lookey.data.network

import android.graphics.Bitmap
import com.example.lookey.data.model.ApiResponse
import com.example.lookey.data.model.LoginResponse
import com.example.lookey.data.remote.dto.LocationSearchResult
import com.example.lookey.data.remote.dto.ShelfSearchResult
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
    /** PRODUCT-005: 4장 업로드 */
    suspend fun productShelfSearch(
        cacheDir: File,
        frames: List<Bitmap>
    ): ApiResponse<ShelfSearchResult> {
        require(frames.size == 4) { "PRODUCT-005 requires exactly 4 images" }
        val parts = frames.mapIndexed { i, bmp ->
            val file = bmp.toTempJpeg(cacheDir, "shelf_${i+1}", 800, 600, 80)
            MultipartBody.Part.createFormData(
                "shelf_images",
                file.name,
                file.asRequestBody("image/jpeg".toMediaType())
            )
        }
        return api.searchShelf(parts).bodyOrThrow()
    }

    /** PRODUCT-006: 현재 화면 + 상품명 */
    suspend fun productLocation(
        cacheDir: File,
        frame: Bitmap,
        productName: String
    ): ApiResponse<LocationSearchResult> {
        val file = frame.toTempJpeg(cacheDir, "current_frame", 800, 600, 80)
        val framePart = MultipartBody.Part.createFormData(
            "current_frame", file.name, file.asRequestBody("image/jpeg".toMediaType())
        )
        val namePart = productName.toRequestBody("text/plain".toMediaType())

        return api.searchProductLocation(framePart, namePart).bodyOrThrow()
    }
}
/* ---------------- 유틸 ---------------- */

private fun <T> Response<T>.bodyOrThrow(): T {
    if (isSuccessful) return body() ?: error("Empty body")
    error("HTTP ${code()} ${message()}")
}

private fun Bitmap.toTempJpeg(
    dir: File,
    baseName: String,
    maxW: Int,
    maxH: Int,
    quality: Int
): File {
    val ratio = minOf(maxW.toFloat() / width, maxH.toFloat() / height, 1f)
    val w = (width * ratio).toInt()
    val h = (height * ratio).toInt()
    val scaled = if (ratio < 1f) Bitmap.createScaledBitmap(this, w, h, true) else this

    val bos = ByteArrayOutputStream()
    scaled.compress(Bitmap.CompressFormat.JPEG, quality, bos)
    val bytes = bos.toByteArray()

    return File.createTempFile(baseName, ".jpg", dir).apply {
        outputStream().use { it.write(bytes) }
    }
}