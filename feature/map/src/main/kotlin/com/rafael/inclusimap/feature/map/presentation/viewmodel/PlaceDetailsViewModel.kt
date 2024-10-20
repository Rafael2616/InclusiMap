package com.rafael.inclusimap.feature.map.presentation.viewmodel

import android.content.Context
import android.graphics.Bitmap
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
import com.rafael.inclusimap.core.domain.model.util.extractPlaceID
import com.rafael.inclusimap.core.domain.model.util.extractUserEmail
import com.rafael.inclusimap.core.domain.network.Result
import com.rafael.inclusimap.core.domain.network.onSuccess
import com.rafael.inclusimap.core.domain.util.Constants.INCLUSIMAP_IMAGE_FOLDER_ID
import com.rafael.inclusimap.core.domain.util.Constants.INCLUSIMAP_PARAGOMINAS_PLACE_DATA_FOLDER_ID
import com.rafael.inclusimap.core.domain.util.Constants.MAX_IMAGE_NUMBER
import com.rafael.inclusimap.core.services.GoogleDriveService
import com.rafael.inclusimap.feature.auth.domain.repository.LoginRepository
import com.rafael.inclusimap.feature.map.domain.Contribution
import com.rafael.inclusimap.feature.map.domain.ContributionType
import com.rafael.inclusimap.feature.map.domain.PlaceDetailsEvent
import com.rafael.inclusimap.feature.map.domain.PlaceDetailsState
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
                event.imageFolderId,
                event.placeId,
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
                    userName = it.userName!!
                    userEmail = it.userEmail!!
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
                        driveService.listFiles(_state.value.currentPlace.imageFolderId!!)
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
                                    placeID = placeDetails.id!!,
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
                                        println("Loading image ${file.name}")
                                    }
                                }
                                if (_state.value.currentPlace.images.size == _state.value.currentPlace.imageFolder!!.size) {
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
        imageFolderId: String?,
        placeId: String,
    ) {
        _state.update {
            it.copy(
                imagesToUploadSize = null,
                imagesUploadedSize = 0,
                isUploadingImages = true,
                isErrorUploadingImages = false
            )
        }
        // Add the image to the list of images tobe showed in the app UI
        val options = BitmapFactory.Options()
        options.inSampleSize = 3
        val options2 = BitmapFactory.Options()
        options2.inSampleSize = 2

        val newImages = uris.map { uri ->
            PlaceImage(
                userEmail = userEmail,
                BitmapFactory.decodeStream(
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
                placeId,
                uri.lastPathSegment.toString(),
            )
        }
        _state.update {
            it.copy(
                currentPlace = it.currentPlace.copy(images = it.currentPlace.images + newImages),
                loadedPlaces = it.loadedPlaces.map { place ->
                    if (place.id == it.currentPlace.id) {
                        place.copy(images = it.currentPlace.images + newImages)
                    } else {
                        place
                    }
                },
                imagesToUploadSize = uris.size,
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
            async {
                if (imageFolderId.isNullOrEmpty()) {
                    if (imageFolderId != _state.value.currentPlace.imageFolderId) return@async
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

            var imagesFileIds = emptyList<String?>()
            uris.mapIndexed { index, uri ->
                val bitmap =
                    BitmapFactory.decodeStream(context.contentResolver.openInputStream(uri))
                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
                val compressedImage = outputStream.toByteArray()
                async {
                    if (imageFolderId != _state.value.currentPlace.imageFolderId) return@async
                    val imageId = driveService.uploadFile(
                        ByteArrayInputStream(compressedImage),
                        "${
                            _state.value.currentPlace.id
                        }_$userEmail-${Date().toInstant()}.jpg",
                        _state.value.currentPlace.imageFolderId
                            ?: throw IllegalStateException("Folder not found: Maybe an issue has occurred while creating the folder"),
                    )
                    _state.update { it.copy(imagesUploadedSize = index + 1) }
                    if (imageId == null) {
                        println("Error uploading image $index")
                        _state.update {
                            it.copy(
                                currentPlace = it.currentPlace.copy(images = it.currentPlace.images - newImages[index]),
                                loadedPlaces = it.loadedPlaces.map { place ->
                                    if (place.id == it.currentPlace.id) {
                                        place.copy(images = it.currentPlace.images - newImages[index])
                                    } else {
                                        place
                                    }
                                },
                                isErrorUploadingImages = true
                            )
                        }
                        imagesFileIds = imagesFileIds + imageId
                        return@async
                    }
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
                    removeContribution(
                        Contribution(
                            fileId = imageId,
                            type = ContributionType.IMAGE,
                        ),
                    )
                }

                is Result.Error -> {
                }
            }
        }
    }

    private fun onSendComment() {
        if (_state.value.userComment.isEmpty() || _state.value.userComment.length < 3 || _state.value.userAccessibilityRate == 0) {
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
        val userComment =
            Comment(
                postDate = Date().toInstant().toString(),
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
                userCommentDate = Date().toInstant().toString(),
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
            driveService.listFiles(INCLUSIMAP_PARAGOMINAS_PLACE_DATA_FOLDER_ID)
                .onSuccess { places ->
                    places.find { it.name.extractPlaceID() == _state.value.currentPlace.id }
                        .also { place ->
                            addNewContribution(
                                Contribution(
                                    fileId = place?.id ?: return@launch,
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
                            removeContribution(
                                Contribution(
                                    fileId = place?.id ?: return@launch,
                                    type = ContributionType.COMMENT,
                                ),
                            )
                        }
                }
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

    private fun addNewContribution(contribution: Contribution) {
        viewModelScope.launch(Dispatchers.IO) {
            val userPathId = loginRepository.getLoginInfo(1)?.userPathID ?: return@launch
            driveService.listFiles(userPathId).onSuccess { userFiles ->
                val userContributionsFile = userFiles.find { it.name == "contributions.json" }
                    ?.also { contributionsFile ->
                        val contributions =
                            driveService.getFileContent(contributionsFile.id)
                        val file = json.decodeFromString<List<Contribution>>(
                            contributions?.decodeToString() ?: return@launch,
                        )
                        if (file.any { it == contribution }) return@launch
                        val updatedContributions = file + contribution
                        val updatedContributionsString =
                            json.encodeToString(updatedContributions)
                        driveService.updateFile(
                            contributionsFile.id,
                            "contributions.json",
                            updatedContributionsString.byteInputStream(),
                        )
                        println("Contribution added successfully" + contribution.fileId)
                    }
                if (userContributionsFile == null) {
                    driveService.createFile(
                        "contributions.json",
                        "[]",
                        userPathId,
                    )
                }
            }
        }
    }

    private fun addNewContributions(contributions: List<Contribution>) {
        viewModelScope.launch(Dispatchers.IO) {
            val userPathId = loginRepository.getLoginInfo(1)?.userPathID ?: return@launch
            driveService.listFiles(userPathId).onSuccess { userFiles ->
                val userContributionsFile = userFiles.find { it.name == "contributions.json" }
                    ?.also { contributionsFile ->
                        val userContributions =
                            driveService.getFileContent(contributionsFile.id)
                        val file = json.decodeFromString<List<Contribution>>(
                            userContributions?.decodeToString() ?: return@launch,
                        )
                        val updatedContributions = file + contributions.filter { it !in file }
                        val updatedContributionsString =
                            json.encodeToString(updatedContributions)
                        driveService.updateFile(
                            contributionsFile.id,
                            "contributions.json",
                            updatedContributionsString.byteInputStream(),
                        )
                        println("Contribution added successfully: $contributions")
                    }
                if (userContributionsFile == null) {
                    driveService.createFile(
                        "contributions.json",
                        "[]",
                        userPathId,
                    )
                }
            }
        }
    }

    private fun removeContribution(contribution: Contribution) {
        viewModelScope.launch(Dispatchers.IO) {
            val userPathId = loginRepository.getLoginInfo(1)?.userPathID ?: return@launch
            driveService.listFiles(userPathId).onSuccess { userFiles ->
                userFiles.find { it.name == "contributions.json" }
                    ?.also { contributionsFile ->
                        val contributions =
                            driveService.getFileContent(contributionsFile.id)
                        val file = json.decodeFromString<List<Contribution>>(
                            contributions?.decodeToString() ?: return@launch,
                        )
                        val updatedContributions =
                            file.filter { it != contribution }
                        val updatedContributionsString =
                            json.encodeToString(updatedContributions)
                        driveService.updateFile(
                            contributionsFile.id,
                            "contributions.json",
                            updatedContributionsString.byteInputStream(),
                        )
                        println("Contribution removed successfully" + contribution.fileId)
                    }
            }
        }
    }
}
