package com.example.lookey.data.network

import android.graphics.Bitmap
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.File

private val JPEG = "image/jpeg".toMediaType()
private val TEXT = "text/plain".toMediaType()

// --------- 공통 Bitmap → JPEG 저장 헬퍼 ---------

private fun Bitmap.scaleTo(width: Int, height: Int): Bitmap =
    if (this.width == width && this.height == height) this
    else Bitmap.createScaledBitmap(this, width, height, true)

/** 정확한 해상도/품질로 JPEG 저장 */
fun Bitmap.toJpegExact(
    tmpDir: File,
    name: String,
    width: Int,
    height: Int,
    quality: Int
): File {
    val scaled = scaleTo(width, height)
    val bos = ByteArrayOutputStream()
    scaled.compress(Bitmap.CompressFormat.JPEG, quality.coerceIn(1, 100), bos)
    val file = File.createTempFile(name, ".jpg", tmpDir)
    file.outputStream().use { it.write(bos.toByteArray()) }
    return file
}

/** 최대 용량(바이트) 이하가 되도록 품질을 낮춰 저장 (해상도는 고정) */
fun Bitmap.toJpegUnderSize(
    tmpDir: File,
    name: String,
    width: Int = 800,
    height: Int = 600,
    startQuality: Int = 80,
    minQuality: Int = 70,
    maxBytes: Int = 1_000_000
): File {
    val scaled = scaleTo(width, height)
    var q = startQuality.coerceIn(1, 100)
    var bytes: ByteArray
    do {
        val bos = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, q, bos)
        bytes = bos.toByteArray()
        if (bytes.size <= maxBytes || q <= minQuality) break
        q -= 5
    } while (true)

    val file = File.createTempFile(name, ".jpg", tmpDir)
    file.outputStream().use { it.write(bytes) }
    return file
}

// --------- NAV-001 ---------
// part name = "image"
fun buildNavImagePart(cacheDir: File, bmp: Bitmap): MultipartBody.Part {
    val f = bmp.toJpegExact(cacheDir, "nav_image", 800, 600, 80)
    return MultipartBody.Part.createFormData("image", f.name, f.asRequestBody(JPEG))
}

// --------- PRODUCT-005 ---------
// 요청 사양: "shelf_images" 정확히 1장 (JPEG 800x600, 품질 75~80, 1MB 이하)
fun buildShelfImagePart(cacheDir: File, bmp: Bitmap): MultipartBody.Part {
    val f = bmp.toJpegUnderSize(
        tmpDir = cacheDir,
        name = "shelf",
        width = 800,
        height = 600,
        startQuality = 80,
        minQuality = 75,
        maxBytes = 1_000_000
    )
    return MultipartBody.Part.createFormData("shelf_images", f.name, f.asRequestBody(JPEG))
}

/** (구 스펙 호환용) 더 이상 사용하지 마세요. 005는 1장만 허용됩니다. */
@Deprecated("PRODUCT-005 스펙 변경: 1장만 허용됩니다. buildShelfImagePart(...)를 사용하세요.")
fun buildShelfImageParts(cacheDir: File, bitmaps: List<Bitmap>): List<MultipartBody.Part> {
    require(bitmaps.size == 1) { "PRODUCT-005 스펙 변경: shelf_images 는 정확히 1장이어야 합니다." }
    return listOf(buildShelfImagePart(cacheDir, bitmaps.first()))
}

// --------- PRODUCT-006 ---------
// 이미지: "current_frame" 1장 (JPEG 800x600, Q=80)
fun buildCurrentFramePart(cacheDir: File, bitmap: Bitmap): MultipartBody.Part {
    val f = bitmap.toJpegExact(cacheDir, "current_frame", 800, 600, 80)
    return MultipartBody.Part.createFormData("current_frame", f.name, f.asRequestBody(JPEG))
}

// 텍스트: product_name 등
fun buildTextPart(value: String): RequestBody = value.toRequestBody(TEXT)
