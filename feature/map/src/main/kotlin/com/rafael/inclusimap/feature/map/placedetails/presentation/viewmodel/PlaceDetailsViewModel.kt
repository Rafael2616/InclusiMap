package com.rafael.inclusimap.feature.map.placedetails.presentation.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafael.inclusimap.core.domain.model.AccessibilityResource
import com.rafael.inclusimap.core.domain.model.AccessibleLocalMarker
import com.rafael.inclusimap.core.domain.model.Comment
import com.rafael.inclusimap.core.domain.model.FullAccessibleLocalMarker
import com.rafael.inclusimap.core.domain.model.PlaceImage
import com.rafael.inclusimap.core.domain.model.Resource
import com.rafael.inclusimap.core.domain.model.toAccessibleLocalMarker
import com.rafael.inclusimap.core.domain.model.toFullAccessibleLocalMarker
import com.rafael.inclusimap.core.domain.model.util.extractPlaceID
import com.rafael.inclusimap.core.domain.model.util.extractUserEmail
import com.rafael.inclusimap.core.domain.network.Result
import com.rafael.inclusimap.core.domain.network.onError
import com.rafael.inclusimap.core.domain.network.onSuccess
import com.rafael.inclusimap.core.domain.util.Constants.INCLUSIMAP_IMAGE_FOLDER_ID
import com.rafael.inclusimap.core.domain.util.Constants.INCLUSIMAP_PARAGOMINAS_PLACE_DATA_FOLDER_ID
import com.rafael.inclusimap.core.domain.util.Constants.MAX_IMAGE_NUMBER
import com.rafael.inclusimap.core.services.GoogleDriveService
import com.rafael.inclusimap.feature.auth.domain.repository.LoginRepository
import com.rafael.inclusimap.feature.contributions.domain.model.Contribution
import com.rafael.inclusimap.feature.contributions.domain.model.ContributionType
import com.rafael.inclusimap.feature.contributions.domain.repository.ContributionsRepository
import com.rafael.inclusimap.feature.map.placedetails.domain.model.PlaceDetailsEvent
import com.rafael.inclusimap.feature.map.placedetails.domain.model.PlaceDetailsState
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.Date
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class PlaceDetailsViewModel(
    private val driveService: GoogleDriveService,
    private val loginRepository: LoginRepository,
    private val contributionsRepository: ContributionsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(PlaceDetailsState())
    val state = _state.asStateFlow()
    private var userName = ""
    private var userEmail = ""
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    fun onEvent(event: PlaceDetailsEvent) {
        when (event) {
            is PlaceDetailsEvent.OnUploadPlaceImages -> onUploadPlaceImages(
                event.uris,
                event.context,
                event.placeId,
            )

            PlaceDetailsEvent.OnDestroyPlaceDetails -> onDestroyPlaceDetailsScreen()
            is PlaceDetailsEvent.SetCurrentPlace -> setCurrentPlace(event.place)
            is PlaceDetailsEvent.OnDeletePlaceImage -> onDeletePlaceImage(event.image)
            is PlaceDetailsEvent.SetUserAccessibilityRate -> setUserAccessibilityRate(event.rate)
            is PlaceDetailsEvent.OnSendComment -> onSendComment(event.comment)
            is PlaceDetailsEvent.SetIsUserCommented -> _state.update { it.copy(isUserCommented = event.isCommented) }
            PlaceDetailsEvent.OnDeleteComment -> onDeleteComment()
            is PlaceDetailsEvent.SetIsEditingPlace -> _state.update { it.copy(isEditingPlace = event.isEditing) }
            is PlaceDetailsEvent.OnUpdatePlaceAccessibilityResources -> onUpdatePlaceAccessibilityResources(
                event.resources,
            )

            is PlaceDetailsEvent.SetIsEditingComment -> _state.update { it.copy(isEditingComment = event.isEditing) }
            is PlaceDetailsEvent.SetIsTrySendComment -> _state.update { it.copy(trySendComment = event.isTrying) }
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
                delay(300)
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
                    userName = it.userName ?: return@async
                    userEmail = it.userEmail ?: return@async
                }
            }.await()
        }.invokeOnCompletion {
            val userComment = state.value.loadedPlaces.find { existingPlace ->
                existingPlace.id == place.id
            }?.comments?.find { it.email == userEmail }

            _state.update {
                it.copy(
                    isUserCommented = userComment != null,
                    userComment = userComment?.body ?: "",
                    userCommentDate = userComment?.postDate ?: "",
                    userAccessibilityRate = userComment?.accessibilityRate ?: 0,
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
                isEditingComment = false,
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
                val imageFolderID = state.value.inclusiMapImageRepositoryFolder.find { subPaths ->
                    subPaths.name == placeDetails.id + "_" + placeDetails.authorEmail
                }?.id
                it.copy(
                    currentPlace = it.currentPlace.copy(
                        imageFolderId = imageFolderID,
                    ),
                    loadedPlaces = it.loadedPlaces.map { place ->
                        if (place.id == placeDetails.id) {
                            place.copy(imageFolderId = imageFolderID)
                        } else {
                            place
                        }
                    },
                )
            }
            if (_state.value.currentPlace.imageFolderId.isNullOrEmpty() ||
                when (
                    val result =
                        driveService.listFiles(_state.value.currentPlace.imageFolderId ?: "")
                ) {
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
                    driveService.listFiles(folderId).onSuccess { imageFolder ->
                        _state.update {
                            it.copy(
                                currentPlace = it.currentPlace.copy(
                                    imageFolder = imageFolder,
                                ),
                                loadedPlaces = it.loadedPlaces.map { place ->
                                    if (place.id == placeDetails.id) {
                                        place.copy(imageFolder = imageFolder)
                                    } else {
                                        place
                                    }
                                },
                            )
                        }
                    }
                }
            }.await()

            _state.value.currentPlace.imageFolder?.mapIndexed { index, file ->
                async {
                    // Is expected that this condition never sucessseds, but is important to ensure
                    // that no more images that max limit loads, as it can cause UI lags, and much processing
                    if (index >= MAX_IMAGE_NUMBER) {
                        _state.update {
                            it.copy(allImagesLoaded = true)
                        }
                        println("Max image limit reached, skipping image ${file.name}")
                        return@async
                    }

                    try {
                        val fileContent = driveService.driveService.files().get(file.id)
                            .executeMediaAsInputStream()
                        val options = BitmapFactory.Options()
                        options.inSampleSize = 2
                        BitmapFactory.decodeStream(fileContent, null, options)
                            ?.asImageBitmap()?.also { image ->
                                if (placeDetails.id != _state.value.currentPlace.id) {
                                    return@async
                                }
                                if (file.name in _state.value.currentPlace.images.map { it?.name }) {
                                    println("Skipping already loaded image ${file.name}")
                                    return@async
                                }

                                val placeImage = PlaceImage(
                                    userEmail = file.name.extractUserEmail(),
                                    image = image,
                                    placeID = placeDetails.id ?: return@async,
                                    name = file.name,
                                )
                                _state.update {
                                    it.copy(
                                        currentPlace = it.currentPlace.copy(
                                            images = it.currentPlace.images + placeImage,
                                        ),
                                        loadedPlaces = it.loadedPlaces.map { place ->
                                            if (place.id == state.value.currentPlace.id) {
                                                place.copy(images = place.images + placeImage)
                                            } else {
                                                place
                                            }
                                        },
                                    ).also {
                                        println("Loading image $index with name ${file.name} with id ${file.id}")
                                    }
                                }
                                if (_state.value.currentPlace.images.size == _state.value.currentPlace.imageFolder?.size) {
                                    _state.update {
                                        it.copy(allImagesLoaded = true)
                                    }
                                    println("All images cached successfully for ${placeDetails.title} ${placeDetails.id} + size: ${_state.value.currentPlace.images.size}")
                                }
                            }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }?.awaitAll()
        }.invokeOnCompletion {
            _state.update {
                it.copy(allImagesLoaded = true)
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
            println("All images founded:" + _state.value.loadedPlaces.find { place -> place.id == placeDetails.id }?.images?.size)
            _state.update {
                it.copy(
                    currentPlace = placeWithImages,
                ).also {
                    checkAllImagesLoaded(
                        place = placeDetails,
                        currentImagesSize = it.currentPlace.images.size,
                        imagesLoadedSize = placeWithImages.imageFolder?.size,
                    )
                }
            }
        }
    }

    private fun checkAllImagesLoaded(
        place: AccessibleLocalMarker,
        currentImagesSize: Int,
        imagesLoadedSize: Int?,
    ) {
        if (imagesLoadedSize == null) return
        if (currentImagesSize != imagesLoadedSize) {
            println("Some images are not loaded yet, loading now...")
            loadImages(place)
        } else {
            _state.update {
                it.copy(allImagesLoaded = true)
            }
        }
    }

    private fun onUploadPlaceImages(
        uris: List<Uri>,
        context: Context,
        placeId: String,
    ) {
        _state.update {
            it.copy(
                imagesToUploadSize = null,
                imagesUploadedSize = 0,
                isUploadingImages = true,
                isErrorUploadingImages = false,
            )
        }
        // Add the image to the list of images tobe showed in the app UI
        val options = BitmapFactory.Options()
        options.inSampleSize = 3
        val options2 = BitmapFactory.Options()
        options2.inSampleSize = 2

        _state.update { it.copy(imagesToUploadSize = uris.size) }

        viewModelScope.launch(Dispatchers.IO) {
            async {
                driveService.listFiles(INCLUSIMAP_IMAGE_FOLDER_ID).onSuccess { placeImagesFolder ->
                    val placeImageFolderExists =
                        placeImagesFolder.find { it.name.extractPlaceID() == placeId }?.id
                    _state.update {
                        it.copy(
                            currentPlace = it.currentPlace.copy(
                                imageFolderId = placeImageFolderExists ?: driveService.createFolder(
                                    _state.value.currentPlace.id + "_" + _state.value.currentPlace.authorEmail,
                                    INCLUSIMAP_IMAGE_FOLDER_ID,
                                ),
                            ),
                        )
                    }
                }
            }.await()

            var imagesFileIds = emptyList<String?>()
            uris.mapIndexed { index, uri ->
                val bitmap =
                    BitmapFactory.decodeStream(context.contentResolver.openInputStream(uri))
                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
                val compressedImage = outputStream.toByteArray()
                val imageFileName =
                    "${_state.value.currentPlace.id}_$userEmail-${Date().toInstant()}.jpg"
                async {
                    val imageId = driveService.uploadFile(
                        fileContent = ByteArrayInputStream(compressedImage),
                        fileName = imageFileName,
                        folderId = _state.value.currentPlace.imageFolderId ?: return@async,
                    )

                    if (imageId == null) {
                        println("Error uploading image $index")
                        _state.update {
                            it.copy(isErrorUploadingImages = true)
                        }
                    }
                    val image = PlaceImage(
                        userEmail = userEmail,
                        image = BitmapFactory.decodeStream(
                            context.contentResolver.openInputStream(uri),
                            null,
                            options,
                        )?.asImageBitmap() ?: BitmapFactory.decodeStream(
                            context.contentResolver.openInputStream(uri),
                            null,
                            options2,
                        )?.asImageBitmap() ?: BitmapFactory.decodeStream(
                            context.contentResolver.openInputStream(uri),
                        ).asImageBitmap(),
                        placeID = imageId ?: return@async,
                        name = imageFileName,
                    )

                    _state.update {
                        it.copy(
                            currentPlace = it.currentPlace.copy(
                                images = it.currentPlace.images + image,
                            ),
                            loadedPlaces = it.loadedPlaces.map { place ->
                                if (place.id == _state.value.currentPlace.id) {
                                    place.copy(images = place.images + image)
                                } else {
                                    place
                                }
                            },
                            imagesUploadedSize = it.imagesUploadedSize.plus(1),
                        )
                    }
                    imagesFileIds = imagesFileIds + imageId
                    if (index == uris.size - 1) {
                        println("All images uploaded successfully for $placeId")
                        println(state.value.loadedPlaces.find { it.id == placeId }?.images?.size)
                    }
                }
            }.awaitAll()
            val imageIds = imagesFileIds.filterNotNull()
            val contributions = imageIds.map {
                Contribution(
                    fileId = it,
                    type = ContributionType.IMAGE,
                )
            }
            addNewContributions(contributions)
        }.invokeOnCompletion {
            _state.update {
                it.copy(isUploadingImages = false)
            }
        }
    }

    private fun onDeletePlaceImage(image: PlaceImage) {
        _state.update {
            it.copy(
                isDeletingImage = true,
                isImageDeleted = false,
                isErrorDeletingImage = false,
            )
        }
        viewModelScope.launch(Dispatchers.Default) {
            val folderId = state.value.currentPlace.imageFolderId
            folderId?.let {
                driveService.listFiles(folderId).onSuccess { files ->
                    val imageId = files.find { it.name == image.name }?.id

                    if (imageId == null) {
                        _state.update {
                            it.copy(
                                isErrorDeletingImage = true,
                                isDeletingImage = false,
                            )
                        }
                        println("Image not found in folder")
                        return@launch
                    }
                    driveService.deleteFile(imageId)
                    removeContribution(
                        Contribution(
                            fileId = imageId,
                            type = ContributionType.IMAGE,
                        ),
                    )
                    _state.update {
                        it.copy(
                            currentPlace = it.currentPlace.copy(
                                images = it.currentPlace.images - image,
                            ),
                            loadedPlaces = it.loadedPlaces.map { place ->
                                if (place.id == it.currentPlace.id) {
                                    place.copy(images = it.currentPlace.images - image)
                                } else {
                                    place
                                }
                            },
                            isImageDeleted = true,
                        )
                    }
                }.onError {
                    _state.update {
                        it.copy(
                            isErrorDeletingImage = true,
                            isDeletingImage = false,
                        )
                    }
                    return@launch
                }
            }
            delay(500)
        }.invokeOnCompletion {
            _state.update {
                it.copy(
                    isDeletingImage = false,
                    isImageDeleted = false,
                )
            }
        }
    }

    private fun onSendComment(comment: String) {
        _state.update {
            it.copy(
                trySendComment = true,
                currentPlace = it.currentPlace.copy(
                    comments = it.currentPlace.comments.filter { comment -> comment.email != userEmail },
                ),
            )
        }
        val userComment =
            Comment(
                postDate = Date().toInstant().toString(),
                id = _state.value.currentPlace.comments.size.plus(
                    1,
                ),
                name = userName,
                body = comment,
                email = userEmail,
                accessibilityRate = state.value.userAccessibilityRate,
            )
        _state.update {
            it.copy(
                currentPlace = it.currentPlace.copy(
                    comments = it.currentPlace.comments + userComment,
                ),
                userComment = comment,
                trySendComment = false,
                isEditingComment = false,
                isUserCommented = true,
                loadedPlaces = it.loadedPlaces.map { place ->
                    if (place.id == _state.value.currentPlace.id) {
                        place.copy(comments = place.comments.filter { comment -> comment.email != userEmail } + userComment)
                    } else {
                        place
                    }
                },
                userCommentDate = Date().toInstant().toString(),
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
            driveService.listFiles(INCLUSIMAP_PARAGOMINAS_PLACE_DATA_FOLDER_ID)
                .onSuccess { places ->
                    places.find { it.name.extractPlaceID() == _state.value.currentPlace.id }
                        .also { place ->
                            val placeJson = driveService.getFileContent(place?.id ?: return@launch)
                            val json = Json {
                                ignoreUnknownKeys = true
                                prettyPrint = true
                            }
                            val placeString = json.decodeFromString<AccessibleLocalMarker>(
                                placeJson?.decodeToString() ?: return@launch,
                            )
                            val filteredComments =
                                placeString.comments.filterNot { it.email == userEmail }
                            val updatedPlace =
                                placeString.copy(comments = filteredComments + userComment)
                            val updatedPlaceString = json.encodeToString(updatedPlace)
                            driveService.updateFile(
                                place.id ?: return@launch,
                                placeString.id + "_" + placeString.authorEmail + ".json",
                                updatedPlaceString.byteInputStream(),
                            )
                            addNewContribution(
                                Contribution(
                                    fileId = place.id ?: return@launch,
                                    type = ContributionType.COMMENT,
                                ),
                            )
                        }
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
        viewModelScope.launch(Dispatchers.IO) {
            driveService.listFiles(INCLUSIMAP_PARAGOMINAS_PLACE_DATA_FOLDER_ID)
                .onSuccess { places ->
                    places.find { it.name.extractPlaceID() == _state.value.currentPlace.id }
                        .also { place ->
                            val placeJson = driveService.getFileContent(place?.id ?: return@launch)
                            val json = Json {
                                ignoreUnknownKeys = true
                                prettyPrint = true
                            }
                            val placeString = json.decodeFromString<AccessibleLocalMarker>(
                                placeJson?.decodeToString() ?: return@launch,
                            )

                            val updatedPlace = placeString.copy(
                                comments = placeString.comments.filterNot { it.email == userEmail },
                            )
                            val updatedPlaceString = json.encodeToString(updatedPlace)

                            driveService.updateFile(
                                place.id ?: return@launch,
                                placeString.id + "_" + placeString.authorEmail + ".json",
                                updatedPlaceString.byteInputStream(),
                            )

                            removeContribution(
                                Contribution(
                                    fileId = place.id ?: return@launch,
                                    type = ContributionType.COMMENT,
                                ),
                            )
                        }
                }
        }
    }

    private fun setUserAccessibilityRate(rate: Int) {
        if (_state.value.isUserCommented && !_state.value.isEditingComment) return
        _state.update {
            it.copy(userAccessibilityRate = rate)
        }
    }

    private fun onUpdatePlaceAccessibilityResources(updatedResources: List<Resource>) {
        val updatedResourcesBuilder = updatedResources.map { resource ->
            AccessibilityResource(
                resource = resource,
                lastModified = Date().toInstant().toString(),
                lastModifiedBy = userEmail,
            )
        }

        _state.update {
            it.copy(currentPlace = it.currentPlace.copy(resources = updatedResourcesBuilder))
        }

        viewModelScope.launch(Dispatchers.IO) {
            driveService.listFiles(INCLUSIMAP_PARAGOMINAS_PLACE_DATA_FOLDER_ID)
                .onSuccess { places ->
                    places.find { it.name.extractPlaceID() == _state.value.currentPlace.id }
                        .also { place ->
                            val placeJson = driveService.getFileContent(place?.id ?: return@launch)
                            val placeString = json.decodeFromString<AccessibleLocalMarker>(
                                placeJson?.decodeToString() ?: return@launch,
                            )
                            val updatedPlace = placeString.copy(resources = updatedResourcesBuilder)
                            val updatedPlaceString = json.encodeToString(updatedPlace)
                            driveService.updateFile(
                                place.id ?: return@launch,
                                placeString.id + "_" + placeString.authorEmail + ".json",
                                updatedPlaceString.byteInputStream(),
                            )
                            addNewContribution(
                                Contribution(
                                    fileId = place.id ?: return@launch,
                                    type = ContributionType.ACCESSIBLE_RESOURCES,
                                ),
                            )
                        }
                }
        }
    }

    private suspend fun addNewContribution(contribution: Contribution) =
        contributionsRepository.addNewContribution(contribution)

    private suspend fun addNewContributions(contributions: List<Contribution>) =
        contributionsRepository.addNewContributions(contributions)

    private suspend fun removeContribution(contribution: Contribution) =
        contributionsRepository.removeContribution(contribution)
}
