package com.ranveeraggarwal.macrolens.photo

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.math.max
import kotlin.math.roundToInt

/** A photo shrunk down for display and upload. */
class DownscaledPhoto(
    val bitmap: Bitmap,
    val jpegBytes: ByteArray,
)

/**
 * Shrinks a full-size camera photo so that its long edge is at most [maxLongEdgePx].
 * Smaller uploads are faster and the model doesn't need more detail than that.
 */
class PhotoDownscaler(
    private val maxLongEdgePx: Int = 1280,
    private val jpegQuality: Int = 85,
) {

    suspend fun downscale(photoFile: File): DownscaledPhoto = withContext(Dispatchers.Default) {
        val bitmap = decodeScaled(photoFile)
        DownscaledPhoto(bitmap = bitmap, jpegBytes = encodeJpeg(bitmap))
    }

    /** ImageDecoder also applies the EXIF rotation, so the result is upright. */
    private fun decodeScaled(photoFile: File): Bitmap {
        val source = ImageDecoder.createSource(photoFile)
        return ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
            val scale = scaleFactor(info.size.width, info.size.height)
            decoder.setTargetSize(
                max(1, (info.size.width * scale).roundToInt()),
                max(1, (info.size.height * scale).roundToInt()),
            )
            // Software bitmaps can always be compressed; hardware ones may not be.
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
        }
    }

    /** 1.0 for photos that are already small enough; we never upscale. */
    private fun scaleFactor(width: Int, height: Int): Float {
        val longEdge = max(width, height)
        return if (longEdge <= maxLongEdgePx) 1f else maxLongEdgePx.toFloat() / longEdge
    }

    private fun encodeJpeg(bitmap: Bitmap): ByteArray {
        val output = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, jpegQuality, output)
        return output.toByteArray()
    }
}
