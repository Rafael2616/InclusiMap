package com.rafael.inclusimap.feature.map.domain

import com.google.api.services.drive.model.File
import com.rafael.inclusimap.core.domain.model.FullAccessibleLocalMarker

data class PlaceDetailsState(
    val inclusiMapImageRepositoryFolder: List<File> = emptyList(),
    val loadedPlaces: List<FullAccessibleLocalMarker> = emptyList(),
    val isCurrentPlaceLoaded: Boolean = false,
    val currentPlace: FullAccessibleLocalMarker = FullAccessibleLocalMarker(),
    val allImagesLoaded: Boolean = false,
    val userComment: String = "",
    val userCommentDate: String = "",
    val userAccessibilityRate: Int = 0,
    val trySendComment: Boolean = false,
    val isUserCommented: Boolean = false,
    val isEditingPlace: Boolean = false,
    val imagesToUploadSize: Int? = null,
    val imagesUploadedSize: Int = 0,
    val isErrorUploadingImages: Boolean = false,
    val isUploadingImages: Boolean = false
)
