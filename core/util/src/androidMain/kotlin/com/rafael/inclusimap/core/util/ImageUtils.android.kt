package com.rafael.inclusimap.core.util

import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.scale
import java.io.ByteArrayOutputStream
import org.jetbrains.compose.resources.decodeToImageBitmap

actual fun compressByteArray(image: ByteArray): ByteArray = image.decodeToImageBitmap().asAndroidBitmap().let { bitmap ->
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
    scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)

    baos.toByteArray()
}


private fun rotateBitmap(
    source: ByteArray,
    angle: Float,
): ImageBitmap {
    val matrix = Matrix().apply { postRotate(angle) }
    val sourceBitmap = source.decodeToImageBitmap().asAndroidBitmap()
    return Bitmap.createBitmap(
        sourceBitmap,
        0,
        0,
        sourceBitmap.width,
        sourceBitmap.height,
        matrix,
        true,
    ).asImageBitmap()
}
