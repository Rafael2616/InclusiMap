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
import com.rafael.inclusimap.domain.Comment
import com.rafael.inclusimap.domain.PlaceDetailsEvent
import com.rafael.inclusimap.domain.PlaceDetailsState
import com.rafael.inclusimap.domain.PlaceImage
import com.rafael.inclusimap.domain.toFullAccessibleLocalMarker
import com.rafael.inclusimap.domain.util.Constants.INCLUSIMAP_IMAGE_FOLDER_ID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date

class PlaceDetailsViewModel(
    private val driveService: GoogleDriveService
) : ViewModel() {

    private val _state = MutableStateFlow(PlaceDetailsState())
    val state = _state.asStateFlow()
    val USER_NAME = "<Sem Nome>" // Get the real user name when create de LoginEntity db
    fun onEvent(event: PlaceDetailsEvent) {
        when (event) {
            is PlaceDetailsEvent.OnUploadPlaceImages -> onUploadPlaceImages(
                event.uri,
                event.context
            )

            PlaceDetailsEvent.OnDestroyPlaceDetails -> onDestroyPlaceDetailsScreen()
            is PlaceDetailsEvent.SetCurrentPlace -> setCurrentPlace(event.place)
            is PlaceDetailsEvent.OnDeletePlaceImage -> onDeletePlaceImage(event.image)
            is PlaceDetailsEvent.SetUserAccessibilityRate -> setUserAccessibilityRate(event.rate)
            PlaceDetailsEvent.OnSendComment -> onSendComment()
            is PlaceDetailsEvent.SetUserComment -> setUserComment(event.comment)
            is PlaceDetailsEvent.SetIsUserCommented -> _state.update { it.copy(isUserCommented = event.isCommented) }
            PlaceDetailsEvent.OnDeleteComment -> onDeleteComment()
        }
    }

    private fun setCurrentPlace(place: AccessibleLocalMarker) {
        _state.update {
            it.copy(
                isCurrentPlaceLoaded = it.loadedPlaces.any { existingPlace -> existingPlace.title == place.title },
                allImagesLoaded = it.loadedPlaces.any { existingPlace -> existingPlace.title == place.title },
                isUserCommented = it.loadedPlaces.find { existingPlace -> existingPlace.title == place.title }?.comments?.any { comment -> comment.name == USER_NAME } ?: false,
                userComment = it.loadedPlaces.find { existingPlace -> existingPlace.title == place.title }?.comments?.find { comment -> comment.name == USER_NAME }?.body ?: "",
                userAccessibilityRate = it.loadedPlaces.find { existingPlace -> existingPlace.title == place.title }?.comments?.find { comment -> comment.name == USER_NAME }?.accessibilityRate ?: 0,
                currentPlace = place,
                loadedPlaces = state.value.loadedPlaces + place.toFullAccessibleLocalMarker(
                    emptyList()
                )
            ).also {
                println("Is place ${place.title} already loaded? ${state.value.isCurrentPlaceLoaded}")
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
                isUserCommented = false,
                userComment = "",
                allImagesLoaded = false,
            )
        }
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
                println("No images found for place ${placeDetails.title}")
                return@launch
            }
            _state.update { it.copy(currentPlaceImagesFolder = driveService.listFiles(state.value.currentPlaceFolderID!!)) }

            _state.value.currentPlaceImagesFolder.map { file ->
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
                    println("Images cached successfully for ${it.currentPlace.title} + size: ${it.currentPlaceImages.size}")
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
                    println("Loading images from cache + size: ${it.currentPlaceImages.size}")
                }
            }
        }
    }

    private fun onUploadPlaceImages(uri: Uri, context: Context) {
        // Add the image to the list of images tobe showed in the app UI
        val newImage = PlaceImage(
            userName = USER_NAME,
            BitmapFactory.decodeStream(
                context.contentResolver.openInputStream(uri)
            ).asImageBitmap()
        )
        _state.update {
            it.copy(
                currentPlaceImages = it.currentPlaceImages + newImage,
                loadedPlaces = it.loadedPlaces.map { place ->
                    if (place.title == it.currentPlace.title) {
                        place.copy(images = it.currentPlaceImages + newImage)
                    } else {
                        place
                    }
                }
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

    private fun onDeletePlaceImage(image: PlaceImage) {
        _state.update {
            it.copy(
                currentPlaceImages = it.currentPlaceImages - image,
                loadedPlaces = it.loadedPlaces.map { place ->
                    if (place.title == it.currentPlace.title) {
                        place.copy(images = it.currentPlaceImages - image)
                    } else {
                        place
                    }
                },
                currentPlaceFolderID = state.value.inclusiMapImageRepositoryFolder.find { subPaths ->
                    subPaths.name == _state.value.currentPlace.title
                }?.id
            )
        }
        viewModelScope.launch(Dispatchers.Default) {
            val localMarkerFiles =
                driveService.listFiles(state.value.currentPlaceFolderID.orEmpty())
            val imageId =
                localMarkerFiles.find { it.name.extractUserName() == image.userName }?.id.orEmpty()
            driveService.deleteFile(imageId)
        }
    }

    private fun onSendComment() {
        _state.update {
            it.copy(
                trySendComment = true,
                currentPlace = it.currentPlace.copy(
                    comments = it.currentPlace.comments.filter { comment -> comment.name != USER_NAME }
                )
            )
        }
        if (state.value.userComment.isNotEmpty() && state.value.userAccessibilityRate != 0) {
            val userComment =
                    Comment(
                        postDate = System.currentTimeMillis()
                            .toString(),
                        id = _state.value.currentPlace.comments.size.plus(
                            1
                        ),
                        name = USER_NAME,
                        body = state.value.userComment,
                        email = "",
                        accessibilityRate = state.value.userAccessibilityRate,
                    )
            _state.update {
                it.copy(
                    currentPlace = it.currentPlace.copy(
                        comments = it.currentPlace.comments + userComment,
                    ),
                    trySendComment = false,
                    isUserCommented = true,
                    loadedPlaces = it.loadedPlaces.map { place ->
                        if (place.title == _state.value.currentPlace.title) {
                            place.copy(comments = place.comments + userComment)
                        } else {
                            place
                        }
                    }
                )
            }
        }
    }

    private fun onDeleteComment() {
        _state.update {
            it.copy(
                userComment = "",
                trySendComment = false,
                userAccessibilityRate = 0,
                isUserCommented = false,
                currentPlace = it.currentPlace.copy(
                    comments = it.currentPlace.comments.filter { comment -> comment.name != USER_NAME }
                ),
                loadedPlaces = it.loadedPlaces.map { place ->
                    if (place.title == it.currentPlace.title) {
                        place.copy(comments = place.comments.filter { comment -> comment.name != USER_NAME })
                    } else {
                        place
                    }
                }
            )
        }
    }

    private fun setUserAccessibilityRate(rate: Int) {
        if (_state.value.isUserCommented) return
        _state.update {
            it.copy(userAccessibilityRate = rate)
        }
    }

    private fun setUserComment(comment: String) {
        _state.update {
            it.copy(
                trySendComment = false,
                userComment = comment
            )
        }
    }
}