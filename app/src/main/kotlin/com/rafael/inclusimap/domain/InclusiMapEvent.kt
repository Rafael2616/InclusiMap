package com.rafael.inclusimap.domain

import com.google.android.gms.maps.model.LatLng

sealed interface InclusiMapEvent {
    data class UpdateMapCameraPosition(val latLng: LatLng, val isMyLocationFounded: Boolean) : InclusiMapEvent
    data object OnMapLoaded : InclusiMapEvent
    data class OnMappedPlaceSelected(val place: AccessibleLocalMarker) : InclusiMapEvent
    data class OnUnmappedPlaceSelected(val latLng: LatLng) : InclusiMapEvent
    data class OnAddNewMappedPlace(val newPlace: AccessibleLocalMarker) : InclusiMapEvent
    data class SetLocationPermissionGranted(val isGranted: Boolean) : InclusiMapEvent
    data class OnUpdateMappedPlace(val placeUpdated: AccessibleLocalMarker) : InclusiMapEvent
    data class OnDeleteMappedPlace(val placeId: String) : InclusiMapEvent
}
