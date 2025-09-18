package com.rafael.inclusimap.core.util.map.model

import kotlinx.serialization.Serializable

@Serializable
data class Location(
    val lat: Double,
    val lng: Double,
    val placeId: String,
)
