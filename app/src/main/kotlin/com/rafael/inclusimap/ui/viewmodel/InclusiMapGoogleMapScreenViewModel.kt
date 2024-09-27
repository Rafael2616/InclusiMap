package com.rafael.inclusimap.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.rafael.inclusimap.domain.AccessibleLocalMarker
import com.rafael.inclusimap.domain.InclusiMapEvent
import com.rafael.inclusimap.domain.InclusiMapState
import com.rafael.inclusimap.domain.repository.AccessibleLocalsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class InclusiMapGoogleMapScreenViewModel(
    private val accessibleLocalsRepository: AccessibleLocalsRepository
) : ViewModel() {
    private val _state = MutableStateFlow(InclusiMapState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            _state.update {
                it.copy(
                    allMappedPlaces = accessibleLocalsRepository.getAccessibleLocals()
                )
            }
        }
    }
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
        }
    }

    private fun updateMapCameraPosition(latLng: LatLng, isMyLocationFounded: Boolean) {
        _state.value = _state.value.copy(
            defaultLocationLatLng = latLng,
            isMyLocationFound = isMyLocationFounded
        )
    }

    private fun onMapLoaded() {
        _state.value = _state.value.copy(
            isMapLoaded = true
        )
    }

    private fun onMappedPlaceSelected(place: AccessibleLocalMarker) {
        _state.value = _state.value.copy(
            selectedMappedPlace = place
        )
    }

    private fun onUnmappedPlaceSelected(latLng: LatLng) {
        _state.value = _state.value.copy(
            selectedUnmappedPlaceLatLng = latLng
        )
    }

    private fun onAddNewMappedPlace(newPlace: AccessibleLocalMarker) {
        _state.value = _state.value.copy(
            allMappedPlaces = _state.value.allMappedPlaces + newPlace
        )
    }

    private fun setLocationPermissionGranted(isGranted: Boolean) {
        _state.value = _state.value.copy(
            isLocationPermissionGranted = isGranted
        )
    }

    private fun onUpdateMappedPlace(placeUpdated: AccessibleLocalMarker) {
        _state.value = _state.value.copy(
            allMappedPlaces = _state.value.allMappedPlaces.map {
                if (it.id == placeUpdated.id) {
                    placeUpdated
                } else it
            }
        )
    }
}