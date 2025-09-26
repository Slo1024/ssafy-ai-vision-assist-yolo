package com.example.lookey.data.network

import android.graphics.Bitmap
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import android.util.Base64

private val JPEG = "image/jpeg".toMediaType()
private val TEXT = "text/plain".toMediaType()

// === 공통 타깃 해상도 (신규 사양) ===
private const val TARGET_W = 1280
private const val TARGET_H = 720
private const val NAV_JPEG_QUALITY = 100

// --------- 공통 Bitmap → JPEG 저장 헬퍼 ---------

private fun Bitmap.scaleTo(width: Int, height: Int): Bitmap =
    if (this.width == width && this.height == height) this
    else Bitmap.createScaledBitmap(this, width, height, true)

/** 정확한 해상도/품질로 JPEG 저장 (정확히 width x height로 맞춤) */
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
    width: Int = TARGET_W,       // ✅ 1280
    height: Int = TARGET_H,      // ✅ 720
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

// (비율 유지 축소 + 용량 제한) — 필요 시 사용
fun Bitmap.toJpegMaxUnderSize(
    tmpDir: File,
    name: String,
    maxW: Int,
    maxH: Int,
    startQuality: Int,
    minQuality: Int,
    maxBytes: Int
): File {
    val scale = minOf(maxW.toFloat() / width, maxH.toFloat() / height, 1f)
    val scaled = if (scale < 1f)
        Bitmap.createScaledBitmap(this, (width * scale).toInt(), (height * scale).toInt(), true)
    else this

    var q = startQuality.coerceIn(1, 100)
    var bytes: ByteArray
    do {
        val bos = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, q, bos)
        bytes = bos.toByteArray()
        if (bytes.size <= maxBytes || q <= minQuality) break
        q -= 5
    } while (true)

    return File.createTempFile(name, ".jpg", tmpDir).apply {
        outputStream().use { it.write(bytes) }
    }
}

fun Bitmap.toBase64Jpeg(width: Int, height: Int, quality: Int): String {
    val scaled = if (this.width == width && this.height == height)
        this else Bitmap.createScaledBitmap(this, width, height, true)
    val bos = ByteArrayOutputStream()
    scaled.compress(Bitmap.CompressFormat.JPEG, quality.coerceIn(1, 100), bos)
    return Base64.encodeToString(bos.toByteArray(), Base64.NO_WRAP)
}




fun buildNavFilePart(cacheDir: File, bmp: Bitmap): MultipartBody.Part {
    // ✅ 정확히 1280×720, 품질 100으로 저장
    val f = bmp.toJpegExact(
        tmpDir = cacheDir,
        name = "nav_image",
        width = TARGET_W,           // 1280
        height = TARGET_H,          // 720
        quality = NAV_JPEG_QUALITY  // 100
    )
    // 필드명은 "file" (ApiService.navGuideMultipart와 매칭)
    return MultipartBody.Part.createFormData("file", f.name, f.asRequestBody(JPEG))
}




// --------- NAV-001 ---------
// part name = "image"
// ✅ 요청에 맞춰 정확히 1280x720, 4MB 이하로 보냄
fun buildNavImagePart(cacheDir: File, bmp: Bitmap): MultipartBody.Part {
    val f = bmp.toJpegUnderSize(
        tmpDir = cacheDir,
        name = "nav_image",
        width = TARGET_W, height = TARGET_H,
        startQuality = 85, minQuality = 60,
        maxBytes = 4_000_000
    )
    return MultipartBody.Part.createFormData("image", f.name, f.asRequestBody(JPEG))
}

// --------- PRODUCT-005 ---------
// 요청 사양: "file" 필드명 사용
// ✅ 1280x720로 상향(기존 800x600 → 1280x720), 1MB 제한 유지
fun buildShelfImagePart(cacheDir: File, bmp: Bitmap): MultipartBody.Part {
    val f = bmp.toJpegUnderSize(
        tmpDir = cacheDir,
        name = "shelf",
        width = TARGET_W,
        height = TARGET_H,
        startQuality = 80,
        minQuality = 70,
        maxBytes = 1_000_000
    )
    return MultipartBody.Part.createFormData("file", f.name, f.asRequestBody(JPEG))
}

/** (구 스펙 호환용) 더 이상 사용하지 마세요. 005는 1장만 허용됩니다. */
@Deprecated("PRODUCT-005 스펙 변경: 1장만 허용됩니다. buildShelfImagePart(...)를 사용하세요.")
fun buildShelfImageParts(cacheDir: File, bitmaps: List<Bitmap>): List<MultipartBody.Part> {
    require(bitmaps.size == 1) { "PRODUCT-005 스펙 변경: shelf_images 는 정확히 1장이어야 합니다." }
    return listOf(buildShelfImagePart(cacheDir, bitmaps.first()))
}

// --------- PRODUCT-006 ---------
// 이미지: "current_frame" 1장
// ✅ 정확히 1280x720, Q=80로 고정
fun buildCurrentFramePart(cacheDir: File, bitmap: Bitmap): MultipartBody.Part {
    val f = bitmap.toJpegExact(cacheDir, "current_frame", TARGET_W, TARGET_H, 80)
    return MultipartBody.Part.createFormData("current_frame", f.name, f.asRequestBody(JPEG))
}

// 텍스트: product_name 등
fun buildTextPart(value: String): RequestBody = value.toRequestBody(TEXT)
