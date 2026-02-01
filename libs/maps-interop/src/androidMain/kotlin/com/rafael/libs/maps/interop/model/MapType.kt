package com.rafael.libs.maps.interop.model

fun MapType.toMapType() = when (this) {
    MapType.HYBRID -> com.google.maps.android.compose.MapType.HYBRID
    MapType.NORMAL -> com.google.maps.android.compose.MapType.NORMAL
    MapType.SATELLITE -> com.google.maps.android.compose.MapType.SATELLITE
    MapType.TERRAIN -> com.google.maps.android.compose.MapType.TERRAIN
}
