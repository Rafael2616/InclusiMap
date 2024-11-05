package com.rafael.inclusimap.core.domain.util

import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.exifinterface.media.ExifInterface
import java.io.ByteArrayOutputStream

fun rotateImage(
    bitmap: Bitmap,
    orientation: Int,
): Bitmap {
    var rotatedBitmap = bitmap

    when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> rotatedBitmap = rotateBitmap(bitmap, 90f)
        ExifInterface.ORIENTATION_ROTATE_180 -> rotatedBitmap = rotateBitmap(bitmap, 180f)
        ExifInterface.ORIENTATION_ROTATE_270 -> rotatedBitmap = rotateBitmap(bitmap, 270f)
    }

    return rotatedBitmap
}

private fun rotateBitmap(
    source: Bitmap,
    angle: Float,
): Bitmap {
    val matrix = Matrix().apply { postRotate(angle) }
    return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
}

fun resizedImageAsByteArrayOS(image: ImageBitmap): ByteArrayOutputStream =
    image.asAndroidBitmap().let { bitmap ->
        val maxSize = 1024
        val width = bitmap.width
        val height = bitmap.height
        val scale =
            if (width > height) {
                maxSize.toFloat() / width
            } else {
                maxSize.toFloat() / height
            }

        val scaledBitmap =
            Bitmap.createScaledBitmap(
                bitmap,
                (width * scale).toInt(),
                (height * scale).toInt(),
                true,
            )

        val baos = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, baos)

        baos
    }
