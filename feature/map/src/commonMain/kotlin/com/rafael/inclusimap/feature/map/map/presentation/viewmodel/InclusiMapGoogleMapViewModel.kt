package com.rafael.inclusimap.feature.map.map.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafael.inclusimap.core.services.AwsFileApiService
import com.rafael.inclusimap.core.util.map.Constants.INCLUSIMAP_IMAGE_FOLDER_PATH
import com.rafael.inclusimap.core.util.map.Constants.INCLUSIMAP_PARAGOMINAS_PLACE_DATA_FOLDER_PATH
import com.rafael.inclusimap.core.util.map.model.AccessibleLocalMarker
import com.rafael.inclusimap.feature.contributions.domain.model.Contribution
import com.rafael.inclusimap.feature.contributions.domain.model.ContributionType
import com.rafael.inclusimap.feature.contributions.domain.repository.ContributionsRepository
import com.rafael.inclusimap.feature.map.map.domain.model.AccessibleLocalsEntity
import com.rafael.inclusimap.feature.map.map.domain.model.InclusiMapEntity
import com.rafael.inclusimap.feature.map.map.domain.model.InclusiMapEvent
import com.rafael.inclusimap.feature.map.map.domain.model.InclusiMapState
import com.rafael.inclusimap.feature.map.map.domain.repository.AccessibleLocalsRepository
import com.rafael.inclusimap.feature.map.map.domain.repository.InclusiMapRepository
import com.rafael.libs.maps.interop.model.MapsCameraPosition
import com.rafael.libs.maps.interop.model.MapsLatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class InclusiMapGoogleMapViewModel(
    private val accessibleLocalsRepository: AccessibleLocalsRepository,
    private val inclusiMapRepository: InclusiMapRepository,
    private val awsService: AwsFileApiService,
    private val contributionsRepository: ContributionsRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(InclusiMapState())
    val state = _state.asStateFlow()
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    init {
        getCurrentState()
    }

    fun onEvent(event: InclusiMapEvent) {
        when (event) {
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
            InclusiMapEvent.ResetState -> onResetState()
            InclusiMapEvent.LoadCachedPlaces -> loadCachedPlaces()
            is InclusiMapEvent.SetIsContributionsScreen -> _state.update {
                it.copy(isContributionsScreen = event.isContributionsScreen)
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

    private fun updateMapState(mapState: MapsCameraPosition) {
        _state.update { it.copy(currentLocation = mapState) }
        viewModelScope.launch(Dispatchers.IO) {
            val currentState = inclusiMapRepository.getPosition(1) ?: InclusiMapEntity.getDefault()
            val updatedState = currentState.copy(
                lat = mapState.target.latitude,
                lng = mapState.target.longitude,
                zoom = mapState.zoom,
                tilt = mapState.tilt,
                bearing = mapState.bearing,
            )
            println("Updating map state: $updatedState")
            inclusiMapRepository.updatePosition(updatedState)
        }
    }

    private fun getCurrentState() {
        _state.update { it.copy(isStateRestored = false) }
        viewModelScope.launch(Dispatchers.IO) {
            val currentState = inclusiMapRepository.getPosition(1) ?: InclusiMapEntity.getDefault()
            _state.update {
                it.copy(
                    currentLocation = MapsCameraPosition(
                        tilt = currentState.tilt,
                        target = MapsLatLng(currentState.lat, currentState.lng),
                        bearing = currentState.bearing,
                        zoom = currentState.zoom,
                    ),
                    isStateRestored = true,
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
            val cachedPlaces = json.decodeFromString<List<AccessibleLocalMarker>>(
                accessibleLocalsEntity.locals,
            )
            _state.update { it.copy(allMappedPlaces = cachedPlaces) }
        }
    }

    private fun onLoadPlaces() {
        _state.update {
            it.copy(
                failedToLoadPlaces = false,
                failedToConnectToServer = false,
                failedToGetNewPlaces = false,
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
                if (mappedPlaces.isEmpty() && !_state.value.useAppWithoutInternet) {
                    _state.update { it.copy(failedToGetNewPlaces = true) }
                    return@launch
                }
                if (mappedPlaces.isEmpty()) return@launch

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
                _state.update { it.copy(useAppWithoutInternet = false) }
            }
        }
    }

    private fun onMapLoad() {
        _state.update { it.copy(isMapLoaded = true) }
    }

    private fun onMappedPlaceSelected(place: AccessibleLocalMarker) {
        _state.update { it.copy(selectedMappedPlace = place) }
    }

    private fun onUnmappedPlaceSelected(latLng: MapsLatLng) {
        _state.update { it.copy(selectedUnmappedPlaceLatLng = latLng) }
    }

    private fun onAddNewMappedPlace(newPlace: AccessibleLocalMarker) {
        _state.update {
            it.copy(
                isErrorAddingNewPlace = false,
                isAddingNewPlace = true,
                isPlaceAdded = false,
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
            val placeFileId = accessibleLocalsRepository.saveAccessibleLocal(newPlace)
            addNewContribution(
                Contribution(
                    fileId = placeFileId ?: return@launch,
                    type = ContributionType.PLACE,
                ),
            )
            val updatedPlaces = _state.value.allMappedPlaces + newPlace
            accessibleLocalsRepository.updateAccessibleLocalStored(
                AccessibleLocalsEntity(
                    id = 1,
                    locals = json.encodeToString<List<AccessibleLocalMarker>>(updatedPlaces),
                ),
            )
            _state.update {
                it.copy(
                    allMappedPlaces = updatedPlaces,
                    isErrorAddingNewPlace = false,
                    isAddingNewPlace = false,
                    isPlaceAdded = true,
                )
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
                isErrorDeletingPlace = false,
                isDeletingPlace = true,
                isPlaceDeleted = false,
            )
        }
        println("Initializing Deleting place job: $placeID")
        viewModelScope.launch(Dispatchers.IO) {
            val newLocals = state.value.allMappedPlaces.filter { it.id != placeID }
            awsService.deleteFile("$INCLUSIMAP_IMAGE_FOLDER_PATH/$placeID")
            awsService.listFiles(INCLUSIMAP_PARAGOMINAS_PLACE_DATA_FOLDER_PATH).onSuccess {
                accessibleLocalsRepository.deleteAccessibleLocal(placeID)
                accessibleLocalsRepository.updateAccessibleLocalStored(
                    AccessibleLocalsEntity(
                        id = 1,
                        locals = json.encodeToString<List<AccessibleLocalMarker>>(newLocals),
                    ),
                )
                _state.update {
                    it.copy(
                        isErrorDeletingPlace = false,
                        isDeletingPlace = false,
                        isPlaceDeleted = true,
                        allMappedPlaces = _state.value.allMappedPlaces.filter { it.id != placeID },
                    )
                }
                println("Place deleted successfully")
            }
            _state.update {
                it.copy(
                    isErrorDeletingPlace = false,
                    isDeletingPlace = false,
                    isPlaceDeleted = false,
                )
            }
            println("Job completed")
        }
    }

    private fun setPlaceById(placeID: String) {
        _state.update {
            it.copy(
                selectedMappedPlace = it.allMappedPlaces.find { it.id == placeID },
            )
        }
    }

    private suspend fun addNewContribution(contribution: Contribution) =
        contributionsRepository.addNewContributions(listOf(contribution))

    private suspend fun removeContribution(contribution: Contribution) =
        contributionsRepository.removeContribution(contribution)

    private suspend fun removeContributions(contributions: List<Contribution>) =
        contributionsRepository.removeContributions(contributions)
}
