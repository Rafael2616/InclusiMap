package com.rafael.inclusimap.domain

import android.content.Context
import android.net.Uri

sealed interface PlaceDetailsEvent {
    data class OnUploadPlaceImages(val uri: Uri, val context: Context) : PlaceDetailsEvent
    data object OnDetroyPlaceDetails : PlaceDetailsEvent
    data class SetCurrentPlace(val place: AccessibleLocalMarker) : PlaceDetailsEvent
}