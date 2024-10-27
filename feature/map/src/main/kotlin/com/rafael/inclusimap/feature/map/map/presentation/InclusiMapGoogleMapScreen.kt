package com.rafael.inclusimap.feature.map.map.presentation

import android.Manifest
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.ComposeMapColorScheme
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.rafael.inclusimap.core.domain.model.toCategoryName
import com.rafael.inclusimap.core.domain.model.util.toHUE
import com.rafael.inclusimap.core.domain.network.InternetConnectionState
import com.rafael.inclusimap.core.navigation.Location
import com.rafael.inclusimap.core.settings.domain.model.SettingsState
import com.rafael.inclusimap.feature.intro.domain.model.AppIntroState
import com.rafael.inclusimap.feature.intro.presentation.dialogs.AppIntroDialog
import com.rafael.inclusimap.feature.map.map.domain.InclusiMapEvent
import com.rafael.inclusimap.feature.map.map.domain.InclusiMapState
import com.rafael.inclusimap.feature.map.map.presentation.dialog.PlacesNotLoadedDialog
import com.rafael.inclusimap.feature.map.map.presentation.dialog.PlacesNotUpdatedDialog
import com.rafael.inclusimap.feature.map.map.presentation.dialog.ServerUnavailableDialog
import com.rafael.inclusimap.feature.map.placedetails.domain.model.PlaceDetailsEvent
import com.rafael.inclusimap.feature.map.placedetails.domain.model.PlaceDetailsState
import com.rafael.inclusimap.feature.map.placedetails.presentation.PlaceDetailsBottomSheet
import com.rafael.inclusimap.feature.report.domain.model.Report
import com.rafael.inclusimap.feature.report.domain.model.ReportState
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Suppress("ktlint:compose:modifier-not-used-at-root")
@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun InclusiMapGoogleMapScreen(
    state: InclusiMapState,
    onEvent: (InclusiMapEvent) -> Unit,
    placeDetailsState: PlaceDetailsState,
    onPlaceDetailsEvent: (PlaceDetailsEvent) -> Unit,
    appIntroState: AppIntroState,
    onDismissAppIntro: (Boolean) -> Unit,
    onUpdateSearchHistory: (String) -> Unit,
    settingsState: SettingsState,
    userName: String,
    userEmail: String,
    onReport: (Report) -> Unit,
    reportState: ReportState,
    location: Location?,
    allowedShowUserProfilePicture: suspend (String) -> Boolean,
    downloadUserProfilePicture: suspend (String) -> ImageBitmap?,
    cameraPositionState: CameraPositionState,
    modifier: Modifier = Modifier,
) {
    val onPlaceTravelScope = rememberCoroutineScope()
    val addPlaceBottomSheetState = rememberModalBottomSheetState()
    val addPlaceBottomSheetScope = rememberCoroutineScope()
    val locationPermission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    val showMarkers by remember(
        !cameraPositionState.isMoving,
        state.allMappedPlaces,
    ) { mutableStateOf(cameraPositionState.position.zoom >= 15f) }
    val latestOnEvent by rememberUpdatedState(onEvent)
    val latestOnPlaceDetailsEvent by rememberUpdatedState(onPlaceDetailsEvent)
    val latestOnDismissAppIntro by rememberUpdatedState(onDismissAppIntro)
    val context = LocalContext.current
    val internetState = remember { InternetConnectionState(context) }
    val isInternetAvailable by internetState.state.collectAsStateWithLifecycle()
    var firstTimeAnimation by remember { mutableStateOf<Boolean?>(null) }
    var openPlaceDetailsBottomSheet by rememberSaveable { mutableStateOf(false) }

    GoogleMap(
        modifier = modifier
            .fillMaxSize(),
        properties = remember(
            settingsState.mapType,
            state.isLocationPermissionGranted,
            state.isMyLocationFound,
        ) {
            MapProperties(
                isBuildingEnabled = true,
                mapType = settingsState.mapType,
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
            settingsState.isFollowingSystemOn && isSystemInDarkTheme() -> ComposeMapColorScheme.DARK
            settingsState.isFollowingSystemOn && !isSystemInDarkTheme() -> ComposeMapColorScheme.LIGHT
            settingsState.isDarkThemeOn -> ComposeMapColorScheme.DARK
            else -> ComposeMapColorScheme.LIGHT
        },
        onMapLoaded = {
            latestOnEvent(InclusiMapEvent.OnMapLoad)
            if (!appIntroState.showAppIntro) {
                locationPermission.launchPermissionRequest()
            }
        },
        onMapLongClick = {
            latestOnEvent(InclusiMapEvent.OnUnmappedPlaceSelected(it))
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
                    mutableStateOf(
                        place.comments.map { it.accessibilityRate }.average().toFloat(),
                    )
                }
                Marker(
                    state = MarkerState(
                        position = LatLng(
                            place.position.first,
                            place.position.second,
                        ),
                    ),
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
                        openPlaceDetailsBottomSheet = true
                        false
                    },
                    visible = showMarkers,
                )
            }
        }
    }

    AnimatedVisibility(appIntroState.showAppIntro) {
        AppIntroDialog(
            userName = userName,
            onDismiss = {
                latestOnEvent(InclusiMapEvent.ResetState)
                latestOnDismissAppIntro(false)
                firstTimeAnimation = true
            },
        )
    }

    AnimatedVisibility(openPlaceDetailsBottomSheet) {
        PlaceDetailsBottomSheet(
            state = placeDetailsState,
            inclusiMapState = state,
            onEvent = latestOnPlaceDetailsEvent,
            userName = userName,
            userEmail = userEmail,
            onDismiss = {
                latestOnPlaceDetailsEvent(PlaceDetailsEvent.OnDestroyPlaceDetails)
                openPlaceDetailsBottomSheet = false
            },
            onUpdateMappedPlace = { placeUpdated ->
                latestOnEvent(InclusiMapEvent.OnUpdateMappedPlace(placeUpdated))
            },
            onReport = onReport,
            reportState = reportState,
            downloadUserProfilePicture = downloadUserProfilePicture,
            allowedShowUserProfilePicture = allowedShowUserProfilePicture,
            userPicture = settingsState.profilePicture,
        )
    }

    AnimatedVisibility(addPlaceBottomSheetState.isVisible || placeDetailsState.isEditingPlace) {
        AddEditPlaceBottomSheet(
            latlng = state.selectedUnmappedPlaceLatLng ?: LatLng(0.0, 0.0),
            placeDetailsState = placeDetailsState,
            userEmail = userEmail,
            bottomSheetScaffoldState = addPlaceBottomSheetState,
            isInternetAvailable = isInternetAvailable,
            onDismiss = {
                addPlaceBottomSheetScope.launch {
                    addPlaceBottomSheetState.hide()
                }
                latestOnPlaceDetailsEvent(PlaceDetailsEvent.SetIsEditingPlace(false))
            },
            onAddNewPlace = { newPlace ->
                latestOnEvent(InclusiMapEvent.OnAddNewMappedPlace(newPlace))
            },
            mapState = state,
            onEditNewPlace = {
                latestOnEvent(InclusiMapEvent.OnUpdateMappedPlace(it))
                latestOnPlaceDetailsEvent(PlaceDetailsEvent.SetCurrentPlace(it))
            },
            onDeletePlace = {
                latestOnEvent(InclusiMapEvent.OnDeleteMappedPlace(it))
                openPlaceDetailsBottomSheet = false
            },
        )
    }

    AnimatedVisibility(state.failedToLoadPlaces) {
        PlacesNotLoadedDialog(
            onRetry = {
                latestOnEvent(InclusiMapEvent.OnFailToLoadPlaces(false))
            },
        )
    }
    AnimatedVisibility(state.failedToConnectToServer) {
        ServerUnavailableDialog(
            onRetry = {
                latestOnEvent(InclusiMapEvent.OnFailToConnectToServer(false))
            },
        )
    }

    AnimatedVisibility(state.failedToGetNewPlaces && !state.useAppWithoutInternet) {
        PlacesNotUpdatedDialog(
            onRetry = {
                latestOnEvent(InclusiMapEvent.OnFailToLoadPlaces(false))
            },
            onDismiss = {
                latestOnEvent(InclusiMapEvent.UseAppWithoutInternet)
            },
        )
    }

    LaunchedEffect(!isInternetAvailable) {
        addPlaceBottomSheetScope.launch {
            addPlaceBottomSheetState.hide()
        }
        latestOnPlaceDetailsEvent(PlaceDetailsEvent.SetIsEditingPlace(false))
    }

    DisposableEffect(state.allMappedPlaces.isEmpty() || state.useAppWithoutInternet && isInternetAvailable) {
        latestOnEvent(InclusiMapEvent.OnLoadPlaces)
        onDispose { }
    }

    DisposableEffect(locationPermission.status) {
        latestOnEvent(InclusiMapEvent.SetLocationPermissionGranted(locationPermission.status == PermissionStatus.Granted))
        onDispose { }
    }

    LaunchedEffect(state.shouldAnimateMap, firstTimeAnimation) {
        if (firstTimeAnimation == true) {
            async {
                cameraPositionState.animate(
                    update = CameraUpdateFactory.newLatLngZoom(
                        state.defaultLocationLatLng,
                        15f,
                    ),
                    durationMs = 3500,
                )
            }.await()
            locationPermission.launchPermissionRequest()
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
                state.currentLocation?.target ?: state.defaultLocationLatLng,
                state.currentLocation?.zoom ?: 15f,
                state.currentLocation?.tilt ?: 0f,
                state.currentLocation?.bearing ?: 0f,
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
                    openPlaceDetailsBottomSheet = true
                } else {
                    Toast.makeText(
                        context,
                        "Não foi possivel encontrar o local selecionado!",
                        Toast.LENGTH_SHORT,
                    ).show()
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
                            LatLng(
                                position.first,
                                position.second,
                            ),
                            18f,
                        ),
                        2500,
                    )
                }.await()
                latestOnEvent(InclusiMapEvent.SetShouldTravel(false))
                openPlaceDetailsBottomSheet = true
            }
        }
    }

    val defaultPos = rememberCameraPositionState().position
    DisposableEffect(!cameraPositionState.isMoving) {
        if (!cameraPositionState.isMoving && cameraPositionState.position != defaultPos) {
            latestOnEvent(InclusiMapEvent.UpdateMapState(cameraPositionState.position))
        }
        onDispose { }
    }

    DisposableEffect(Unit) {
        latestOnEvent(InclusiMapEvent.GetCurrentState)
        if (!appIntroState.showAppIntro) firstTimeAnimation = false
        onDispose { }
    }
}
