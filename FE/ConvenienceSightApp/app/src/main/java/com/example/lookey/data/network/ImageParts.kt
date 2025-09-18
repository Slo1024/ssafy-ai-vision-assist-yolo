package com.example.lookey.data.network

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.File

private val JPEG = "image/jpeg".toMediaType()
private val TEXT = "text/plain".toMediaType()

// 800x600 권장 + 품질 80%
private fun Bitmap.compressToJpegFile(tmpDir: File, name: String): File {
    // 권장 해상도 스케일(선택)
    val targetW = 800
    val targetH = 600
    val scaled = if (width != targetW || height != targetH) {
        Bitmap.createScaledBitmap(this, targetW, targetH, true)
    } else this

    val bos = ByteArrayOutputStream()
    scaled.compress(Bitmap.CompressFormat.JPEG, 80, bos)
    val bytes = bos.toByteArray()

    val file = File.createTempFile(name, ".jpg", tmpDir)
    file.outputStream().use { it.write(bytes) }
    return file
}

// PRODUCT-005: "shelf_images" 4장 파트 생성
fun buildShelfImageParts(cacheDir: File, bitmaps: List<Bitmap>): List<MultipartBody.Part> {
    require(bitmaps.size == 4) { "shelf_images must be exactly 4 images" }
    return bitmaps.mapIndexed { idx, bmp ->
        val f = bmp.compressToJpegFile(cacheDir, "shelf_${idx+1}")
        MultipartBody.Part.createFormData("shelf_images", f.name, f.asRequestBody(JPEG))
    }
}

// PRODUCT-006: "current_frame" 1장 파트
fun buildCurrentFramePart(cacheDir: File, bitmap: Bitmap): MultipartBody.Part {
    val f = bitmap.compressToJpegFile(cacheDir, "current_frame")
    return MultipartBody.Part.createFormData("current_frame", f.name, f.asRequestBody(JPEG))
}

// 텍스트 파트
fun buildTextPart(value: String): RequestBody = value.toRequestBody(TEXT)
