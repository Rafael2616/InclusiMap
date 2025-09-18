package com.rafael.inclusimap.feature.map.map.domain.model

import com.rafael.inclusimap.core.util.map.model.AccessibleLocalMarker
import com.rafael.libs.maps.interop.model.MapsCameraPosition
import com.rafael.libs.maps.interop.model.MapsLatLng

sealed interface InclusiMapEvent {
    data object OnLoadPlaces : InclusiMapEvent
    data class OnMappedPlaceSelected(val place: AccessibleLocalMarker) : InclusiMapEvent
    data class OnUnmappedPlaceSelected(val latLng: MapsLatLng) : InclusiMapEvent
    data class OnAddNewMappedPlace(val newPlace: AccessibleLocalMarker) : InclusiMapEvent
    data class SetLocationPermissionGranted(val isGranted: Boolean) : InclusiMapEvent
    data class OnUpdateMappedPlace(val placeUpdated: AccessibleLocalMarker) : InclusiMapEvent
    data class OnDeleteMappedPlace(val placeId: String) : InclusiMapEvent
    data class OnFailToLoadPlaces(val isFailed: Boolean) : InclusiMapEvent
    data class OnFailToConnectToServer(val isFailed: Boolean) : InclusiMapEvent
    data object OnMapLoad : InclusiMapEvent
    data object LoadCachedPlaces : InclusiMapEvent
    data object UseAppWithoutInternet : InclusiMapEvent
    data class ShouldAnimateMap(val shouldAnimate: Boolean) : InclusiMapEvent
    data class UpdateMapState(val mapState: MapsCameraPosition) : InclusiMapEvent
    data object GetCurrentState : InclusiMapEvent
    data class SetCurrentPlaceById(val placeId: String) : InclusiMapEvent
    data object ResetState : InclusiMapEvent
    data class OnTravelToPlace(val placeId: String) : InclusiMapEvent
    data class SetShouldTravel(val shouldTravel: Boolean) : InclusiMapEvent
    data class SetIsContributionsScreen(val isContributionsScreen: Boolean) : InclusiMapEvent
}
