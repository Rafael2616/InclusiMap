package com.rafael.inclusimap.feature.map.domain

import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.rafael.inclusimap.core.domain.model.AccessibleLocalMarker
import com.rafael.inclusimap.core.domain.util.Constants.PARAGOMINAS_LAT_LNG

data class InclusiMapState(
    val isMyLocationFound: Boolean = false,
    val isMapLoaded: Boolean = false,
    val shouldAnimateMap: Boolean = true,
    val isLocationPermissionGranted: Boolean = false,
    val selectedUnmappedPlaceLatLng: LatLng? = null,
    val selectedMappedPlace: AccessibleLocalMarker? = null,
    val defaultLocationLatLng: LatLng = PARAGOMINAS_LAT_LNG,
    val currentLocation: CameraPosition? = null,
    val allMappedPlaces: List<AccessibleLocalMarker> = emptyList(),
    val failedToLoadPlaces: Boolean = false,
    val failedToGetNewPlaces: Boolean = false,
    val failedToConnectToServer: Boolean = false,
    var useAppWithoutInternet: Boolean = false,
    val userContributions: Contributions = Contributions(),
    var allCommentsContributionsLoaded: Boolean = false,
    val allPlacesContributionsLoaded: Boolean = false,
    val allImagesContributionsLoaded: Boolean = false,
    val isContributionsScreen: Boolean = false,
    val isLoadingContributions: Boolean = false,
    val shouldRefresh: Boolean = false,
    val contributionsSize: ContributionsSize = ContributionsSize(),
)
