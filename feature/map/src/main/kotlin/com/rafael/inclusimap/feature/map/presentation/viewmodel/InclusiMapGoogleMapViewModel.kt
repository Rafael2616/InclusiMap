package com.rafael.inclusimap.feature.map.presentation.viewmodel

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.rafael.inclusimap.core.domain.model.AccessibleLocalMarker
import com.rafael.inclusimap.core.domain.model.PlaceImage
import com.rafael.inclusimap.core.domain.model.util.extractPlaceID
import com.rafael.inclusimap.core.domain.model.util.formatDate
import com.rafael.inclusimap.core.domain.model.util.removeTime
import com.rafael.inclusimap.core.domain.network.onError
import com.rafael.inclusimap.core.domain.network.onSuccess
import com.rafael.inclusimap.core.domain.util.Constants.INCLUSIMAP_IMAGE_FOLDER_ID
import com.rafael.inclusimap.core.domain.util.Constants.INCLUSIMAP_PARAGOMINAS_PLACE_DATA_FOLDER_ID
import com.rafael.inclusimap.core.services.GoogleDriveService
import com.rafael.inclusimap.feature.auth.domain.repository.LoginRepository
import com.rafael.inclusimap.feature.map.domain.AccessibleLocalMarkerWithFileId
import com.rafael.inclusimap.feature.map.domain.AccessibleLocalsEntity
import com.rafael.inclusimap.feature.map.domain.CommentWithPlace
import com.rafael.inclusimap.feature.map.domain.Contribution
import com.rafael.inclusimap.feature.map.domain.ContributionType
import com.rafael.inclusimap.feature.map.domain.ContributionsSize
import com.rafael.inclusimap.feature.map.domain.InclusiMapEntity
import com.rafael.inclusimap.feature.map.domain.InclusiMapEvent
import com.rafael.inclusimap.feature.map.domain.InclusiMapState
import com.rafael.inclusimap.feature.map.domain.PlaceImageWithPlace
import com.rafael.inclusimap.feature.map.domain.repository.AccessibleLocalsRepository
import com.rafael.inclusimap.feature.map.domain.repository.InclusiMapRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class InclusiMapGoogleMapViewModel(
    private val accessibleLocalsRepository: AccessibleLocalsRepository,
    private val inclusiMapRepository: InclusiMapRepository,
    private val driveService: GoogleDriveService,
    private val loginRepository: LoginRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(InclusiMapState())
    val state = _state.asStateFlow()
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }
    private var userEmail: MutableStateFlow<String?> = MutableStateFlow(null)

    init {
        loadCachedPlaces()
        viewModelScope.launch(Dispatchers.IO) {
            userEmail.update { loginRepository.getLoginInfo(1)?.userEmail }
        }
    }

    fun onEvent(event: InclusiMapEvent) {
        when (event) {
            is InclusiMapEvent.UpdateMapCameraPosition -> updateMapCameraPosition(
                event.latLng,
                event.isMyLocationFounded,
            )

            InclusiMapEvent.OnLoadPlaces -> onLoadPlaces()
            InclusiMapEvent.OnMapLoad -> onMapLoad()
            is InclusiMapEvent.OnMappedPlaceSelected -> onMappedPlaceSelected(event.place)
            is InclusiMapEvent.OnUnmappedPlaceSelected -> onUnmappedPlaceSelected(event.latLng)
            is InclusiMapEvent.OnAddNewMappedPlace -> onAddNewMappedPlace(event.newPlace)
            is InclusiMapEvent.SetLocationPermissionGranted -> setLocationPermissionGranted(event.isGranted)
            is InclusiMapEvent.OnUpdateMappedPlace -> onUpdateMappedPlace(event.placeUpdated)
            is InclusiMapEvent.OnDeleteMappedPlace -> onDeleteMappedPlace(event.placeId)
            is InclusiMapEvent.OnFailToLoadPlaces -> onLoadPlaces()
            is InclusiMapEvent.OnFailToConnectToServer -> onLoadPlaces()
            InclusiMapEvent.UseAppWithoutInternet -> _state.update { it.copy(useAppWithoutInternet = true) }
            is InclusiMapEvent.ShouldAnimateMap -> _state.update { it.copy(shouldAnimateMap = event.shouldAnimate) }
            is InclusiMapEvent.UpdateMapState -> updateMapState(event.mapState)
            InclusiMapEvent.GetCurrentState -> getCurrentState()
            InclusiMapEvent.ResetState -> onResetState()
            is InclusiMapEvent.LoadUserContributions -> loadUserContributions(event.userEmail)
            is InclusiMapEvent.SetIsContributionsScreen -> _state.update {
                it.copy(
                    isContributionsScreen = event.isContributionsScreen,
                )
            }

            is InclusiMapEvent.SetCurrentPlaceById -> setPlaceById(event.placeId)
            is InclusiMapEvent.OnTravelToPlace -> onTravelToPlace(event.placeId)
            is InclusiMapEvent.SetShouldTravel -> _state.update {
                it.copy(shouldTravel = event.shouldTravel)
            }
        }
    }

    private fun onTravelToPlace(placeId: String) {
        setPlaceById(placeId)
        _state.update { it.copy(shouldTravel = true) }
    }

    private fun updateMapCameraPosition(latLng: LatLng, isMyLocationFounded: Boolean) {
        _state.update {
            it.copy(
                defaultLocationLatLng = latLng,
                isMyLocationFound = isMyLocationFounded,
            )
        }
    }

    private fun updateMapState(mapState: CameraPosition) {
        _state.update {
            it.copy(currentLocation = mapState)
        }
        viewModelScope.launch(Dispatchers.IO) {
            val currentState = inclusiMapRepository.getPosition(1) ?: InclusiMapEntity.getDefault()
            currentState.lat = mapState.target.latitude
            currentState.lng = mapState.target.longitude
            currentState.zoom = mapState.zoom
            currentState.tilt = mapState.tilt
            currentState.bearing = mapState.bearing

            inclusiMapRepository.updatePosition(currentState)
        }
    }

    private fun getCurrentState() {
        viewModelScope.launch(Dispatchers.IO) {
            val currentState = inclusiMapRepository.getPosition(1) ?: InclusiMapEntity.getDefault()
            _state.update {
                it.copy(
                    currentLocation = CameraPosition(
                        LatLng(currentState.lat, currentState.lng),
                        currentState.zoom,
                        currentState.tilt,
                        currentState.bearing,
                    ),
                )
            }
        }
    }

    private fun onResetState() {
        _state.update {
            InclusiMapState(isMapLoaded = true)
        }
        viewModelScope.launch(Dispatchers.IO) {
            accessibleLocalsRepository.updateAccessibleLocalStored(AccessibleLocalsEntity.getDefault())
        }
        onLoadPlaces()
    }

    private fun loadCachedPlaces() {
        viewModelScope.launch(Dispatchers.IO) {
            // Get the cached places from local database
            val accessibleLocalsEntity = accessibleLocalsRepository.getAccessibleLocalsStored(1)
                ?: AccessibleLocalsEntity.getDefault()
            _state.update {
                it.copy(
                    allMappedPlaces = json.decodeFromString<List<AccessibleLocalMarker>>(
                        accessibleLocalsEntity.locals,
                    ).also {
                        println("Loaded ${it.size} places from cache")
                    },
                )
            }
        }
    }

    private fun onLoadPlaces() {
        _state.update {
            it.copy(
                failedToLoadPlaces = false,
                failedToConnectToServer = false,
                failedToGetNewPlaces = false,
                useAppWithoutInternet = false,
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
            // try to fetch new and updated places from server
            accessibleLocalsRepository.getAccessibleLocals().let { mappedPlaces ->
                if (mappedPlaces == null) {
                    _state.update { it.copy(failedToConnectToServer = true) }
                    return@launch
                }
                if (mappedPlaces.isEmpty() && _state.value.allMappedPlaces.isEmpty()) {
                    _state.update { it.copy(failedToLoadPlaces = true) }
                    return@launch
                }
                if (mappedPlaces.isEmpty()) {
                    _state.update { it.copy(failedToGetNewPlaces = true) }
                    return@launch
                }
                _state.update { it.copy(allMappedPlaces = mappedPlaces) }
            }
        }.invokeOnCompletion {
            // Update the cache for places
            if (!_state.value.failedToConnectToServer && !_state.value.failedToLoadPlaces) {
                viewModelScope.launch(Dispatchers.IO) {
                    accessibleLocalsRepository.updateAccessibleLocalStored(
                        AccessibleLocalsEntity(
                            id = 1,
                            locals = json.encodeToString<List<AccessibleLocalMarker>>(state.value.allMappedPlaces),
                        ),
                    )
                }
            }
        }
    }

    private fun onMapLoad() {
        _state.update { it.copy(isMapLoaded = true) }
    }

    private fun onMappedPlaceSelected(place: AccessibleLocalMarker) {
        _state.update {
            it.copy(
                selectedMappedPlace = place,
            )
        }
    }

    private fun onUnmappedPlaceSelected(latLng: LatLng) {
        _state.update {
            it.copy(
                selectedUnmappedPlaceLatLng = latLng,
            )
        }
    }

    private fun onAddNewMappedPlace(newPlace: AccessibleLocalMarker) {
        _state.update {
            it.copy(
                allMappedPlaces = _state.value.allMappedPlaces + newPlace,
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
            accessibleLocalsRepository.saveAccessibleLocal(newPlace)
            accessibleLocalsRepository.updateAccessibleLocalStored(
                AccessibleLocalsEntity(
                    id = 1,
                    locals = json.encodeToString<List<AccessibleLocalMarker>>(state.value.allMappedPlaces),
                ),
            )
        }.invokeOnCompletion {
            viewModelScope.launch(Dispatchers.IO) {
                driveService.listFiles(INCLUSIMAP_PARAGOMINAS_PLACE_DATA_FOLDER_ID).onSuccess {
                    it.find { it.name.extractPlaceID() == newPlace.id }?.also { placeFile ->
                        addNewContribution(
                            Contribution(
                                fileId = placeFile.id,
                                type = ContributionType.PLACE,
                            ),
                        )
                    }
                    println("Founded place in server")
                }.onError {
                    println("Place not in server yet")
                }
            }
        }
    }

    private fun setLocationPermissionGranted(isGranted: Boolean) {
        _state.value = _state.value.copy(
            isLocationPermissionGranted = isGranted,
        )
    }

    private fun onUpdateMappedPlace(placeUpdated: AccessibleLocalMarker) {
        if (placeUpdated.id.isNullOrEmpty() || placeUpdated.id !in _state.value.allMappedPlaces.map { it.id }) return
        _state.update {
            it.copy(
                allMappedPlaces = _state.value.allMappedPlaces.map {
                    if (it.id == placeUpdated.id) {
                        placeUpdated
                    } else {
                        it
                    }
                },
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
            accessibleLocalsRepository.updateAccessibleLocal(placeUpdated)
            accessibleLocalsRepository.updateAccessibleLocalStored(
                AccessibleLocalsEntity(
                    id = 1,
                    locals = json.encodeToString<List<AccessibleLocalMarker>>(state.value.allMappedPlaces),
                ),
            )
        }
    }

    private fun onDeleteMappedPlace(placeID: String) {
        _state.update {
            it.copy(
                allMappedPlaces = _state.value.allMappedPlaces.filter { it.id != placeID },
            )
        }
        val fileId = driveService.getFileMetadata(placeID)?.id
        viewModelScope.launch(Dispatchers.IO) {
            accessibleLocalsRepository.deleteAccessibleLocal(placeID)
            accessibleLocalsRepository.updateAccessibleLocalStored(
                AccessibleLocalsEntity(
                    id = 1,
                    locals = json.encodeToString<List<AccessibleLocalMarker>>(state.value.allMappedPlaces),
                ),
            )
            removeContribution(
                Contribution(
                    fileId = fileId ?: return@launch,
                    type = ContributionType.PLACE,
                ),
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
            driveService.listFiles(INCLUSIMAP_IMAGE_FOLDER_ID).onSuccess {
                it.find { it.name.extractPlaceID() == placeID }?.also { placeImageFolder ->
                    driveService.listFiles(placeImageFolder.id).onSuccess { images ->
                        images.forEach { image ->
                            driveService.deleteFile(image.id)
                        }
                        val contributions = images.map { contribution ->
                            Contribution(
                                fileId = contribution.id,
                                type = ContributionType.IMAGE
                            )
                        }
                        removeContributions(contributions)
                    }
                }
            }
        }
    }

    private fun loadUserContributions(userEmail: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _state.update { it.copy(isLoadingContributions = true) }
            val userPathId = loginRepository.getLoginInfo(1)?.userPathID ?: return@launch
            driveService.listFiles(userPathId).onSuccess { userFiles ->
                val userContributionsFile = userFiles.find { it.name == "contributions.json" }
                    ?.also { contributionsFile ->
                        val userContributionsString =
                            driveService.getFileContent(contributionsFile.id)
                                ?.decodeToString()
                        val userContributions = json.decodeFromString<List<Contribution>>(
                            userContributionsString ?: return@launch,
                        )

                        val commentsContributions =
                            userContributions.filter { it.type == ContributionType.COMMENT }
                        val placesContributions =
                            userContributions.filter { it.type == ContributionType.PLACE }
                        val imageContributions =
                            userContributions.filter { it.type == ContributionType.IMAGE }

                        _state.update {
                            it.copy(
                                contributionsSize = ContributionsSize(
                                    comments = commentsContributions.size,
                                    places = placesContributions.size,
                                    images = imageContributions.size,
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
                                userEmail,
                                imageContributions,
                            )
                        }
                    }
                if (userContributionsFile == null) {
                    _state.update {
                        it.copy(
                            allCommentsContributionsLoaded = true,
                            allPlacesContributionsLoaded = true,
                            allImagesContributionsLoaded = true,
                        )
                    }
                    return@launch
                }
            }
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
            val options = BitmapFactory.Options().apply { inSampleSize = 3 }
            val batchSize = 5
            contributions.chunked(batchSize).forEach { batch ->
                val deferreds = batch.map { contribution ->
                    async {
                        if (contribution.fileId in inProgressFileIds || contribution.fileId in state.value.userContributions.images.map { it.placeImage.name }) {
                            println("Skipping already loaded image: ${contribution.fileId}")
                            return@async
                        }

                        inProgressFileIds.add(contribution.fileId)
                        try {
                            val placeMetadata = driveService.getFileMetadata(contribution.fileId)
                            val placeID = placeMetadata?.name?.extractPlaceID()

                            driveService.getFileContent(contribution.fileId)?.let { content ->
                                BitmapFactory.decodeByteArray(content, 0, content.size, options)
                                    ?.asImageBitmap()?.let { img ->
                                        _state.update {
                                            it.copy(
                                                userContributions = it.userContributions.copy(
                                                    images = it.userContributions.images + PlaceImageWithPlace(
                                                        placeImage = PlaceImage(
                                                            image = img,
                                                            userEmail = userEmail,
                                                            placeID = placeID ?: return@async,
                                                            name = contribution.fileId,
                                                        ),
                                                        date = placeMetadata.name.removeTime()
                                                            ?.formatDate(),
                                                        place = loadPlaceById(placeID)
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
            removeInexistentImageContributions(contributions)
        }
    }

    private fun loadCommentContributions(
        contributions: List<Contribution>,
    ) {
        _state.update { it.copy(allCommentsContributionsLoaded = false) }
        viewModelScope.launch(Dispatchers.IO) {
            contributions.map { contribution ->
                async {
                    driveService.getFileContent(contribution.fileId)
                        ?.also { content ->
                            val place =
                                json.decodeFromString<AccessibleLocalMarker>(content.decodeToString())
                            val filteredComments =
                                place.comments.filterNot { it in state.value.userContributions.comments.map { it.comment } }
                            _state.update {
                                it.copy(
                                    userContributions = it.userContributions.copy(
                                        comments = it.userContributions.comments + filteredComments.map {
                                            CommentWithPlace(
                                                comment = it,
                                                place = place,
                                                fileId = contribution.fileId,
                                            )
                                        },
                                    ),
                                )
                            }
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
        }
    }

    private fun loadPlaceContributions(
        contributions: List<Contribution>,
    ) {
        _state.update { it.copy(allPlacesContributionsLoaded = false) }
        viewModelScope.launch(Dispatchers.IO) {
            contributions.map { contribution ->
                async {
                    driveService.getFileContent(contribution.fileId)
                        ?.also { content ->
                            val place =
                                json.decodeFromString<AccessibleLocalMarker>(
                                    content.decodeToString(),
                                )
                            if (place.id in state.value.userContributions.places.map { it.place.id }) return@async
                            _state.update {
                                it.copy(
                                    userContributions = it.userContributions.copy(
                                        places = it.userContributions.places + AccessibleLocalMarkerWithFileId(
                                            place = place,
                                            fileId = contribution.fileId,
                                        ),
                                    ),
                                )
                            }
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
            removeInexistentPlacesAndCommentsContributions(contributions)
        }
    }

    private fun removeInexistentPlacesAndCommentsContributions(contributions : List<Contribution>) {
        viewModelScope.launch(Dispatchers.IO) {
            contributions.forEach {
                val placeExists = driveService.getFileMetadata(it.fileId)
                if (placeExists != null) return@forEach
                removeContribution(
                    Contribution(
                        fileId = it.fileId,
                        type = ContributionType.PLACE,
                    )
                )
            }
        }
    }

    private fun removeInexistentImageContributions(contributions: List<Contribution>) {
        viewModelScope.launch(Dispatchers.IO) {
            contributions.forEach {
                val imageExists = driveService.getFileMetadata(it.fileId)
                if (imageExists != null) return@forEach
                removeContribution(
                    Contribution(
                        fileId = it.fileId,
                        type = ContributionType.IMAGE,
                    )
                )
            }
        }
    }

    private suspend fun loadPlaceById(placeID: String): AccessibleLocalMarker? {
        var place: AccessibleLocalMarker? = null
        return withContext(Dispatchers.IO) {
            driveService.listFiles(INCLUSIMAP_PARAGOMINAS_PLACE_DATA_FOLDER_ID)
                .onSuccess {
                    val placeFileID =
                        it.find { it.name.extractPlaceID() == placeID }?.id
                            ?: return@withContext null
                    driveService.getFileContent(placeFileID)?.let { content ->
                        place =
                            json.decodeFromString<AccessibleLocalMarker>(content.decodeToString())
                    }
                }
            place
        }
    }

    private fun setPlaceById(placeID: String) {
        _state.update {
            it.copy(
                selectedMappedPlace = it.allMappedPlaces.find { it.id == placeID },
            )
        }
    }

    private fun addNewContribution(contribution: Contribution) {
        viewModelScope.launch(Dispatchers.IO) {
            val userPathId =
                loginRepository.getLoginInfo(1)?.userPathID ?: return@launch

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

    private fun removeContribution(contribution: Contribution) {
        viewModelScope.launch(Dispatchers.IO) {
            val userPathId =
                loginRepository.getLoginInfo(1)?.userPathID ?: return@launch

            driveService.listFiles(userPathId).onSuccess { userFiles ->
                userFiles.find { it.name == "contributions.json" }
                    ?.also { contributionsFile ->
                        val contributions =
                            driveService.getFileContent(contributionsFile.id)
                        val file = json.decodeFromString<List<Contribution>>(
                            contributions?.decodeToString() ?: return@launch,
                        )
                        val updatedContributions =
                            file.filter { if (contribution.type == ContributionType.PLACE) it.fileId != contribution.fileId else it != contribution }
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
    private fun removeContributions(contributions: List<Contribution>) {
        viewModelScope.launch(Dispatchers.IO) {
            val userPathId =
                loginRepository.getLoginInfo(1)?.userPathID ?: return@launch

            driveService.listFiles(userPathId).onSuccess { userFiles ->
                userFiles.find { it.name == "contributions.json" }
                    ?.also { contributionsFile ->
                        val userContributions =
                            driveService.getFileContent(contributionsFile.id)
                        val file = json.decodeFromString<List<Contribution>>(
                            userContributions?.decodeToString() ?: return@launch,
                        )
                        val updatedContributions = file.filter { it !in contributions }
                        val updatedContributionsString =
                            json.encodeToString(updatedContributions)
                        driveService.updateFile(
                            contributionsFile.id,
                            "contributions.json",
                            updatedContributionsString.byteInputStream(),
                        )
                        println("Contribution removed successfully: $contributions")
                    }
            }
        }
    }
}
