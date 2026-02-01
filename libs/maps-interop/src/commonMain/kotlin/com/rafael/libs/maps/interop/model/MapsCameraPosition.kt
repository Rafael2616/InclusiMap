package com.rafael.libs.maps.interop.model

data class MapsCameraPosition(
    val target: MapsLatLng,
    val bearing: Float,
    val tilt: Float,
    val zoom: Float,
)
