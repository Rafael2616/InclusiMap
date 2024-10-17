package com.rafael.inclusimap.feature.map.domain

import android.content.Context
import android.net.Uri
import com.rafael.inclusimap.core.domain.model.AccessibleLocalMarker
import com.rafael.inclusimap.core.domain.model.PlaceImage

sealed interface PlaceDetailsEvent {
    data class OnUploadPlaceImages(val uris: List<Uri>, val context: Context, val imageFolderId: String?, val placeId: String) : PlaceDetailsEvent
    data class OnDeletePlaceImage(val image: PlaceImage) : PlaceDetailsEvent
    data object OnDestroyPlaceDetails : PlaceDetailsEvent
    data class SetCurrentPlace(val place: AccessibleLocalMarker) : PlaceDetailsEvent
    data class SetUserAccessibilityRate(val rate: Int) : PlaceDetailsEvent
    data object OnSendComment : PlaceDetailsEvent
    data object OnDeleteComment : PlaceDetailsEvent
    data class SetUserComment(val comment: String) : PlaceDetailsEvent
    data class SetIsUserCommented(val isCommented: Boolean) : PlaceDetailsEvent
    data class SetIsEditingPlace(val isEditing: Boolean) : PlaceDetailsEvent
}
