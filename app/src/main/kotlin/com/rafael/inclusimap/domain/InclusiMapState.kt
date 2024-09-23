package com.rafael.inclusimap.domain

import com.google.android.gms.maps.model.LatLng
import com.rafael.inclusimap.data.repository.mappedPlaces

data class InclusiMapState(
    val isMyLocationFound: Boolean = false,
    val isMapLoaded: Boolean = false,
    val isLocationPermissionGranted: Boolean = false,
    val selectedUnmappedPlaceLatLng: LatLng? = null,
    val selectedMappedPlace: AccessibleLocalMarker? = null,
    val defaultLocationLatLng: LatLng = PARAGOMINAS_LAT_LNG,
    val allMappedPlaces: List<AccessibleLocalMarker> = mappedPlaces,
)

val PARAGOMINAS_LAT_LNG = LatLng(-2.98, -47.35)
