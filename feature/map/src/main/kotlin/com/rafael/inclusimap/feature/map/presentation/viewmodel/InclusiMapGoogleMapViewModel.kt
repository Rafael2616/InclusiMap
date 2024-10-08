package com.rafael.inclusimap.feature.map.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.rafael.inclusimap.core.domain.model.AccessibleLocalMarker
import com.rafael.inclusimap.feature.map.domain.AccessibleLocalsEntity
import com.rafael.inclusimap.feature.map.domain.InclusiMapEvent
import com.rafael.inclusimap.feature.map.domain.InclusiMapState
import com.rafael.inclusimap.feature.map.domain.repository.AccessibleLocalsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class InclusiMapGoogleMapViewModel(
    private val accessibleLocalsRepository: AccessibleLocalsRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(InclusiMapState())
    val state = _state.asStateFlow()
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
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
            InclusiMapEvent.UseAppWithoutInternet -> _state.update { it.copy(failedToGetNewPlaces = false) }
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
        loadCachedPlaces()

        _state.update {
            it.copy(
                failedToLoadPlaces = false,
                failedToConnectToServer = false,
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
                println("Loaded ${state.value.allMappedPlaces.size} places from server")
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
    }
}
