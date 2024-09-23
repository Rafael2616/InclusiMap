package com.rafael.inclusimap.ui

import android.Manifest
import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.ComposeMapColorScheme
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.rafael.inclusimap.R
import com.rafael.inclusimap.data.GoogleDriveService
import com.rafael.inclusimap.data.repository.mappedPlaces
import com.rafael.inclusimap.data.toHUE
import com.rafael.inclusimap.domain.AccessibleLocalMarker
import kotlinx.coroutines.launch

@SuppressLint("MissingPermission")
@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun InclusiMapGoogleMapScreen(
    driveService: GoogleDriveService,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val activity = LocalContext.current
    val fusedLocationClient by lazy { LocationServices.getFusedLocationProviderClient(activity) }
    var isMyLocationFound by remember { mutableStateOf(false) }
    var latlang = LatLng(-2.98, -47.35)
    var isMapLoaded by remember { mutableStateOf(false) }
    val cameraPositionState = rememberCameraPositionState()
    val bottomSheetScaffoldState = rememberModalBottomSheetState()
    val addPlaceBottomSheetScaffoldState = rememberModalBottomSheetState()
    val bottomSheetScope = rememberCoroutineScope()
    val addPlaceBottomSheetScope = rememberCoroutineScope()

    val locationPermissionState = rememberPermissionState(
        permission = Manifest.permission.ACCESS_FINE_LOCATION
    )
    val locationPermissionGranted by remember(locationPermissionState.status) {
        mutableStateOf(locationPermissionState.status == PermissionStatus.Granted)
    }
    var currentPlaceDetais by remember { mutableStateOf<AccessibleLocalMarker?>(null) }
    var addPlacePos by remember { mutableStateOf<LatLng?>(null) }
    var currentMappedPlaces by remember { mutableStateOf(mappedPlaces) }

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

    var expanded by remember { mutableStateOf(false) }
    var searchState by remember { mutableStateOf("") }

    SearchBar(
        modifier = Modifier
            .navigationBarsPadding()
            .statusBarsPadding()
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .semantics { traversalIndex = -1f },
        inputField = {
            SearchBarDefaults.InputField(
                query = searchState,
                onQueryChange = {
                    searchState = it
                },
                onSearch = {
                    expanded = false
                },
                expanded = expanded,
                onExpandedChange = { },
                placeholder = { Text("Pesquise algo aqui") },
                leadingIcon = {
                    Image(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape),
                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = null,
                    )
                },
                trailingIcon = {
                    if (searchState.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                searchState = ""
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = null,
                            )
                        }
                    }
                },
            )
        },
        expanded = expanded,
        onExpandedChange = { expanded = it },
        colors = SearchBarDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
            dividerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
        ),
    ) { }
    GoogleMap(
        modifier = modifier
            .fillMaxSize(),
        properties = MapProperties(
            isBuildingEnabled = true,
            mapType = MapType.NORMAL,
            isMyLocationEnabled = locationPermissionGranted && isMyLocationFound,
        ),
        uiSettings = MapUiSettings(
            zoomControlsEnabled = true,
            zoomGesturesEnabled = true,
            compassEnabled = true,
            rotationGesturesEnabled = true
        ),
        cameraPositionState = cameraPositionState,
        onMapClick = {
            println("latitude ${it.latitude}" + "," + it.longitude)
        },
        mapColorScheme = if (isSystemInDarkTheme()) ComposeMapColorScheme.DARK else ComposeMapColorScheme.LIGHT,
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
        },
        onMapLongClick = {
            addPlacePos = it
            addPlaceBottomSheetScope.launch {
                addPlaceBottomSheetScaffoldState.show()
            }
        }
    ) {
        if (isMapLoaded) {
            currentMappedPlaces.forEach { place ->
                val accessibilityAverage =
                    place.comments?.map { it.accessibilityRate }?.average()?.toFloat()
                Marker(
                    state = place.markerState,
                    title = place.title,
                    snippet = place.description,
                    icon = BitmapDescriptorFactory.defaultMarker(
                        accessibilityAverage?.toHUE() ?: BitmapDescriptorFactory.HUE_ORANGE
                    ),
                    onClick = {
                        bottomSheetScope.launch {
                            bottomSheetScaffoldState.show()
                        }
                        currentPlaceDetais = place
                        false
                    },
                )
            }
        }
    }
    LaunchedEffect(Unit) {
        locationPermissionState.launchPermissionRequest()
    }
    AnimatedVisibility(bottomSheetScaffoldState.isVisible) {
        PlaceDetailsBottomSheet(
            driveService = driveService,
            localMarker = currentPlaceDetais!!,
            bottomSheetScaffoldState = bottomSheetScaffoldState,
            onDismiss = {
                bottomSheetScope.launch {
                    bottomSheetScaffoldState.hide()
                }
            }
        )
    }
    AnimatedVisibility(addPlaceBottomSheetScaffoldState.isVisible) {
        AddPlaceBottomSheet(
            latLng = addPlacePos!!,
            bottomSheetScaffoldState = addPlaceBottomSheetScaffoldState,
            onDismiss = { newLocal ->
                newLocal?.let {
                    currentMappedPlaces = currentMappedPlaces + it
                }
                addPlaceBottomSheetScope.launch {
                    addPlaceBottomSheetScaffoldState.hide()
                }
            }
        )
    }
}
