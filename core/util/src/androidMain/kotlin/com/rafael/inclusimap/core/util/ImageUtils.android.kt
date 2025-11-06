package com.rafael.inclusimap.core.util

import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.scale
import java.io.ByteArrayOutputStream

actual fun resizedImageAsByteArray(image: ImageBitmap): ByteArray = image.asAndroidBitmap().let { bitmap ->
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
        bitmap.scale((width * scale).toInt(), (height * scale).toInt())

    val baos = ByteArrayOutputStream()
    scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, baos)

    baos.toByteArray()
}


private fun rotateBitmap(
    source: ImageBitmap,
    angle: Float,
): ImageBitmap {
    val matrix = Matrix().apply { postRotate(angle) }
    return Bitmap.createBitmap(
        source.asAndroidBitmap(),
        0,
        0,
        source.width,
        source.height,
        matrix,
        true,
    ).asImageBitmap()
}
