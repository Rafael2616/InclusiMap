package com.rafael.inclusimap.domain.repository

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.MarkerState
import com.rafael.inclusimap.domain.AccessibleLocalMarker

val mappedPlaces = listOf(
    AccessibleLocalMarker(
        markerState = MarkerState(
            position = LatLng(-2.9961928457018034,-47.34839804470539)
        ),
        title = "Lago Verde",
        description = "Ponto Turístico",
        isAccessible = false
    ),
    AccessibleLocalMarker(
        markerState = MarkerState(
            position = LatLng(-2.9751372910178024, -47.360620871186256)
        ),
        title = "Top Alimentos",
        description = "Supermercado",
        isAccessible = true
    ),
    AccessibleLocalMarker(
        markerState = MarkerState(
            position = LatLng(-2.987957301132429,-47.357158809900284)
        ),
        title = "Mix Mateus",
        description = "Supermercado",
        isAccessible = true
    ),
    AccessibleLocalMarker(
        markerState = MarkerState(
            position = LatLng(-2.985339672724703,-47.35865514725447)
        ),
        title = "UEPA",
        description = "Universidade",
        isAccessible = false
    )
)
