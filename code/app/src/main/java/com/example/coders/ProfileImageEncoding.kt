/**
 * Encodes selected profile photos into bounded JPEG data URLs and decodes stored data URLs for
 * Compose rendering.
 *
 * Outstanding issues: Very detailed source images may be rejected after the documented quality
 * retries so that a Firestore image record stays below the configured size limit.
 */

package com.example.coders

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.Base64
import kotlin.math.max
import kotlin.math.roundToInt

// The following block was added using OpenAI, Codex, "Implement US 01.08.02: add, update,
// and remove profile images stored as Firestore data URLs", 2026-11-22.
private const val MAX_PROFILE_IMAGE_SIDE_PX = 512
private const val MAX_PROFILE_IMAGE_DATA_URL_LENGTH = 900_000
private val profileImageQualities = intArrayOf(80, 70, 60, 50)
private val imageDataUrlPattern = Regex("^data:image/[^;,]+;base64,([A-Za-z0-9+/]*={0,2})$")

// The following function was added using OpenAI, Codex, "Implement US 01.08.02: add, update,
// and remove profile images stored as Firestore data URLs", 2026-11-22.
/**
 * Converts JPEG bytes into a Firestore-storable data URL.
 *
 * @param bytes The JPEG bytes to encode.
 * @return A `data:image/jpeg;base64,` URL containing [bytes].
 */
fun jpegBytesToDataUrl(bytes: ByteArray): String =
    "data:image/jpeg;base64,${Base64.getEncoder().encodeToString(bytes)}"

// The following function was added using OpenAI, Codex, "Implement US 01.08.02: add, update,
// and remove profile images stored as Firestore data URLs", 2026-11-22.
/**
 * Decodes an image data URL into its raw bytes.
 *
 * @param dataUrl The candidate `data:image/...;base64,` URL.
 * @return The decoded bytes, or `null` when [dataUrl] is not a valid image data URL.
 */
fun dataUrlToBytes(dataUrl: String): ByteArray? {
    val match = imageDataUrlPattern.matchEntire(dataUrl) ?: return null
    return try {
        Base64.getDecoder().decode(match.groupValues[1])
    } catch (_: IllegalArgumentException) {
        null
    }
}

// The following function was added using OpenAI, Codex, "Implement US 01.08.02: add, update,
// and remove profile images stored as Firestore data URLs", 2026-11-22.
/**
 * Encodes a selected image as a bounded JPEG data URL.
 *
 * The image is rotated by [ImageDecoder] where available, scaled so that its longest side is at
 * most 512 px, then compressed at progressively lower JPEG qualities until it fits in Firestore.
 *
 * @param context The context used to open [uri].
 * @param uri The image selected through the system photo picker.
 * @return A JPEG data URL shorter than 900,000 characters.
 * @throws IllegalArgumentException When the selected image cannot be read or encoded small enough.
 */
suspend fun encodeProfileImage(context: Context, uri: Uri): String = withContext(Dispatchers.IO) {
    val bitmap = decodeAndScaleProfileImage(context, uri)
    try {
        profileImageQualities.forEach { quality ->
            val output = ByteArrayOutputStream()
            if (!bitmap.compress(Bitmap.CompressFormat.JPEG, quality, output)) {
                throw IllegalArgumentException("The selected image could not be encoded as JPEG.")
            }
            val dataUrl = jpegBytesToDataUrl(output.toByteArray())
            if (dataUrl.length < MAX_PROFILE_IMAGE_DATA_URL_LENGTH) {
                return@withContext dataUrl
            }
        }
    } finally {
        bitmap.recycle()
    }

    throw IllegalArgumentException(
        "The selected image is too large after compression. Please choose a smaller image."
    )
}

// The following function was added using OpenAI, Codex, "Implement US 01.08.02: add, update,
// and remove profile images stored as Firestore data URLs", 2026-11-22.
/**
 * Decodes an image data URL for Compose rendering.
 *
 * @param dataUrl The stored image data URL.
 * @return The decoded image, or `null` if decoding fails.
 */
fun dataUrlToImageBitmap(dataUrl: String): ImageBitmap? {
    val bytes = dataUrlToBytes(dataUrl) ?: return null
    return try {
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
    } catch (_: Exception) {
        null
    }
}

// The following function was added using OpenAI, Codex, "Implement US 01.08.02: add, update,
// and remove profile images stored as Firestore data URLs", 2026-11-22.
/**
 * Decodes and scales a photo according to the device API level.
 *
 * @param context The context used to open [uri].
 * @param uri The selected image URI.
 * @return A software bitmap whose longest side is at most 512 px.
 */
private fun decodeAndScaleProfileImage(context: Context, uri: Uri): Bitmap =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val source = ImageDecoder.createSource(context.contentResolver, uri)
        ImageDecoder.decodeBitmap(source) { decoder, imageInfo, _ ->
            val size = imageInfo.size
            val scale = minOf(
                1f,
                MAX_PROFILE_IMAGE_SIDE_PX.toFloat() / max(size.width, size.height).toFloat()
            )
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            decoder.setTargetSize(
                max(1, (size.width * scale).roundToInt()),
                max(1, (size.height * scale).roundToInt())
            )
        }
    } else {
        decodeLegacyProfileImage(context, uri)
    }

// The following function was added using OpenAI, Codex, "Implement US 01.08.02: add, update,
// and remove profile images stored as Firestore data URLs", 2026-11-22.
/**
 * Decodes and scales a selected photo on Android API 26 and 27.
 *
 * @param context The context used to open [uri].
 * @param uri The selected image URI.
 * @return A bitmap whose longest side is at most 512 px.
 * @throws IllegalArgumentException When the selected image cannot be decoded.
 */
private fun decodeLegacyProfileImage(context: Context, uri: Uri): Bitmap {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    context.contentResolver.openInputStream(uri)?.use { input ->
        BitmapFactory.decodeStream(input, null, bounds)
    } ?: throw IllegalArgumentException("The selected image could not be read.")

    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
        throw IllegalArgumentException("The selected file is not a supported image.")
    }

    val options = BitmapFactory.Options().apply {
        inSampleSize = profileImageSampleSize(bounds.outWidth, bounds.outHeight)
        inPreferredConfig = Bitmap.Config.ARGB_8888
    }
    val decoded = context.contentResolver.openInputStream(uri)?.use { input ->
        BitmapFactory.decodeStream(input, null, options)
    } ?: throw IllegalArgumentException("The selected image could not be decoded.")

    return scaleProfileBitmap(decoded)
}

// The following function was added using OpenAI, Codex, "Implement US 01.08.02: add, update,
// and remove profile images stored as Firestore data URLs", 2026-11-22.
/**
 * Calculates a power-of-two sample size for legacy bitmap decoding.
 *
 * @param width The source image width in pixels.
 * @param height The source image height in pixels.
 * @return The requested BitmapFactory sample size.
 */
private fun profileImageSampleSize(width: Int, height: Int): Int {
    var sampleSize = 1
    while (max(width, height) / (sampleSize * 2) >= MAX_PROFILE_IMAGE_SIDE_PX) {
        sampleSize *= 2
    }
    return sampleSize
}

// The following function was added using OpenAI, Codex, "Implement US 01.08.02: add, update,
// and remove profile images stored as Firestore data URLs", 2026-11-22.
/**
 * Scales a bitmap down so its longest side is at most 512 px.
 *
 * @param bitmap The decoded source bitmap.
 * @return [bitmap] when it already fits, otherwise a scaled copy.
 */
private fun scaleProfileBitmap(bitmap: Bitmap): Bitmap {
    val longestSide = max(bitmap.width, bitmap.height)
    if (longestSide <= MAX_PROFILE_IMAGE_SIDE_PX) return bitmap

    val scale = MAX_PROFILE_IMAGE_SIDE_PX.toFloat() / longestSide.toFloat()
    val scaled = Bitmap.createScaledBitmap(
        bitmap,
        max(1, (bitmap.width * scale).roundToInt()),
        max(1, (bitmap.height * scale).roundToInt()),
        true
    )
    if (scaled !== bitmap) {
        bitmap.recycle()
    }
    return scaled
}
