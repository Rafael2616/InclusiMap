package com.rafael.inclusimap.feature.map.placedetails.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafael.inclusimap.core.services.AwsFileApiService
import com.rafael.inclusimap.core.services.PlacesApiService
import com.rafael.inclusimap.core.util.map.Constants.INCLUSIMAP_IMAGE_FOLDER_PATH
import com.rafael.inclusimap.core.util.map.Constants.INCLUSIMAP_PARAGOMINAS_PLACE_DATA_FOLDER_PATH
import com.rafael.inclusimap.core.util.map.extractPlaceID
import com.rafael.inclusimap.core.util.map.extractUserEmail
import com.rafael.inclusimap.core.util.map.model.AccessibilityResource
import com.rafael.inclusimap.core.util.map.model.AccessibleLocalMarker
import com.rafael.inclusimap.core.util.map.model.Comment
import com.rafael.inclusimap.core.util.map.model.FullAccessibleLocalMarker
import com.rafael.inclusimap.core.util.map.model.PlaceImage
import com.rafael.inclusimap.core.util.map.model.Resource
import com.rafael.inclusimap.core.util.map.model.toAccessibleLocalMarker
import com.rafael.inclusimap.core.util.map.model.toFullAccessibleLocalMarker
import com.rafael.inclusimap.core.util.compressByteArray
import com.rafael.inclusimap.feature.contributions.domain.model.Contribution
import com.rafael.inclusimap.feature.contributions.domain.model.ContributionType
import com.rafael.inclusimap.feature.contributions.domain.repository.ContributionsRepository
import com.rafael.inclusimap.feature.map.map.domain.model.MapConstants.MAX_IMAGE_NUMBER
import com.rafael.inclusimap.feature.map.placedetails.domain.model.PlaceDetailsEvent
import com.rafael.inclusimap.feature.map.placedetails.domain.model.PlaceDetailsState
import com.rafael.libs.maps.interop.model.MapsLatLng
import kotlin.time.Clock.System
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class PlaceDetailsViewModel(
    private val awsService: AwsFileApiService,
    private val placesApiService: PlacesApiService,
    private val contributionsRepository: ContributionsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(PlaceDetailsState())
    val state = _state.asStateFlow()
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    fun onEvent(event: PlaceDetailsEvent) {
        when (event) {
            is PlaceDetailsEvent.OnUploadPlaceImages -> onUploadPlaceImages(
                event.imagesContent,
                event.placeId,
                event.userEmail,
            )

            PlaceDetailsEvent.OnDestroyPlaceDetails -> onDestroyPlaceDetailsScreen()
            is PlaceDetailsEvent.SetCurrentPlace -> setCurrentPlace(event.place, event.userEmail)
            is PlaceDetailsEvent.OnDeletePlaceImage -> onDeletePlaceImage(event.image)
            is PlaceDetailsEvent.SetUserAccessibilityRate -> setUserAccessibilityRate(event.rate)
            is PlaceDetailsEvent.OnSendComment -> onSendComment(
                event.comment,
                event.userEmail,
                event.userName,
            )

            is PlaceDetailsEvent.SetIsUserCommented -> _state.update { it.copy(isUserCommented = event.isCommented) }
            is PlaceDetailsEvent.OnDeleteComment -> onDeleteComment(event.userEmail)
            is PlaceDetailsEvent.SetIsEditingPlace -> _state.update { it.copy(isEditingPlace = event.isEditing) }
            is PlaceDetailsEvent.OnUpdatePlaceAccessibilityResources -> onUpdatePlaceAccessibilityResources(
                event.resources,
                event.userEmail,
            )

            is PlaceDetailsEvent.SetIsEditingComment -> _state.update { it.copy(isEditingComment = event.isEditing) }
            is PlaceDetailsEvent.SetIsTrySendComment -> _state.update { it.copy(trySendComment = event.isTrying) }
            is PlaceDetailsEvent.GetCurrentNearestPlaceUri -> getNearestPlaceUri(event.latLng)
        }
    }

    private fun setCurrentPlace(local: AccessibleLocalMarker, userEmail: String) {
        val place = state.value.loadedPlaces.find { existingPlace -> existingPlace.id == local.id }
        val fullPlace = place ?: local.toFullAccessibleLocalMarker(
            images = emptyList(),
            imageFolder = null,
            imageFolderId = null,
        )
        _state.update {
            it.copy(
                isCurrentPlaceLoaded = place != null,
                allImagesLoaded = place?.images?.isNotEmpty() == true,
                currentPlace = fullPlace,
                loadedPlaces = if (place?.id !in state.value.loadedPlaces.map { it.id }) {
                    state.value.loadedPlaces + fullPlace
                } else {
                    state.value.loadedPlaces
                },
            )
        }
        if (!state.value.isCurrentPlaceLoaded || state.value.loadedPlaces.find { existingPlace -> existingPlace.id == place?.id }?.images?.isEmpty() == true) {
            loadImages(local)
        } else {
            loadImagesFromCache(local)
        }
        loadUserComment(local, userEmail)
    }

    private fun loadUserComment(place: AccessibleLocalMarker, userEmail: String) {
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
        val imageFolderID =
            "$INCLUSIMAP_IMAGE_FOLDER_PATH/${placeDetails.id}_${placeDetails.authorEmail}"
        _state.update {
            it.copy(
                allImagesLoaded = false,
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
        viewModelScope.launch(Dispatchers.IO) {
            if (_state.value.currentPlace.imageFolderId.isNullOrEmpty() ||
                awsService.listFiles(_state.value.currentPlace.imageFolderId ?: return@launch)
                    .getOrNull()
                    ?.isEmpty() == true
            ) {
                _state.update { it.copy(allImagesLoaded = true) }
                println("No images found for place ${placeDetails.title} ${placeDetails.id}")
                return@launch
            }
            val folderId = state.value.currentPlace.imageFolderId
            folderId?.let {
                awsService.listFiles(folderId).onSuccess { imageFolder ->
                    _state.update {
                        it.copy(
                            currentPlace = it.currentPlace.copy(imageFolder = imageFolder),
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
            _state.value.currentPlace.imageFolder?.mapIndexed { index, file ->
                async {
                    // Is expected that this condition never succeeds, but is important to ensure
                    // that no more images that max limit loads, as it can cause UI lags, and much processing
                    if (index >= MAX_IMAGE_NUMBER) {
                        _state.update {
                            it.copy(allImagesLoaded = true)
                        }
                        println("Max image limit reached, skipping image $index")
                        return@async
                    }

                    try {
                        val fileContent =
                            awsService.downloadImage("$imageFolderID/$file").getOrNull()

                        if (placeDetails.id != _state.value.currentPlace.id) {
                            return@async
                        }
                        if (file.hashCode() in _state.value.currentPlace.images.map { it?.hashCode() }) {
                            println("Skipping already loaded image $file")
                            return@async
                        }

                        val placeImage = PlaceImage(
                            userEmail = file?.extractUserEmail(),
                            image = fileContent ?: return@async,
                            placeID = placeDetails.id ?: return@async,
                            name = file ?: return@async,
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
                            )
                        }
                        println("Loaded image $index with name $file")
                        if (_state.value.currentPlace.images.size == _state.value.currentPlace.imageFolder?.size) {
                            _state.update {
                                it.copy(allImagesLoaded = true)
                            }
                            println("All images cached successfully for ${placeDetails.title} ${placeDetails.id} + size: ${_state.value.currentPlace.images.size}")
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }?.awaitAll()
        }
    }

    private fun loadImagesFromCache(placeDetails: AccessibleLocalMarker) {
        val place = state.value.loadedPlaces.find { place -> place.id == placeDetails.id }
        val placeWithImages = placeDetails.toFullAccessibleLocalMarker(
            place?.images ?: emptyList(),
            place?.imageFolder,
            place?.imageFolderId,
        )
        println("All images founded:" + state.value.loadedPlaces.find { place -> place.id == placeDetails.id }?.images?.size)
        _state.update {
            it.copy(currentPlace = placeWithImages)
        }
        checkAllImagesLoaded(
            place = placeDetails,
            currentImagesSize = state.value.currentPlace.images.size,
            imagesLoadedSize = placeWithImages.imageFolder?.size,
        )
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
            _state.update { it.copy(allImagesLoaded = true) }
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun onUploadPlaceImages(
        images: List<ByteArray>,
        placeId: String,
        userEmail: String,
    ) {
        _state.update {
            it.copy(
                imagesUploadedSize = 0,
                isUploadingImages = true,
                isErrorUploadingImages = false,
                imagesToUploadSize = images.size,
            )
        }

        viewModelScope.launch(Dispatchers.IO) {
            awsService.listFiles(INCLUSIMAP_IMAGE_FOLDER_PATH).onSuccess { placeImagesFolder ->
                val placeImageFolderExists =
                    placeImagesFolder.find { it.extractPlaceID() == placeId }
                val folderId =
                    INCLUSIMAP_IMAGE_FOLDER_PATH + "/" + _state.value.currentPlace.id + "_" + _state.value.currentPlace.authorEmail
                _state.update {
                    it.copy(
                        currentPlace = it.currentPlace.copy(
                            imageFolderId = placeImageFolderExists ?: folderId,
                        ),
                    )
                }
            }

            var imagesFileIds = emptyList<String?>()
            val compressedImages = images.map { compressByteArray(it) }
            compressedImages.mapIndexed { index, image ->
                val imageFileName = "${_state.value.currentPlace.id}_$userEmail-${
                    System.now().toEpochMilliseconds()
                }.jpg"
                val imageFilePath = "${_state.value.currentPlace.imageFolderId}/$imageFileName"

                async {
                    val isSuccessful = awsService.uploadImage(imageFilePath, image).getOrNull()

                    if (isSuccessful != 200) {
                        println("Error uploading image $imageFilePath")
                        _state.update {
                            it.copy(isErrorUploadingImages = true)
                        }
                    }

                    val image = PlaceImage(
                        userEmail = userEmail,
                        image = image,
                        placeID = placeId,
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
                    imagesFileIds = (imagesFileIds + imageFileName).distinct()
                    if (index == images.size - 1) {
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
        val imagePath =
            "$INCLUSIMAP_IMAGE_FOLDER_PATH/${state.value.currentPlace.id}_${state.value.currentPlace.authorEmail}/${image.name}"
        println("Working on folder id: $imagePath")
        viewModelScope.launch(Dispatchers.IO) {
            awsService.deleteFile(imagePath)
            removeContribution(
                Contribution(
                    fileId = imagePath,
                    type = ContributionType.IMAGE,
                ),
            )
        }
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
                isDeletingImage = false,
                isImageDeleted = true,
            )
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun onSendComment(comment: String, userEmail: String, userName: String) {
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
                postDate = System.now().toEpochMilliseconds().toString(),
                id = _state.value.currentPlace.comments.size.plus(1),
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
                userCommentDate = System.now().toEpochMilliseconds().toString(),
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
            val placePath =
                INCLUSIMAP_PARAGOMINAS_PLACE_DATA_FOLDER_PATH + "/" + _state.value.currentPlace.id + "_" + _state.value.currentPlace.authorEmail + ".json"
            val place = awsService.downloadFile(placePath).getOrNull()

            val placeString = json.decodeFromString<AccessibleLocalMarker>(
                place?.decodeToString() ?: return@launch,
            )
            val filteredComments =
                placeString.comments.filterNot { it.email == userEmail }
            val updatedPlace =
                placeString.copy(comments = filteredComments + userComment)
            val updatedPlaceString = json.encodeToString(updatedPlace)
            awsService.uploadFile(
                placePath,
                updatedPlaceString,
            )
            addNewContribution(
                Contribution(
                    fileId = placeString.id!!,
                    type = ContributionType.COMMENT,
                ),
            )
        }
    }

    private fun onDeleteComment(userEmail: String) {
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
            val place = state.value.currentPlace
            val updatedPlace = place.toAccessibleLocalMarker().copy(
                comments = place.comments.filterNot { it.email == userEmail },
            )
            val updatedPlaceString = json.encodeToString(updatedPlace)

            awsService.uploadFile(
                INCLUSIMAP_PARAGOMINAS_PLACE_DATA_FOLDER_PATH + "/" + _state.value.currentPlace.id + place.id + "_" + place.authorEmail + ".json",
                updatedPlaceString,
            )

            removeContribution(
                Contribution(
                    fileId = place.id!!,
                    type = ContributionType.COMMENT,
                ),
            )
        }
    }

    private fun setUserAccessibilityRate(rate: Int) {
        if (_state.value.isUserCommented && !_state.value.isEditingComment) return
        _state.update {
            it.copy(userAccessibilityRate = rate)
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun onUpdatePlaceAccessibilityResources(
        updatedResources: List<Resource>,
        userEmail: String,
    ) {
        val updatedResourcesBuilder = updatedResources.map { resource ->
            AccessibilityResource(
                resource = resource,
                lastModified = System.now().toEpochMilliseconds().toString(),
                lastModifiedBy = userEmail,
            )
        }

        _state.update {
            it.copy(currentPlace = it.currentPlace.copy(resources = updatedResourcesBuilder))
        }

        viewModelScope.launch(Dispatchers.IO) {
            val placePath =
                INCLUSIMAP_PARAGOMINAS_PLACE_DATA_FOLDER_PATH + "/" + _state.value.currentPlace.id + "_" + _state.value.currentPlace.authorEmail + ".json"
            val updatedPlace = state.value.currentPlace.copy(resources = updatedResourcesBuilder)
            val updatedPlaceString = json.encodeToString(updatedPlace.toAccessibleLocalMarker())
            awsService.uploadFile(
                placePath,
                updatedPlaceString,
            )
            addNewContribution(
                Contribution(
                    fileId = updatedPlace.id!!,
                    type = ContributionType.ACCESSIBLE_RESOURCES,
                ),
            )
        }
    }

    private fun getNearestPlaceUri(latLng: MapsLatLng) {
        _state.update { it.copy(nearestPlaceUri = null) }
        viewModelScope.launch(Dispatchers.IO) {
            _state.update {
                it.copy(nearestPlaceUri = placesApiService.getNearestPlaceId(latLng))
            }
        }
    }

    private suspend fun addNewContribution(contribution: Contribution) =
        contributionsRepository.addNewContributions(listOf(contribution))

    private suspend fun addNewContributions(contributions: List<Contribution>) =
        contributionsRepository.addNewContributions(contributions)

    private suspend fun removeContribution(contribution: Contribution) =
        contributionsRepository.removeContribution(contribution)
}
