package com.rafael.inclusimap.feature.contributions.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafael.inclusimap.core.services.AwsFileApiService
import com.rafael.inclusimap.core.util.map.Constants.INCLUSIMAP_PARAGOMINAS_PLACE_DATA_FOLDER_PATH
import com.rafael.inclusimap.core.util.map.formatDate
import com.rafael.inclusimap.core.util.map.model.AccessibleLocalMarker
import com.rafael.inclusimap.core.util.map.model.PlaceImage
import com.rafael.inclusimap.core.util.map.removeTime
import com.rafael.inclusimap.feature.auth.domain.repository.LoginRepository
import com.rafael.inclusimap.feature.contributions.domain.ContributionsState
import com.rafael.inclusimap.feature.contributions.domain.model.AccessibleLocalMarkerWithFileId
import com.rafael.inclusimap.feature.contributions.domain.model.CommentWithPlace
import com.rafael.inclusimap.feature.contributions.domain.model.Contribution
import com.rafael.inclusimap.feature.contributions.domain.model.ContributionType
import com.rafael.inclusimap.feature.contributions.domain.model.ContributionsEvent
import com.rafael.inclusimap.feature.contributions.domain.model.ContributionsSize
import com.rafael.inclusimap.feature.contributions.domain.model.PlaceImageWithPlace
import com.rafael.inclusimap.feature.contributions.domain.repository.ContributionsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class ContributionsViewModel(
    private val loginRepository: LoginRepository,
    private val awsService: AwsFileApiService,
    private val contributionsRepository: ContributionsRepository,
) : ViewModel() {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }
    private val _state = MutableStateFlow(ContributionsState())
    val state = _state.asStateFlow()

    fun onEvent(event: ContributionsEvent) {
        when (event) {
            is ContributionsEvent.LoadUserContributions -> loadUserContributions()
        }
    }

    private fun loadUserContributions() {
        viewModelScope.launch(Dispatchers.IO) {
            _state.update {
                it.copy(
                    isLoadingContributions = true,
                    errorWhileConnectingToServer = false,
                )
            }
            val userEmail = loginRepository.getLoginInfo(1)?.userEmail
            val contributionsFile =
                awsService.downloadFile("Users/$userEmail/contributions.json").getOrNull() ?: run {
                    _state.update {
                        it.copy(errorWhileConnectingToServer = true)
                    }
                    null
                }

            val userContributions = json.decodeFromString<List<Contribution>>(
                contributionsFile?.decodeToString() ?: return@launch,
            )
            val commentsContributions =
                userContributions.filter { it.type == ContributionType.COMMENT }
            val placesContributions =
                userContributions.filter { it.type == ContributionType.PLACE }
            val imageContributions =
                userContributions.filter { it.type == ContributionType.IMAGE }
            val resourcesContributions =
                userContributions.filter { it.type == ContributionType.ACCESSIBLE_RESOURCES }

            _state.update {
                it.copy(
                    contributionsSize = ContributionsSize(
                        comments = commentsContributions.size,
                        places = placesContributions.size,
                        images = imageContributions.size,
                        resources = resourcesContributions.size,
                    ),
                )
            }
            if (!_state.value.allCommentsContributionsLoaded || state.value.userContributions.comments.size != commentsContributions.size) {
                loadCommentContributions(commentsContributions)
            }

            if (!_state.value.allPlacesContributionsLoaded || state.value.userContributions.places.size != placesContributions.size) {
                loadPlaceContributions(placesContributions)
            }

            if (!_state.value.allImagesContributionsLoaded || state.value.userContributions.images.size != imageContributions.size) {
                loadImageContributions(
                    userEmail ?: return@launch,
                    imageContributions,
                )
            }
            if (!_state.value.allResourcesContributionsLoaded || state.value.userContributions.resources.size != resourcesContributions.size) {
                loadResourcesContributions(resourcesContributions)
            }
            removeInexistentContributions(
                imageContributions,
                placesContributions,
                commentsContributions,
            )
        }.invokeOnCompletion {
            _state.update {
                it.copy(
                    isLoadingContributions = false,
                    shouldRefresh = false,
                )
            }
        }
    }

    private val inProgressFileIds = mutableSetOf<String>()

    private fun loadImageContributions(
        userEmail: String,
        contributions: List<Contribution>,
    ) {
        _state.update { it.copy(allImagesContributionsLoaded = false) }
        viewModelScope.launch(Dispatchers.IO) {
            val batchSize = 8
            contributions.chunked(batchSize).forEach { batch ->
                val deferreds = batch.map { contribution ->
                    async {
                        if (contribution.fileId in inProgressFileIds || contribution.fileId in state.value.userContributions.images.map { it.placeImage.name }) {
                            println("Skipping already loaded image: ${contribution.fileId}")
                            return@async
                        }

                        inProgressFileIds.add(contribution.fileId)
                        try {
                            val place =
                                awsService.downloadFile("PlaceData/Paragominas-PA/${contribution.fileId}")
                                    .getOrNull()
                            val placeFile = runCatching {
                                json.decodeFromString<AccessibleLocalMarker>(
                                    place?.decodeToString() ?: return@async,
                                )
                            }.getOrNull()
                            println("Place ID: ${placeFile?.id} for image contribution: ${contribution.fileId}")

                            awsService.listFiles("PlaceImages/${placeFile?.id}").getOrNull()
                                ?.let { content ->
                                    println("Image founded for contribution: ${contribution.fileId}")
                                    if (!content.contains(loginRepository.getLoginInfo(1)?.userEmail)) return@async
                                    awsService.downloadImage("PlaceImages/${placeFile?.id}/$content")
                                        .getOrNull()?.also { image ->
                                            _state.update {
                                                it.copy(
                                                    userContributions = it.userContributions.copy(
                                                        images = it.userContributions.images + PlaceImageWithPlace(
                                                            placeImage = PlaceImage(
                                                                image = image,
                                                                userEmail = userEmail,
                                                                placeID = placeFile?.id ?: return@async,
                                                                name = contribution.fileId,
                                                            ),
                                                            date = placeFile.time.removeTime()
                                                                ?.formatDate(),
                                                            place = loadPlaceById(
                                                                placeFile.id ?: return@async,
                                                            )
                                                                ?: return@async,
                                                            fileId = contribution.fileId,
                                                        ),
                                                    ),
                                                )
                                            }
                                        }
                                }
                        } finally {
                            inProgressFileIds.remove(contribution.fileId)
                        }
                    }
                }
                deferreds.awaitAll()
            }
        }.invokeOnCompletion {
            _state.update {
                it.copy(
                    allImagesContributionsLoaded = true,
                    userContributions = it.userContributions.copy(
                        images = it.userContributions.images.filter { imagesWithFileId ->
                            imagesWithFileId.fileId in contributions.map { it.fileId }
                        },
                    ),
                )
            }
            println("Loaded all images contributions + size: ${state.value.userContributions.images.size}")
        }
    }

    private fun loadCommentContributions(
        contributions: List<Contribution>,
    ) {
        _state.update { it.copy(allCommentsContributionsLoaded = false) }
        viewModelScope.launch(Dispatchers.IO) {
            val userEmail = loginRepository.getLoginInfo(1)?.userEmail
            contributions.map { contribution ->
                async {
                    val placeFile =
                        awsService.downloadFile("PlaceData/Paragominas-PA/${contribution.fileId}")
                            .getOrNull()

                    val place = runCatching {
                        json.decodeFromString<AccessibleLocalMarker>(
                            placeFile?.decodeToString() ?: return@async,
                        )
                    }.getOrNull()
                    val userComment =
                        place?.comments?.filterNot { it in state.value.userContributions.comments.map { it.comment } }
                            ?.find { it.email == userEmail }

                    _state.update {
                        it.copy(
                            userContributions = it.userContributions.copy(
                                comments = it.userContributions.comments + CommentWithPlace(
                                    comment = userComment ?: return@async,
                                    place = place,
                                    fileId = contribution.fileId,
                                ),
                            ),
                        )
                    }
                }
            }.awaitAll()
        }.invokeOnCompletion {
            _state.update {
                it.copy(
                    allCommentsContributionsLoaded = true,
                    userContributions = it.userContributions.copy(
                        comments = it.userContributions.comments.filter { commentsWithFileId ->
                            commentsWithFileId.fileId in contributions.map { it.fileId }
                        },
                    ),
                )
            }
            println("Loaded all comments contributions")
        }
    }

    private fun loadResourcesContributions(
        contributions: List<Contribution>,
    ) {
        _state.update { it.copy(allResourcesContributionsLoaded = false) }
        viewModelScope.launch(Dispatchers.IO) {
            contributions.map { contribution ->
                async {
                    val placeFile =
                        awsService.downloadFile("PlaceData/Paragominas-PA/${contribution.fileId}")
                            .getOrNull()
                    val place = runCatching {
                        json.decodeFromString<AccessibleLocalMarker>(
                            placeFile?.decodeToString() ?: return@async,
                        )
                    }.getOrNull()
                    _state.update {
                        it.copy(
                            userContributions = it.userContributions.copy(
                                resources = it.userContributions.resources + AccessibleLocalMarkerWithFileId(
                                    place = place ?: return@async,
                                    fileId = contribution.fileId,
                                ),
                            ),
                        )
                    }
                }
            }.awaitAll()
        }.invokeOnCompletion {
            _state.update {
                it.copy(
                    allResourcesContributionsLoaded = true,
                    userContributions = it.userContributions.copy(
                        resources = it.userContributions.resources.filter { resourcesWithFileId ->
                            resourcesWithFileId.fileId in contributions.map { it.fileId }
                        },
                    ),
                )
            }
            println("Loaded all resources contributions")
        }
    }

    private fun loadPlaceContributions(
        contributions: List<Contribution>,
    ) {
        _state.update { it.copy(allPlacesContributionsLoaded = false) }
        viewModelScope.launch(Dispatchers.IO) {
            contributions.map { contribution ->
                async {
                    val placeFile =
                        awsService.downloadFile("PlaceData/paragominas-PA/${contribution.fileId}")
                            .getOrNull()

                    val place =
                      runCatching {
                          json.decodeFromString<AccessibleLocalMarker>(
                              placeFile?.decodeToString() ?: return@async,
                          )
                      }.getOrNull()
                    if (place?.id in state.value.userContributions.places.map { it.place.id }) return@async
                    _state.update {
                        it.copy(
                            userContributions = it.userContributions.copy(
                                places = it.userContributions.places + AccessibleLocalMarkerWithFileId(
                                    place = place ?: return@async,
                                    fileId = contribution.fileId,
                                ),
                            ),
                        )
                    }
                }
            }.awaitAll()
        }.invokeOnCompletion {
            _state.update {
                it.copy(
                    allPlacesContributionsLoaded = true,
                    userContributions = it.userContributions.copy(
                        places = it.userContributions.places.filter { placeWithFileId ->
                            placeWithFileId.fileId in contributions.map { it.fileId }
                        },
                    ),
                )
            }
            println("Loaded all places contributions")
        }
    }

    private fun removeInexistentPlacesContributions(contributions: List<Contribution>) {
        viewModelScope.launch(Dispatchers.IO) {
            contributions.forEach {
                val placeExists =
                    awsService.downloadFile("PlaceData/paragominas-PA/${it.fileId}").getOrNull()
                if (placeExists != null) return@forEach
                removeContribution(
                    Contribution(
                        fileId = it.fileId,
                        type = ContributionType.PLACE,
                    ),
                )
                println("Removed inexistent place contribution: ${it.fileId}")
            }
        }
    }

    private fun removeInexistentCommentsContributions(contributions: List<Contribution>) {
        viewModelScope.launch(Dispatchers.IO) {
            contributions.forEach {
                val placeExists =
                    awsService.downloadFile("PlaceData/paragominas-PA/${it.fileId}").getOrNull()
                if (placeExists != null) return@forEach
                removeContribution(
                    Contribution(
                        fileId = it.fileId,
                        type = ContributionType.COMMENT,
                    ),
                )
                println("Removed inexistent comment contribution: ${it.fileId}")
            }
        }
    }

    private fun removeInexistentImageContributions(contributions: List<Contribution>) {
        viewModelScope.launch(Dispatchers.IO) {
            contributions.forEach { contribution ->
                val image =
                    awsService.downloadImage("PlaceImages/${contribution.fileId}").getOrNull()
                if (image != null) return@forEach
                removeContribution(
                    Contribution(
                        fileId = contribution.fileId,
                        type = ContributionType.IMAGE,
                    ),
                )
                println("Removed inexistent image contribution: ${contribution.fileId}")
            }
        }
    }

    private fun removeInexistentContributions(
        imageContributions: List<Contribution>,
        placesContributions: List<Contribution>,
        commentsContributions: List<Contribution>,
    ) {
        removeInexistentImageContributions(imageContributions)
        removeInexistentPlacesContributions(placesContributions)
        removeInexistentCommentsContributions(commentsContributions)
    }

    private suspend fun loadPlaceById(placeID: String): AccessibleLocalMarker? {
        var place: AccessibleLocalMarker? = null
        return withContext(Dispatchers.IO) {
            awsService.downloadFile("$INCLUSIMAP_PARAGOMINAS_PLACE_DATA_FOLDER_PATH/$placeID")
                .onSuccess { file ->
                    val placeFile =
                        json.decodeFromString<AccessibleLocalMarker>(file.decodeToString())
                    println("Place founded with ID: ${placeFile.id}")
                    place = placeFile
                }
            place
        }
    }

    private suspend fun removeContribution(contribution: Contribution) =
        contributionsRepository.removeContribution(contribution)
}
