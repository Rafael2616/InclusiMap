package com.rafael.inclusimap.feature.map.placedetails.domain.model

import android.content.Context
import android.net.Uri
import com.google.android.gms.maps.model.LatLng
import com.rafael.inclusimap.core.domain.model.AccessibleLocalMarker
import com.rafael.inclusimap.core.domain.model.PlaceImage
import com.rafael.inclusimap.core.domain.model.Resource

sealed interface PlaceDetailsEvent {
    data class OnUploadPlaceImages(val uris: List<Uri>, val context: Context, val placeId: String) : PlaceDetailsEvent
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
    data class GetCurrentNearestPlaceUri(val latLng: LatLng) : PlaceDetailsEvent
}
