package com.rafael.inclusimap.domain

import androidx.compose.ui.graphics.ImageBitmap
import com.google.maps.android.compose.MarkerState

data class AccessibleLocalMarker(
    val markerState: MarkerState,
    val title: String,
    val description: String,
    val icon: ImageBitmap,
)