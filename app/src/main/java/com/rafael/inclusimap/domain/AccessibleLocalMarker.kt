package com.rafael.inclusimap.domain

import com.google.maps.android.compose.MarkerState

data class AccessibleLocalMarker(
    val markerState: MarkerState,
    val title: String,
    val description: String,
    val isAccessible: Boolean,
    val comments: List<Comment>? = null
)
