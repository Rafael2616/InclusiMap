package com.rafael.inclusimap.ui

import android.Manifest
import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.ComposeMapColorScheme
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.rafael.inclusimap.domain.AccessibleLocalMarker
import com.rafael.inclusimap.domain.repository.mappedPlaces
import kotlinx.coroutines.launch

@SuppressLint("MissingPermission")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun InclusiMapGoogleMapScreen(
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val activity = LocalContext.current
    val fusedLocationClient by lazy { LocationServices.getFusedLocationProviderClient(activity) }
    var isMyLocationFound by remember { mutableStateOf(false) }
    var latlang = LatLng(-2.98, -47.35)
    var isMapLoaded by remember { mutableStateOf(false) }
    val cameraPositionState by remember {
        mutableStateOf(
            CameraPositionState(
                CameraPosition(
                    latlang,
                    15f,
                    0f,
                    0f
                )
            )
        )
    }
    val locationPermissionState = rememberPermissionState(
        permission = Manifest.permission.ACCESS_FINE_LOCATION
    )
    val locationPermissionGranted by remember(locationPermissionState.status) {
        mutableStateOf(locationPermissionState.status == PermissionStatus.Granted)
    }
    var showBottomSheet by remember { mutableStateOf(false) }
    var currentPlaceDetais by remember { mutableStateOf<AccessibleLocalMarker?>(null) }

    LaunchedEffect(locationPermissionGranted) {
        if (locationPermissionGranted) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    latlang = LatLng(it.latitude, it.longitude)
                    isMyLocationFound = true
                }
            }

        }
    }

    LaunchedEffect(isMapLoaded, locationPermissionGranted) {
        if (locationPermissionGranted && isMapLoaded) {
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(latlang, 15f),
                durationMs = 4000
            )
        }
    }

    GoogleMap(
        modifier = modifier.fillMaxSize(),
        properties = MapProperties(
            isBuildingEnabled = true,
            mapType = MapType.NORMAL,
            isMyLocationEnabled = locationPermissionGranted && isMyLocationFound,
        ),
        uiSettings = MapUiSettings(
            zoomControlsEnabled = true,
            zoomGesturesEnabled = true,
            compassEnabled = true,
            myLocationButtonEnabled = true,
            mapToolbarEnabled = true,
            rotationGesturesEnabled = true
        ),
        cameraPositionState = cameraPositionState,
        onMapClick = {
            println("latitude ${it.latitude}" + "," + it.longitude)
        },
        mapColorScheme = ComposeMapColorScheme.FOLLOW_SYSTEM,
        onMapLoaded = {
            isMapLoaded = true
            scope.launch {
                if (isMyLocationFound) {
                    cameraPositionState.animate(
                        update = CameraUpdateFactory.newLatLngZoom(latlang, 55f),
                        durationMs = 4000
                    )
                }
            }
        }
    ) {
        mappedPlaces.forEach { place ->
            Marker(
                state = place.markerState,
                title = place.title,
                snippet = place.description,
                icon = BitmapDescriptorFactory.defaultMarker(
                    if (place.isAccessible) BitmapDescriptorFactory.HUE_GREEN else BitmapDescriptorFactory.HUE_RED
                ),
                onClick = {
                    showBottomSheet = true
                    currentPlaceDetais = place
                    false
                }
            )
        }
    }
    LaunchedEffect(Unit) {
        locationPermissionState.launchPermissionRequest()
    }
    AnimatedVisibility(showBottomSheet) {
        PlaceDetainsBottomSheet(
            localMarker = currentPlaceDetais!!,
            onDismiss = {
                showBottomSheet = false
            }
        )
    }
}


