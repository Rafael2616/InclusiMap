package com.rafael.inclusimap.domain

import android.content.Context
import android.net.Uri

sealed interface PlaceDetailsEvent {
    data class OnUploadPlaceImages(val uri: Uri, val context: Context) : PlaceDetailsEvent
    data class OnDeletePlaceImage(val image: PlaceImage) : PlaceDetailsEvent
    data object OnDestroyPlaceDetails : PlaceDetailsEvent
    data class SetCurrentPlace(val place: AccessibleLocalMarker) : PlaceDetailsEvent
    data class SetUserAccessibilityRate(val rate: Int) : PlaceDetailsEvent
    data object OnSendComment: PlaceDetailsEvent
    data object OnDeleteComment: PlaceDetailsEvent
    data class SetUserComment(val comment: String) : PlaceDetailsEvent
    data class SetIsUserCommented(val isCommented: Boolean) : PlaceDetailsEvent
}