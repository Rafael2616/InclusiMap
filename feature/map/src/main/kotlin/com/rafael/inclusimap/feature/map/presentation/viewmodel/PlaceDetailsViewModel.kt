package com.rafael.inclusimap.feature.map.presentation.viewmodel

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafael.inclusimap.core.domain.model.AccessibleLocalMarker
import com.rafael.inclusimap.core.domain.model.Comment
import com.rafael.inclusimap.core.domain.model.FullAccessibleLocalMarker
import com.rafael.inclusimap.core.domain.model.PlaceImage
import com.rafael.inclusimap.core.domain.model.toAccessibleLocalMarker
import com.rafael.inclusimap.core.domain.model.toFullAccessibleLocalMarker
import com.rafael.inclusimap.core.domain.model.util.extractUserEmail
import com.rafael.inclusimap.core.domain.network.Result
import com.rafael.inclusimap.core.domain.network.onSuccess
import com.rafael.inclusimap.core.domain.util.Constants.INCLUSIMAP_IMAGE_FOLDER_ID
import com.rafael.inclusimap.core.services.GoogleDriveService
import com.rafael.inclusimap.feature.auth.domain.repository.LoginRepository
import com.rafael.inclusimap.feature.map.domain.PlaceDetailsEvent
import com.rafael.inclusimap.feature.map.domain.PlaceDetailsState
import java.util.Date
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PlaceDetailsViewModel(
    private val driveService: GoogleDriveService,
    private val loginRepository: LoginRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(PlaceDetailsState())
    val state = _state.asStateFlow()
    private var userName = ""
    private var userEmail = ""

    fun onEvent(event: PlaceDetailsEvent) {
        when (event) {
            is PlaceDetailsEvent.OnUploadPlaceImages -> onUploadPlaceImages(
                event.uri,
                event.context,
            )

            PlaceDetailsEvent.OnDestroyPlaceDetails -> onDestroyPlaceDetailsScreen()
            is PlaceDetailsEvent.SetCurrentPlace -> setCurrentPlace(event.place)
            is PlaceDetailsEvent.OnDeletePlaceImage -> onDeletePlaceImage(event.image)
            is PlaceDetailsEvent.SetUserAccessibilityRate -> setUserAccessibilityRate(event.rate)
            PlaceDetailsEvent.OnSendComment -> onSendComment()
            is PlaceDetailsEvent.SetUserComment -> setUserComment(event.comment)
            is PlaceDetailsEvent.SetIsUserCommented -> _state.update { it.copy(isUserCommented = event.isCommented) }
            PlaceDetailsEvent.OnDeleteComment -> onDeleteComment()
            is PlaceDetailsEvent.SetIsEditingPlace -> _state.update { it.copy(isEditingPlace = event.isEditing) }
        }
    }

    private fun setCurrentPlace(place: AccessibleLocalMarker) {
        viewModelScope.launch(Dispatchers.IO) {
            async {
                _state.update {
                    it.copy(
                        isCurrentPlaceLoaded = it.loadedPlaces.any { existingPlace -> existingPlace.id == place.id },
                        allImagesLoaded = it.loadedPlaces.find { existingPlace -> existingPlace.id == place.id }?.images?.isNotEmpty()
                            ?: false,
                        currentPlace = place.toFullAccessibleLocalMarker(
                            images = emptyList(),
                            imageFolderId = null,
                            imageFolder = null,
                        ),
                        loadedPlaces = if (place !in state.value.loadedPlaces.map { it.toAccessibleLocalMarker() }) {
                            state.value.loadedPlaces + place.toFullAccessibleLocalMarker(
                                images = emptyList(),
                                imageFolder = null,
                                imageFolderId = null,
                            )
                        } else {
                            state.value.loadedPlaces
                        },
                    )
                }
                delay(350)
            }.await()
        }.invokeOnCompletion {
            if (!state.value.isCurrentPlaceLoaded || state.value.loadedPlaces.find { existingPlace -> existingPlace.id == place.id }?.images?.isEmpty() == true) {
                loadImages(place)
            } else {
                loadImagesFromCache(place)
            }
        }
        loadUserComment(place)
    }

    private fun loadUserComment(place: AccessibleLocalMarker) {
        viewModelScope.launch(Dispatchers.IO) {
            async {
                loginRepository.getLoginInfo(1)?.let {
                    userName = it.userName!!
                    userEmail = it.userEmail!!
                }
            }.await()
        }.invokeOnCompletion {
            _state.update {
                it.copy(
                    isUserCommented = it.loadedPlaces.find { existingPlace -> existingPlace.id == place.id }?.comments?.any { comment -> comment.email == userEmail }
                        ?: false,
                    userComment = it.loadedPlaces.find { existingPlace -> existingPlace.id == place.id }?.comments?.find { comment -> comment.email == userEmail }?.body
                        ?: "",
                    userAccessibilityRate = it.loadedPlaces.find { existingPlace -> existingPlace.id == place.id }?.comments?.find { comment -> comment.email == userEmail }?.accessibilityRate
                        ?: 0,
                )
            }
        }
    }

    private fun onDestroyPlaceDetailsScreen() {
        _state.update {
            it.copy(
                isCurrentPlaceLoaded = false,
                currentPlace = FullAccessibleLocalMarker(),
                isUserCommented = false,
                userComment = "",
                allImagesLoaded = false,
            )
        }
    }

    private fun loadImages(placeDetails: AccessibleLocalMarker) {
        _state.update { it.copy(allImagesLoaded = false) }
        viewModelScope.launch(Dispatchers.IO) {
            async {
                driveService.listFiles(
                    INCLUSIMAP_IMAGE_FOLDER_ID,
                ).onSuccess { imageRepositoryFolder ->
                    _state.update {
                        it.copy(
                            inclusiMapImageRepositoryFolder = imageRepositoryFolder,
                        )
                    }
                }
            }.await()
            _state.update {
                it.copy(
                    currentPlace = it.currentPlace.copy(
                        imageFolderId = state.value.inclusiMapImageRepositoryFolder.find { subPaths ->
                            subPaths.name == placeDetails.id + "_" + placeDetails.authorEmail
                        }?.id,
                    ),
                )
            }
            if (_state.value.currentPlace.imageFolderId.isNullOrEmpty() ||
                when (val result =
                    driveService.listFiles(_state.value.currentPlace.imageFolderId!!)) {
                    is Result.Success -> result.data.isEmpty()
                    is Result.Error -> {
                        true
                    }
                }
            ) {
                _state.update { it.copy(allImagesLoaded = true) }
                println("No images found for place ${placeDetails.title} ${placeDetails.id}")
                return@launch
            }
            async {
                val folderId = state.value.currentPlace.imageFolderId
                folderId?.let {
                    when (val result = driveService.listFiles(folderId)) {
                        is Result.Success -> {
                            _state.update {
                                it.copy(
                                    currentPlace = it.currentPlace.copy(
                                        imageFolder = result.data,
                                    ),
                                )
                            }
                        }

                        is Result.Error -> {
                        }
                    }
                }
            }.await()

            _state.value.currentPlace.imageFolder?.map { file ->
                println("Loading image ${file.name}")
                async {
                    try {
                        val fileContent = driveService.driveService.files().get(file.id)
                            .executeMediaAsInputStream()
                        val options = BitmapFactory.Options()
                        options.inSampleSize = 3
                        BitmapFactory.decodeStream(fileContent, null, options)
                            ?.asImageBitmap()?.also { image ->
                                if (placeDetails.id != _state.value.currentPlace.id) {
                                    return@async
                                }
                                _state.update {
                                    it.copy(
                                        currentPlace = it.currentPlace.copy(
                                            images = it.currentPlace.images +
                                                PlaceImage(
                                                    userEmail = file.name.extractUserEmail(),
                                                    image = image,
                                                    placeID = it.currentPlace.id!!,
                                                ),
                                        ),
                                    ).also {
                                        println("Loading image ${file.name}")
                                    }
                                }
                                if (_state.value.currentPlace.images.size == _state.value.currentPlace.imageFolder!!.size) {
                                    _state.update {
                                        it.copy(allImagesLoaded = true)
                                    }
                                }
                            }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }?.awaitAll()
        }.invokeOnCompletion {
            _state.update {
                it.copy(
                    loadedPlaces = it.loadedPlaces.map { place ->
                        if (place.id == state.value.currentPlace.id) {
                            place.copy(images = state.value.currentPlace.images)
                        } else {
                            place
                        }
                    },
                ).also {
                    println("Images cached successfully for ${it.currentPlace.title} ${it.currentPlace.id} + size: ${it.currentPlace.images.size}")
                }
            }
        }
    }

    private fun loadImagesFromCache(placeDetails: AccessibleLocalMarker) {
        viewModelScope.launch(Dispatchers.IO) {
            val placeWithImages = placeDetails.toFullAccessibleLocalMarker(
                _state.value.loadedPlaces.find { place -> place.id == placeDetails.id }?.images
                    ?: emptyList(),
                _state.value.loadedPlaces.find { place -> place.id == placeDetails.id }?.imageFolder,
                _state.value.loadedPlaces.find { place -> place.id == placeDetails.id }?.imageFolderId,
            )
            _state.update {
                it.copy(
                    currentPlace = placeWithImages,
                    allImagesLoaded = true,
                ).also {
                    println("Loading images from cache + size: ${it.currentPlace.images.size}")
                }
            }
        }
    }

    private fun onUploadPlaceImages(uri: Uri, context: Context) {
        // Add the image to the list of images tobe showed in the app UI
        val newImage = PlaceImage(
            userEmail = userEmail,
            BitmapFactory.decodeStream(
                context.contentResolver.openInputStream(uri),
            ).asImageBitmap(),
            _state.value.currentPlace.id!!,
        )
        _state.update {
            it.copy(
                currentPlace = it.currentPlace.copy(images = it.currentPlace.images + newImage),
                loadedPlaces = it.loadedPlaces.map { place ->
                    if (place.id == it.currentPlace.id) {
                        place.copy(images = it.currentPlace.images + newImage)
                    } else {
                        place
                    }
                },
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
            async {
                if (_state.value.currentPlace.imageFolderId.isNullOrEmpty()) {
                    _state.update {
                        it.copy(
                            currentPlace = it.currentPlace.copy(
                                imageFolderId = driveService.createFolder(
                                    _state.value.currentPlace.id + "_" + _state.value.currentPlace.authorEmail,
                                    INCLUSIMAP_IMAGE_FOLDER_ID,
                                ),
                            ),
                        )
                    }
                }
            }.await()
            driveService.uploadFile(
                context.contentResolver.openInputStream(uri),
                "${
                    _state.value.currentPlace.id
                }_$userEmail-${Date().toInstant()}.jpg",
                _state.value.currentPlace.imageFolderId
                    ?: throw IllegalStateException("Folder not found: Maybe an issue has occurred while creating the folder"),
            )
        }
    }

    private fun onDeletePlaceImage(image: PlaceImage) {
        _state.update {
            it.copy(
                currentPlace = it.currentPlace.copy(
                    images = it.currentPlace.images - image,
                    imageFolderId = state.value.inclusiMapImageRepositoryFolder.find { subPaths ->
                        subPaths.name.split("_")[0] == _state.value.currentPlace.id
                    }?.id,
                ),
                loadedPlaces = it.loadedPlaces.map { place ->
                    if (place.id == it.currentPlace.id) {
                        place.copy(images = it.currentPlace.images - image)
                    } else {
                        place
                    }
                },
            )
        }
        viewModelScope.launch(Dispatchers.Default) {
            val folderId = state.value.currentPlace.imageFolderId.orEmpty()
            when (val result = driveService.listFiles(folderId)) {
                is Result.Success -> {
                    val imageId =
                        result.data.find { it.name.extractUserEmail() == image.userEmail }?.id.orEmpty()
                    driveService.deleteFile(imageId)
                }

                is Result.Error -> {
                }
            }
        }
    }

    private fun onSendComment() {
        if (_state.value.userComment.isEmpty()) {
            _state.update { it.copy(trySendComment = true) }
            return
        }

        _state.update {
            it.copy(
                trySendComment = true,
                currentPlace = it.currentPlace.copy(
                    comments = it.currentPlace.comments.filter { comment -> comment.email != userEmail },
                ),
            )
        }
        if (state.value.userAccessibilityRate != 0) {
            val userComment =
                Comment(
                    postDate = System.currentTimeMillis()
                        .toString(),
                    id = _state.value.currentPlace.comments.size.plus(
                        1,
                    ),
                    name = userName,
                    body = state.value.userComment,
                    email = userEmail,
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
                        if (place.id == _state.value.currentPlace.id) {
                            place.copy(comments = place.comments + userComment)
                        } else {
                            place
                        }
                    },
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
                    comments = it.currentPlace.comments.filter { comment -> comment.email != userEmail },
                ),
                loadedPlaces = it.loadedPlaces.map { place ->
                    if (place.id == it.currentPlace.id) {
                        place.copy(comments = place.comments.filter { comment -> comment.email != userEmail })
                    } else {
                        place
                    }
                },
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
                userComment = comment,
            )
        }
    }
}
