package com.rafael.inclusimap.ui

import android.Manifest
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.ComposeMapColorScheme
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.rafael.inclusimap.R
import com.rafael.inclusimap.data.toHUE
import com.rafael.inclusimap.domain.AppIntroState
import com.rafael.inclusimap.domain.InclusiMapEvent
import com.rafael.inclusimap.domain.InclusiMapState
import com.rafael.inclusimap.domain.LoginState
import com.rafael.inclusimap.domain.PlaceDetailsEvent
import com.rafael.inclusimap.domain.PlaceDetailsState
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun InclusiMapGoogleMapScreen(
    state: InclusiMapState,
    onEvent: (InclusiMapEvent) -> Unit,
    placeDetailsState: PlaceDetailsState,
    onPlaceDetailsEvent: (PlaceDetailsEvent) -> Unit,
    appIntroState: AppIntroState,
    onDismissAppIntro: (Boolean) -> Unit,
    loginState: LoginState,
    fusedLocationClient: FusedLocationProviderClient,
    modifier: Modifier = Modifier,
) {
    var animateMap by remember { mutableStateOf(false) }
    val cameraPositionState = rememberCameraPositionState()
    val bottomSheetScaffoldState = rememberModalBottomSheetState()
    val bottomSheetScope = rememberCoroutineScope()
    val addPlaceBottomSheetScaffoldState = rememberModalBottomSheetState()
    val addPlaceBottomSheetScope = rememberCoroutineScope()
    val locationPermission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    val showMarkers by remember(cameraPositionState.isMoving) { mutableStateOf(cameraPositionState.position.zoom >= 15f) }

    LaunchedEffect(animateMap && !appIntroState.showAppIntro) {
        if (animateMap) {
            async {
                cameraPositionState.animate(
                    update = CameraUpdateFactory.newLatLngZoom(
                        state.defaultLocationLatLng,
                        15f
                    ),
                    durationMs = 3500
                )
            }.await()
            locationPermission.launchPermissionRequest()
        }
    }
    LaunchedEffect(state.isLocationPermissionGranted) {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                onEvent(
                    InclusiMapEvent.UpdateMapCameraPosition(
                        LatLng(
                            it.latitude,
                            it.longitude
                        ), true
                    ).also { pos ->
                        launch {
                            cameraPositionState.animate(
                                update = CameraUpdateFactory.newLatLngZoom(
                                    pos.latLng,
                                    25f
                                ),
                                durationMs = 3500
                            )
                        }
                    }
                )
            }
        }
    }

    LaunchedEffect(locationPermission.status) {
        onEvent(InclusiMapEvent.SetLocationPermissionGranted(locationPermission.status == PermissionStatus.Granted))
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
            isMyLocationEnabled = state.isLocationPermissionGranted && state.isMyLocationFound,
        ),
        uiSettings = MapUiSettings(
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
            onEvent(InclusiMapEvent.OnMapLoaded)
            if (!animateMap && !appIntroState.showAppIntro) {
                cameraPositionState.position = CameraPosition.fromLatLngZoom(
                    state.defaultLocationLatLng,
                    15f
                )
                locationPermission.launchPermissionRequest()
            }
        },
        onMapLongClick = {
            onEvent(InclusiMapEvent.OnUnmappedPlaceSelected(it))
            addPlaceBottomSheetScope.launch {
                addPlaceBottomSheetScaffoldState.show()
            }
        }
    ) {
        if (state.isMapLoaded) {
            state.allMappedPlaces.forEach { place ->
                val accessibilityAverage =
                    place.comments.map { it.accessibilityRate }.average().toFloat()
                Marker(
                    state = place.markerState,
                    title = place.title,
                    snippet = place.category,
                    icon = BitmapDescriptorFactory.defaultMarker(
                        accessibilityAverage.toHUE()
                    ),
                    onClick = {
                        onEvent(InclusiMapEvent.OnMappedPlaceSelected(place))
                        bottomSheetScope.launch {
                            bottomSheetScaffoldState.show()
                        }
                        false
                    },
                    visible = showMarkers
                )
            }
        }
    }

    AnimatedVisibility(appIntroState.showAppIntro) {
        AppIntroDialog(
            state = loginState,
            onDismiss = {
                onDismissAppIntro(false)
                animateMap = true
            }
        )
    }
    AnimatedVisibility(bottomSheetScaffoldState.isVisible) {
        PlaceDetailsBottomSheet(
            state = placeDetailsState,
            onEvent = onPlaceDetailsEvent,
            loginState = loginState,
            localMarker = state.selectedMappedPlace!!,
            bottomSheetScaffoldState = bottomSheetScaffoldState,
            onDismiss = {
                onPlaceDetailsEvent(PlaceDetailsEvent.OnDestroyPlaceDetails)
                bottomSheetScope.launch {
                    bottomSheetScaffoldState.hide()
                }
            },
            onUpdateMappedPlace = { placeUpdated ->
                onEvent(InclusiMapEvent.OnUpdateMappedPlace(placeUpdated))
            }
        )
    }
    AnimatedVisibility(addPlaceBottomSheetScaffoldState.isVisible) {
        AddPlaceBottomSheet(
            latLng = state.selectedUnmappedPlaceLatLng!!,
            loginState = loginState,
            bottomSheetScaffoldState = addPlaceBottomSheetScaffoldState,
            onDismiss = {
                addPlaceBottomSheetScope.launch {
                    addPlaceBottomSheetScaffoldState.hide()
                }
            },
            onAddNewPlace = { newPlace ->
                onEvent(InclusiMapEvent.OnAddNewMappedPlace(newPlace))
            },
        )
    }
}
