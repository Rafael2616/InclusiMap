package com.rafael.inclusimap.feature.map.domain

import com.google.android.gms.maps.model.LatLng
import com.rafael.inclusimap.core.domain.model.AccessibleLocalMarker

sealed interface InclusiMapEvent {
    data class UpdateMapCameraPosition(val latLng: LatLng, val isMyLocationFounded: Boolean) : InclusiMapEvent
    data object OnLoadPlaces : InclusiMapEvent
    data class OnMappedPlaceSelected(val place: AccessibleLocalMarker) : InclusiMapEvent
    data class OnUnmappedPlaceSelected(val latLng: LatLng) : InclusiMapEvent
    data class OnAddNewMappedPlace(val newPlace: AccessibleLocalMarker) : InclusiMapEvent
    data class SetLocationPermissionGranted(val isGranted: Boolean) : InclusiMapEvent
    data class OnUpdateMappedPlace(val placeUpdated: AccessibleLocalMarker) : InclusiMapEvent
    data class OnDeleteMappedPlace(val placeId: String) : InclusiMapEvent
    data class OnFailToLoadPlaces(val isFailed: Boolean) : InclusiMapEvent
    data class OnFailToConnectToServer(val isFailed: Boolean) : InclusiMapEvent
    data object OnMapLoad : InclusiMapEvent
}
