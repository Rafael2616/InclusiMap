package com.rafael.inclusimap.feature.map.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.rafael.inclusimap.core.domain.model.AccessibleLocalMarker
import com.rafael.inclusimap.feature.map.domain.InclusiMapEvent
import com.rafael.inclusimap.feature.map.domain.InclusiMapState
import com.rafael.inclusimap.feature.map.domain.repository.AccessibleLocalsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class InclusiMapGoogleMapScreenViewModel(
    private val accessibleLocalsRepository: AccessibleLocalsRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(InclusiMapState())
    val state = _state.asStateFlow()

    fun onEvent(event: InclusiMapEvent) {
        when (event) {
            is InclusiMapEvent.UpdateMapCameraPosition -> updateMapCameraPosition(
                event.latLng,
                event.isMyLocationFounded
            )

            InclusiMapEvent.OnMapLoaded -> onMapLoaded()
            is InclusiMapEvent.OnMappedPlaceSelected -> onMappedPlaceSelected(event.place)
            is InclusiMapEvent.OnUnmappedPlaceSelected -> onUnmappedPlaceSelected(event.latLng)
            is InclusiMapEvent.OnAddNewMappedPlace -> onAddNewMappedPlace(event.newPlace)
            is InclusiMapEvent.SetLocationPermissionGranted -> setLocationPermissionGranted(event.isGranted)
            is InclusiMapEvent.OnUpdateMappedPlace -> onUpdateMappedPlace(event.placeUpdated)
            is InclusiMapEvent.OnDeleteMappedPlace -> onDeleteMappedPlace(event.placeId)
        }
    }

    private fun updateMapCameraPosition(latLng: LatLng, isMyLocationFounded: Boolean) {
        _state.update {
            it.copy(
                defaultLocationLatLng = latLng,
                isMyLocationFound = isMyLocationFounded
            )
        }
    }

    private fun onMapLoaded() {
        viewModelScope.launch(Dispatchers.IO) {
            accessibleLocalsRepository.getAccessibleLocals()?.let { mappedPlaces ->
            _state.update {
                    it.copy(
                        allMappedPlaces = mappedPlaces,
                        isMapLoaded = true,
                    )
                }
            }
        }
    }

    private fun onMappedPlaceSelected(place: AccessibleLocalMarker) {
        _state.update {
            it.copy(
                selectedMappedPlace = place
            )
        }
    }

    private fun onUnmappedPlaceSelected(latLng: LatLng) {
        _state.update {
            it.copy(
                selectedUnmappedPlaceLatLng = latLng
            )
        }
    }

    private fun onAddNewMappedPlace(newPlace: AccessibleLocalMarker) {
        _state.update {
            it.copy(
                allMappedPlaces = _state.value.allMappedPlaces + newPlace
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
            accessibleLocalsRepository.saveAccessibleLocal(newPlace)
        }
    }

    private fun setLocationPermissionGranted(isGranted: Boolean) {
        _state.value = _state.value.copy(
            isLocationPermissionGranted = isGranted
        )
    }

    private fun onUpdateMappedPlace(placeUpdated: AccessibleLocalMarker) {
        _state.update {
            it.copy(
                allMappedPlaces = _state.value.allMappedPlaces.map {
                    if (it.id == placeUpdated.id) {
                        placeUpdated
                    } else it
                }
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
            accessibleLocalsRepository.updateAccessibleLocal(placeUpdated)
        }
    }

    private fun onDeleteMappedPlace(placeID: String) {
        _state.update {
            it.copy(
                allMappedPlaces = _state.value.allMappedPlaces.filter { it.id != placeID }
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
            accessibleLocalsRepository.deleteAccessibleLocal(placeID)
        }
    }
}
