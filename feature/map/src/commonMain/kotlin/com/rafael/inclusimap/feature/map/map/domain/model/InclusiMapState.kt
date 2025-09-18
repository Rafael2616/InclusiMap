package com.rafael.inclusimap.feature.map.map.domain.model

import com.rafael.inclusimap.core.util.map.model.AccessibleLocalMarker
import com.rafael.inclusimap.feature.map.map.domain.model.MapConstants.PARAGOMINAS_LAT_LNG
import com.rafael.libs.maps.interop.model.MapsCameraPosition
import com.rafael.libs.maps.interop.model.MapsLatLng

data class InclusiMapState(
    val isMyLocationFound: Boolean = false,
    val isMapLoaded: Boolean = false,
    val shouldAnimateMap: Boolean = true,
    val isLocationPermissionGranted: Boolean = false,
    val selectedUnmappedPlaceLatLng: MapsLatLng? = null,
    val selectedMappedPlace: AccessibleLocalMarker? = null,
    val defaultLocationLatLng: MapsLatLng = PARAGOMINAS_LAT_LNG,
    val currentLocation: MapsCameraPosition? = null,
    val allMappedPlaces: List<AccessibleLocalMarker> = emptyList(),
    val failedToLoadPlaces: Boolean = false,
    val failedToGetNewPlaces: Boolean = false,
    val failedToConnectToServer: Boolean = false,
    val useAppWithoutInternet: Boolean = false,
    val shouldTravel: Boolean = false,
    var isTraveling: Boolean = false,
    val isAddingNewPlace: Boolean = false,
    val isErrorAddingNewPlace: Boolean = false,
    val isPlaceAdded: Boolean = false,
    val isDeletingPlace: Boolean = false,
    val isErrorDeletingPlace: Boolean = false,
    val isPlaceDeleted: Boolean = false,
    val isContributionsScreen: Boolean = false,
    val isStateRestored: Boolean = false,
)
