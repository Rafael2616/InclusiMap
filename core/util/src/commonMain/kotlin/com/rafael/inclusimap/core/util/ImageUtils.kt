package com.rafael.inclusimap.core.util

import androidx.compose.ui.graphics.ImageBitmap

expect fun resizedImageAsByteArray(image: ImageBitmap): ByteArray

expect fun rotateImage(
    bitmap: ImageBitmap,
    orientation: Int,
): ImageBitmap
