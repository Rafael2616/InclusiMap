package com.rafael.inclusimap.core.navigation.types

import kotlinx.serialization.Serializable

@Serializable
data class Location(
    val lat: Double,
    val lng: Double,
    val placeId: String,
)
