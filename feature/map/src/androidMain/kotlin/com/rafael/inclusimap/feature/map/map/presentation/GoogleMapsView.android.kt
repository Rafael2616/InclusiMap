package com.rafael.inclusimap.feature.map.map.presentation

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.ComposeMapColorScheme
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.rafael.inclusimap.core.util.map.model.Location
import com.rafael.inclusimap.core.util.map.model.toCategoryName
import com.rafael.inclusimap.feature.map.map.domain.model.InclusiMapEvent
import com.rafael.inclusimap.feature.map.map.domain.model.InclusiMapState
import com.rafael.inclusimap.feature.map.map.domain.model.RevealKeys
import com.rafael.inclusimap.feature.map.map.domain.model.TILT_RANGE
import com.rafael.inclusimap.feature.map.map.domain.model.inNorthRange
import com.rafael.inclusimap.feature.map.map.domain.model.toHUE
import com.rafael.inclusimap.feature.map.map.presentation.components.FindNorthWidget
import com.rafael.libs.maps.interop.model.MapType
import com.rafael.libs.maps.interop.model.MapsLatLng
import com.rafael.libs.maps.interop.model.toCameraPosition
import com.rafael.libs.maps.interop.model.toLatLng
import com.rafael.libs.maps.interop.model.toMapType
import com.rafael.libs.maps.interop.model.toMapsCameraPosition
import com.svenjacobs.reveal.RevealState
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun BoxScope.GoogleMapsView(
    state: InclusiMapState,
    onEvent: (InclusiMapEvent) -> Unit,
    addPlaceBottomSheetState: SheetState,
    addPlaceBottomSheetScope: CoroutineScope,
    showAppIntro: Boolean,
    onUpdateSearchHistory: (String) -> Unit,
    isInternetAvailable: Boolean,
    isFollowingSystemOn: Boolean,
    isDarkThemeOn: Boolean,
    selectedMapType: MapType,
    isPresentationMode: Boolean,
    openPlaceDetailsBottomSheet: (Boolean) -> Unit,
    requestLocationPermission: suspend () -> Unit,
    revealState: RevealState,
    snackbarHostState: SnackbarHostState,
    onSetPresentationMode: (Boolean) -> Unit,
    location: Location?,
    isFullScreenMode: Boolean,
    modifier: Modifier,
) {
    val cameraPositionState = rememberCameraPositionState()
    val context = LocalContext.current
    val latestOnEvent by rememberUpdatedState(onEvent)
    val latestRequestLocationPermission by rememberUpdatedState(requestLocationPermission)
    val latestOnSetPresentationMode by rememberUpdatedState(onSetPresentationMode)
    val latestOpenPlaceDetailsBottomSheet by rememberUpdatedState(openPlaceDetailsBottomSheet)
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        latestOnEvent(InclusiMapEvent.SetLocationPermissionGranted(isGranted))
    }
    var isNorth by remember(state.isMapLoaded) {
        mutableStateOf(
            (state.currentLocation?.bearing?.inNorthRange() == true) && state.currentLocation.tilt in TILT_RANGE,
        )
    }
    var isFindNorthBtnClicked by remember { mutableStateOf(false) }
    val showMarkers by remember(
        !cameraPositionState.isMoving,
        state.allMappedPlaces,
        state.isMapLoaded,
    ) { mutableStateOf(cameraPositionState.position.zoom >= 13f) }
    val onPlaceTravelScope = rememberCoroutineScope()
    var firstTimeAnimation by remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(Unit) {
        cameraPositionState.position = state.currentLocation?.toCameraPosition() ?: CameraPosition(
            state.defaultLocationLatLng.toLatLng(),
            15f,
            0f,
            0f,
        )
    }

    GoogleMap(
        modifier = Modifier
            .fillMaxSize(),
        properties = remember(
            selectedMapType,
            state.isLocationPermissionGranted,
            state.isMyLocationFound,
        ) {
            MapProperties(
                isBuildingEnabled = true,
                mapType = selectedMapType.toMapType(),
                isMyLocationEnabled = state.isLocationPermissionGranted && state.isMyLocationFound,
            )
        },
        uiSettings = remember {
            MapUiSettings(
                zoomGesturesEnabled = true,
                compassEnabled = false,
                rotationGesturesEnabled = true,
                zoomControlsEnabled = false,
                mapToolbarEnabled = false,
            )
        },
        cameraPositionState = cameraPositionState,
        onMapClick = {
            println("latitude ${it.latitude}" + "," + it.longitude)
        },
        mapColorScheme = when {
            isFollowingSystemOn && isSystemInDarkTheme() -> ComposeMapColorScheme.DARK
            isFollowingSystemOn && !isSystemInDarkTheme() -> ComposeMapColorScheme.LIGHT
            isDarkThemeOn -> ComposeMapColorScheme.DARK
            else -> ComposeMapColorScheme.LIGHT
        },
        onMapLoaded = {
            latestOnEvent(InclusiMapEvent.OnMapLoad)
            if (!showAppIntro) {
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        },
        onMapLongClick = {
            latestOnEvent(
                InclusiMapEvent.OnUnmappedPlaceSelected(
                    MapsLatLng(
                        it.latitude,
                        it.longitude,
                    ),
                ),
            )
            if (isInternetAvailable) {
                addPlaceBottomSheetScope.launch {
                    addPlaceBottomSheetState.show()
                }
            } else {
                Toast.makeText(
                    context,
                    "Não é possivel adicionar novos locais sem internet, verifique sua conexão!",
                    Toast.LENGTH_SHORT,
                ).show()
            }
        },
    ) {
        if (state.isMapLoaded) {
            state.allMappedPlaces.forEach { place ->
                val accessibilityAverage by remember(place.comments) {
                    mutableFloatStateOf(
                        place.comments.map { it.accessibilityRate }.average().toFloat(),
                    )
                }
                if (isPresentationMode && state.allMappedPlaces
                    .find { it.id == "fd9aa418-bc04-46fe-8974-f0bb8c400969" }?.id != place.id) return@forEach
                Marker(
                    state = remember(place.position) {
                        MarkerState(
                            position = LatLng(
                                place.position.first,
                                place.position.second,
                            ),
                        )
                    },
                    title = place.title,
                    snippet = place.category?.toCategoryName(),
                    icon = remember(accessibilityAverage) {
                        BitmapDescriptorFactory.defaultMarker(
                            accessibilityAverage.toHUE(),
                        )
                    },
                    onClick = {
                        latestOnEvent(InclusiMapEvent.OnMappedPlaceSelected(place))
                        place.id?.let { id -> onUpdateSearchHistory(id) }
                        latestOpenPlaceDetailsBottomSheet(true)
                        false
                    },
                    visible = showMarkers,
                )
            }
        }
    }
    if (!isFullScreenMode && !isNorth && state.isMapLoaded) {
        FindNorthWidget(
            cameraPosition = cameraPositionState.position.toMapsCameraPosition(),
            isDarkThemeOn = isDarkThemeOn,
            onFind = {
                isFindNorthBtnClicked = true
                onPlaceTravelScope.launch {
                    async {
                        cameraPositionState.animate(
                            with(cameraPositionState.position) {
                                CameraUpdateFactory.newCameraPosition(
                                    CameraPosition(
                                        target,
                                        zoom,
                                        0f,
                                        0f,
                                    ),
                                )
                            },
                        )
                    }.await()
                    delay(800)
                    isNorth = true
                    isFindNorthBtnClicked = false
                }
            },
        )
    }

    LaunchedEffect(state.shouldAnimateMap, firstTimeAnimation) {
        if (firstTimeAnimation == true) {
            async {
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngZoom(
                        state.defaultLocationLatLng.toLatLng(),
                        15f,
                    ),
                    3200,
                )
            }.await()
            latestRequestLocationPermission()
            latestOnSetPresentationMode(true)
            delay(2.seconds)
            revealState.reveal(RevealKeys.PLACE_DETAILS_TIP)
            firstTimeAnimation = false
        }
    }

    LaunchedEffect(
        state.shouldAnimateMap,
        state.isStateRestored,
        firstTimeAnimation,
        state.currentLocation,
    ) {
        if (state.shouldAnimateMap && firstTimeAnimation == false && state.isStateRestored) {
            cameraPositionState.position = CameraPosition(
                state.currentLocation?.target?.toLatLng() ?: state.defaultLocationLatLng.toLatLng(),
                state.currentLocation?.bearing ?: 0f,
                state.currentLocation?.tilt ?: 0f,
                state.currentLocation?.zoom ?: 15f,
            )
            if (state.currentLocation != null) {
                latestOnEvent(InclusiMapEvent.ShouldAnimateMap(false))
            }
        }
    }

    LaunchedEffect(location) {
        if (location != null && state.isContributionsScreen) {
            latestOnEvent(InclusiMapEvent.SetIsContributionsScreen(false))
            latestOnEvent(
                InclusiMapEvent.SetCurrentPlaceById(
                    location.placeId,
                ),
            )
            delay(300)
            onPlaceTravelScope.launch {
                async {
                    cameraPositionState.animate(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(location.lat, location.lng),
                            18f,
                        ),
                        2800,
                    )
                }.await()
                if (location.placeId in state.allMappedPlaces.map { it.id }) {
                    latestOpenPlaceDetailsBottomSheet(true)
                } else {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    snackbarHostState.showSnackbar(
                        "Não foi possivel encontrar o local selecionado!",
                    )
                    return@launch
                }
            }
        }
    }

    LaunchedEffect(state.shouldTravel) {
        if (state.shouldTravel) {
            onPlaceTravelScope.launch {
                val position = state.selectedMappedPlace?.position ?: return@launch
                async {
                    cameraPositionState.animate(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(position.first, position.second),
                            18f,
                        ),
                    )
                }.await()
                latestOnEvent(InclusiMapEvent.SetShouldTravel(false))
                latestOpenPlaceDetailsBottomSheet(true)
            }
        }
    }

    val defaultPos = rememberCameraPositionState().position
    DisposableEffect(cameraPositionState.isMoving) {
        if (!cameraPositionState.isMoving && cameraPositionState.position != defaultPos) {
            latestOnEvent(InclusiMapEvent.UpdateMapState(cameraPositionState.position.toMapsCameraPosition()))
        }
        onDispose { }
    }

    LaunchedEffect(!cameraPositionState.isMoving) {
        if (!cameraPositionState.isMoving && !isFindNorthBtnClicked && cameraPositionState.position.tilt in TILT_RANGE && cameraPositionState.position.bearing.inNorthRange()) {
            delay(800)
            isNorth = true
        }
    }

    DisposableEffect(cameraPositionState.position.bearing, cameraPositionState.position.tilt) {
        if (!cameraPositionState.position.bearing.inNorthRange() || cameraPositionState.position.tilt !in TILT_RANGE) {
            isNorth = false
        }
        onDispose { }
    }
}
