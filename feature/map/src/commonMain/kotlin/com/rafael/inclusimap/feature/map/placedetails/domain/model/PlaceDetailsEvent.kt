package com.rafael.inclusimap.feature.map.placedetails.domain.model

import com.rafael.inclusimap.core.util.map.model.AccessibleLocalMarker
import com.rafael.inclusimap.core.util.map.model.PlaceImage
import com.rafael.inclusimap.core.util.map.model.Resource
import com.rafael.libs.maps.interop.model.MapsLatLng

sealed interface PlaceDetailsEvent {
    data class OnUploadPlaceImages(val imagesContent: List<ByteArray>, val placeId: String) : PlaceDetailsEvent
    data class OnDeletePlaceImage(val image: PlaceImage) : PlaceDetailsEvent
    data object OnDestroyPlaceDetails : PlaceDetailsEvent
    data class SetCurrentPlace(val place: AccessibleLocalMarker) : PlaceDetailsEvent
    data class SetUserAccessibilityRate(val rate: Int) : PlaceDetailsEvent
    data class OnSendComment(val comment: String) : PlaceDetailsEvent
    data object OnDeleteComment : PlaceDetailsEvent
    data class SetIsUserCommented(val isCommented: Boolean) : PlaceDetailsEvent
    data class SetIsTrySendComment(val isTrying: Boolean) : PlaceDetailsEvent
    data class SetIsEditingPlace(val isEditing: Boolean) : PlaceDetailsEvent
    data class SetIsEditingComment(val isEditing: Boolean) : PlaceDetailsEvent
    data class OnUpdatePlaceAccessibilityResources(val resources: List<Resource>) : PlaceDetailsEvent
    data class GetCurrentNearestPlaceUri(val latLng: MapsLatLng) : PlaceDetailsEvent
}
