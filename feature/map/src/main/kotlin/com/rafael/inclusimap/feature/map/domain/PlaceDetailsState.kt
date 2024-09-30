package com.rafael.inclusimap.feature.map.domain

import com.google.api.services.drive.model.File
import com.rafael.inclusimap.core.domain.model.AccessibleLocalMarker
import com.rafael.inclusimap.core.domain.model.FullAccessibleLocalMarker
import com.rafael.inclusimap.core.domain.model.PlaceImage

data class PlaceDetailsState(
    val inclusiMapImageRepositoryFolder: List<File> = emptyList(),
    val loadedPlaces : List<FullAccessibleLocalMarker> = emptyList(),
    val isCurrentPlaceLoaded: Boolean = false,
    val currentPlace: AccessibleLocalMarker = AccessibleLocalMarker(),
    val currentPlaceFolderID : String? = null,
    val currentPlaceImagesFolder: List<File> = emptyList(),
    val currentPlaceImages: List<PlaceImage?> = emptyList(),
    val allImagesLoaded: Boolean = false,
    val userComment: String = "",
    val userAccessibilityRate: Int = 0,
    val trySendComment: Boolean = false,
    val isUserCommented: Boolean = false,
    val isEditingPlace: Boolean = false,
)
