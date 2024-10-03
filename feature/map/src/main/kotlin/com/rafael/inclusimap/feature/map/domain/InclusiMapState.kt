package com.rafael.inclusimap.feature.map.domain

import com.google.android.gms.maps.model.LatLng
import com.rafael.inclusimap.core.domain.model.AccessibleLocalMarker
import com.rafael.inclusimap.core.domain.util.Constants.PARAGOMINAS_LAT_LNG

data class InclusiMapState(
    val isMyLocationFound: Boolean = false,
    val isMapLoaded: Boolean = false,
    val isLocationPermissionGranted: Boolean = false,
    val selectedUnmappedPlaceLatLng: LatLng? = null,
    val selectedMappedPlace: AccessibleLocalMarker? = null,
    val defaultLocationLatLng: LatLng = PARAGOMINAS_LAT_LNG,
    val allMappedPlaces: List<AccessibleLocalMarker> = emptyList(),
    val failedToLoadPlaces: Boolean = false,
    val failedToConnectToServer: Boolean = false,
)
