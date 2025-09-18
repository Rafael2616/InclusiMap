package com.rafael.inclusimap.core.util.map.model

import androidx.compose.ui.graphics.ImageBitmap

data class PlaceImage(
    val userEmail: String?,
    val image: ImageBitmap,
    val placeID: String,
    val name: String,
)
