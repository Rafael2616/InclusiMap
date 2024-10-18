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
import com.rafael.inclusimap.core.domain.network.onError
import com.rafael.inclusimap.core.domain.network.onSuccess
import com.rafael.inclusimap.core.domain.util.Constants.INCLUSIMAP_PARAGOMINAS_PLACE_DATA_FOLDER_ID
import com.rafael.inclusimap.core.services.GoogleDriveService
import com.rafael.inclusimap.feature.auth.domain.repository.LoginRepository
import com.rafael.inclusimap.feature.map.domain.AccessibleLocalsEntity
import com.rafael.inclusimap.feature.map.domain.CommentWithPlace
import com.rafael.inclusimap.feature.map.domain.Contribution
import com.rafael.inclusimap.feature.map.domain.ContributionType
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
        }
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
        viewModelScope.launch(Dispatchers.IO) {
            accessibleLocalsRepository.deleteAccessibleLocal(placeID)
            accessibleLocalsRepository.updateAccessibleLocalStored(
                AccessibleLocalsEntity(
                    id = 1,
                    locals = json.encodeToString<List<AccessibleLocalMarker>>(state.value.allMappedPlaces),
                ),
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
            driveService.listFiles(INCLUSIMAP_PARAGOMINAS_PLACE_DATA_FOLDER_ID).onSuccess {
                it.find { it.name.extractPlaceID() == placeID }?.also { contributionsFile ->
                    removeContribution(
                        Contribution(
                            fileId = contributionsFile.id,
                            type = ContributionType.PLACE,
                        ),
                    )
                }
            }
        }
    }

    private fun loadUserContributions(userEmail: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val userPathId = loginRepository.getLoginInfo(1)?.userPathID ?: return@launch
            driveService.listFiles(userPathId).onSuccess { userFiles ->
                userFiles.find { it.name == "contributions.json" }
                    ?.also { contributionsFile ->
                        val userContributionsString =
                            driveService.getFileContent(contributionsFile.id)
                                ?.decodeToString()
                        val userContributions = json.decodeFromString<List<Contribution>>(
                            userContributionsString ?: return@launch,
                        )

                        if (!_state.value.allCommentsContributionsLoaded) {
                            val commentsContributions =
                                userContributions.filter { it.type == ContributionType.COMMENT }
                            loadCommentContributions(commentsContributions)
                        }

                        if (!_state.value.allPlacesContributionsLoaded) {
                            val placesContributions =
                                userContributions.filter { it.type == ContributionType.PLACE }
                            loadPlaceContributions(placesContributions)
                        }

                        if (!_state.value.allImagesContributionsLoaded) {
                            val imageContributions =
                                userContributions.filter { it.type == ContributionType.IMAGE }
                            loadImageContributions(
                                userEmail,
                                imageContributions,
                            )
                        }
                    }
            }
        }
    }

    private fun loadImageContributions(
        userEmail: String,
        contributions: List<Contribution>,
    ) {
        _state.update { it.copy(allImagesContributionsLoaded = false) }
        viewModelScope.launch(Dispatchers.IO) {
            val options = BitmapFactory.Options()
            options.inSampleSize = 3
            contributions.map { contribution ->
                async {
                    val placeID = driveService.getFileMetadata(
                        contribution.fileId,
                    )?.name?.extractPlaceID()
                    driveService.getFileContent(contribution.fileId)
                        ?.let { content ->
                            BitmapFactory.decodeByteArray(
                                content,
                                0,
                                content.size,
                                options,
                            )?.asImageBitmap()?.let { img ->
                                if (img in state.value.userContributions.images.map { it.placeImage.image }) return@async
                                _state.update {
                                    it.copy(
                                        userContributions = it.userContributions.copy(
                                            images = it.userContributions.images + PlaceImageWithPlace(
                                                placeImage = PlaceImage(
                                                    image = img,
                                                    userEmail = userEmail,
                                                    placeID = placeID ?: return@async,
                                                    name = "",
                                                ),
                                                place = loadPlaceById(placeID)
                                                    ?: return@async,
                                            ),
                                        ),
                                    )
                                }
                            }
                        }
                }
            }.awaitAll()
        }.invokeOnCompletion {
            _state.update { it.copy(allImagesContributionsLoaded = true) }
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
                            val filteredComments = place.comments.filterNot { it in state.value.userContributions.comments.map { it.comment } }
                            _state.update {
                                it.copy(
                                    userContributions = it.userContributions.copy(
                                        comments = it.userContributions.comments + place.comments.filterNot { it in state.value.userContributions.comments.map { it.comment } }.map {
                                            CommentWithPlace(
                                                comment = it,
                                                place = place,
                                            )
                                        }
                                    ),
                                )
                            }
                        }
                }
            }.awaitAll()
        }.invokeOnCompletion {
            _state.update { it.copy(allCommentsContributionsLoaded = true) }
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
                            if (place.id in state.value.userContributions.places.map { it.id }) return@async
                            _state.update {
                                it.copy(
                                    userContributions = it.userContributions.copy(
                                        places = it.userContributions.places + place,
                                    ),
                                )
                            }
                        }
                }
            }
        }.invokeOnCompletion {
            _state.update { it.copy(allPlacesContributionsLoaded = true) }
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
                userFiles.find { it.name == "contributions.json" }
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
