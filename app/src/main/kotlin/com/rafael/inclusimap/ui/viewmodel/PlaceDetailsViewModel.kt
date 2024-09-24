package com.rafael.inclusimap.ui.viewmodel

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafael.inclusimap.data.GoogleDriveService
import com.rafael.inclusimap.data.extractUserName
import com.rafael.inclusimap.domain.AccessibleLocalMarker
import com.rafael.inclusimap.domain.FullAccessibleLocalMarker
import com.rafael.inclusimap.domain.INCLUSIMAP_IMAGE_FOLDER_ID
import com.rafael.inclusimap.domain.PlaceDetailsEvent
import com.rafael.inclusimap.domain.PlaceDetailsState
import com.rafael.inclusimap.domain.PlaceImage
import com.rafael.inclusimap.domain.USER_NAME
import com.rafael.inclusimap.domain.toFullAccessibleLocalMarker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date

class PlaceDetailsViewModel : ViewModel() {
    private val driveService: GoogleDriveService = GoogleDriveService()

    private val _state = MutableStateFlow(PlaceDetailsState())
    val state = _state.asStateFlow()

    fun onEvent(event: PlaceDetailsEvent) {
        when (event) {
            is PlaceDetailsEvent.OnUploadPlaceImages -> onUploadPlaceImages(
                event.uri,
                event.context
            )

            PlaceDetailsEvent.OnDetroyPlaceDetails -> onDestroyPlaceDetailsScreen()
            is PlaceDetailsEvent.SetCurrentPlace -> setCurrentPlace(event.place)
        }
    }

    private fun setCurrentPlace(place: AccessibleLocalMarker) {
        _state.update {
            it.copy(
                isCurrentPlaceLoaded = it.loadedPlaces.any { existingPlace -> existingPlace.title == place.title },
                allImagesLoaded = it.loadedPlaces.any { existingPlace -> existingPlace.title == place.title },
                currentPlace = place,
                loadedPlaces = state.value.loadedPlaces + place.toFullAccessibleLocalMarker(null)
            ).also {
                println("Place ${place.title} already loaded? ${state.value.isCurrentPlaceLoaded}")
            }
        }
        if (!_state.value.isCurrentPlaceLoaded) {
            loadImages(place)
        } else {
            loadImagesFromCache()
        }
    }

    private fun onDestroyPlaceDetailsScreen() {
        _state.update {
            it.copy(
                currentPlaceImages = emptyList(),
                currentPlaceImagesFolder = emptyList(),
                currentPlaceFolderID = null,
                currentPlace = AccessibleLocalMarker(),
                allImagesLoaded = false,
            )
        }
        println("Place details screen destroyed")
    }

    private fun loadImages(placeDetails: AccessibleLocalMarker) {
        viewModelScope.launch(Dispatchers.IO) {
            async {
                _state.update {
                    it.copy(
                        inclusiMapImageRepositoryFolder = driveService.listFiles(
                            INCLUSIMAP_IMAGE_FOLDER_ID
                        ),
                    )
                }
            }.await()
            _state.update {
                it.copy(
                    currentPlaceFolderID = state.value.inclusiMapImageRepositoryFolder.find { subPaths -> subPaths.name == placeDetails.title }?.id
                )
            }
            if (_state.value.currentPlaceFolderID.isNullOrEmpty() || driveService.listFiles(state.value.currentPlaceFolderID!!)
                    .isEmpty()
            ) {
                _state.update { it.copy(allImagesLoaded = true) }
                println("No images found for ${placeDetails.title}")
                return@launch
            }
            _state.update { it.copy(currentPlaceImagesFolder = driveService.listFiles(state.value.currentPlaceFolderID!!)) }

            state.value.currentPlaceImagesFolder.map { file ->
                async {
                    try {
                        val fileContent = driveService.driveService.files().get(file.id)
                            .executeMediaAsInputStream()
                        val options = BitmapFactory.Options()
                        options.inSampleSize = 3
                        BitmapFactory.decodeStream(fileContent, null, options)
                            ?.asImageBitmap()?.also { image ->
                                _state.update {
                                    it.copy(
                                        currentPlaceImages = it.currentPlaceImages +
                                                PlaceImage(
                                                    userName = file.name.extractUserName(),
                                                    image = image
                                                )
                                    ).also {
                                        println("Loading image ${file.name}")
                                    }
                                }
                                if (_state.value.currentPlaceImages.size == _state.value.currentPlaceImagesFolder.size) {
                                    _state.update {
                                        it.copy(allImagesLoaded = true)
                                    }
                                }
                            }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }.awaitAll()
        }.invokeOnCompletion {
            _state.update {
                it.copy(
                    loadedPlaces = it.loadedPlaces.map { place ->
                        if (place.title == it.currentPlace.title) {
                            place.copy(images = it.currentPlaceImages)
                        } else {
                            place
                        }
                    }
                ).also {
                    println("Images cached successfully for ${it.currentPlace.title} + ${it.currentPlaceImages.size}")
                }
            }
        }
    }

    private fun loadImagesFromCache() {
        viewModelScope.launch(Dispatchers.IO) {
            _state.update {
                it.copy(
                    currentPlaceImages = _state.value.loadedPlaces.find { it.title == _state.value.currentPlace.title }?.images
                        ?: emptyList()
                ).also {
                    println("Loading images from cache" + it.currentPlaceImages.size)
                }
            }
        }
    }

    private fun onUploadPlaceImages(uri: Uri, context: Context) {
        // Add the image to the list of images tobe showed in the app UI
        _state.update {
            it.copy(
                currentPlaceImages = it.currentPlaceImages +
                        PlaceImage(
                            userName = USER_NAME,
                            BitmapFactory.decodeStream(
                                context.contentResolver.openInputStream(uri)
                            ).asImageBitmap()
                        )
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
            async {
                if (_state.value.currentPlaceFolderID.isNullOrEmpty()) {
                    _state.update {
                        it.copy(
                            currentPlaceFolderID = driveService.createFolder(
                                _state.value.currentPlace.title,
                                INCLUSIMAP_IMAGE_FOLDER_ID
                            )
                        )
                    }
                }
            }.await()
            driveService.uploadFile(
                context.contentResolver.openInputStream(uri),
                "${
                    _state.value.currentPlace.title.replace(
                        " ",
                        ""
                    )
                }-$USER_NAME-${Date().toInstant()}.jpg",
                _state.value.currentPlaceFolderID
                    ?: throw IllegalStateException("Folder not found: Maybe an issue has occurred while creating the folder")
            )
        }
    }
}