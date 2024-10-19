package com.rafael.inclusimap.feature.map.presentation

import android.Manifest
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
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
import com.rafael.inclusimap.core.domain.model.toCategoryName
import com.rafael.inclusimap.core.domain.model.util.toHUE
import com.rafael.inclusimap.core.domain.network.InternetConnectionState
import com.rafael.inclusimap.core.navigation.Location
import com.rafael.inclusimap.core.settings.domain.model.SettingsState
import com.rafael.inclusimap.feature.intro.domain.model.AppIntroState
import com.rafael.inclusimap.feature.intro.presentation.dialogs.AppIntroDialog
import com.rafael.inclusimap.feature.map.domain.InclusiMapEvent
import com.rafael.inclusimap.feature.map.domain.InclusiMapState
import com.rafael.inclusimap.feature.map.domain.PlaceDetailsEvent
import com.rafael.inclusimap.feature.map.domain.PlaceDetailsState
import com.rafael.inclusimap.feature.map.domain.Report
import com.rafael.inclusimap.feature.map.domain.ReportState
import com.rafael.inclusimap.feature.map.presentation.dialog.PlacesNotLoadedDialog
import com.rafael.inclusimap.feature.map.presentation.dialog.PlacesNotUpdatedDialog
import com.rafael.inclusimap.feature.map.presentation.dialog.ServerUnavailableDialog
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
    settingsState: SettingsState,
    userName: String,
    userEmail: String,
    onReport: (Report) -> Unit,
    reportState: ReportState,
    location: Location?,
    allowedShowUserProfilePicture: suspend (String) -> Boolean,
    downloadUserProfilePicture: suspend (String) -> ImageBitmap?,
    cameraPositionState: CameraPositionState,
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    val onPlaceTravelScope = rememberCoroutineScope()
    val bottomSheetScaffoldState = rememberModalBottomSheetState()
    val bottomSheetScope = rememberCoroutineScope()
    val addPlaceBottomSheetScaffoldState = rememberModalBottomSheetState()
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
    var firstTimeAnimation by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        latestOnEvent(InclusiMapEvent.GetCurrentState)
    }

    LaunchedEffect(state.shouldAnimateMap, firstTimeAnimation, state.currentLocation) {
        if (firstTimeAnimation) {
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
        if (state.shouldAnimateMap) {
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

    LaunchedEffect(!cameraPositionState.isMoving) {
        if (!cameraPositionState.isMoving) {
            latestOnEvent(InclusiMapEvent.UpdateMapState(cameraPositionState.position))
        }
    }
    GoogleMap(
        modifier = modifier
            .fillMaxSize(),
        properties = MapProperties(
            isBuildingEnabled = true,
            mapType = settingsState.mapType,
            isMyLocationEnabled = state.isLocationPermissionGranted && state.isMyLocationFound,
        ),
        uiSettings = MapUiSettings(
            zoomGesturesEnabled = true,
            compassEnabled = false,
            rotationGesturesEnabled = true,
            zoomControlsEnabled = false,
            mapToolbarEnabled = false,
        ),
        cameraPositionState = cameraPositionState,
        onMapClick = {
            println("latitude ${it.latitude}" + "," + it.longitude)
        },
        mapColorScheme = if (settingsState.isDarkThemeOn) ComposeMapColorScheme.DARK else ComposeMapColorScheme.LIGHT,
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
                    addPlaceBottomSheetScaffoldState.show()
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
                    snippet = place.category!!.toCategoryName(),
                    icon = BitmapDescriptorFactory.defaultMarker(
                        accessibilityAverage.toHUE(),
                    ),
                    onClick = {
                        latestOnEvent(InclusiMapEvent.OnMappedPlaceSelected(place))
                        bottomSheetScope.launch {
                            bottomSheetScaffoldState.show()
                        }
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

    AnimatedVisibility(bottomSheetScaffoldState.isVisible) {
        PlaceDetailsBottomSheet(
            state = placeDetailsState,
            inclusiMapState = state,
            onEvent = latestOnPlaceDetailsEvent,
            userName = userName,
            userEmail = userEmail,
            bottomSheetScaffoldState = bottomSheetScaffoldState,
            onDismiss = {
                latestOnPlaceDetailsEvent(PlaceDetailsEvent.OnDestroyPlaceDetails)
                bottomSheetScope.launch {
                    bottomSheetScaffoldState.hide()
                }
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

    AnimatedVisibility(addPlaceBottomSheetScaffoldState.isVisible || placeDetailsState.isEditingPlace) {
        AddEditPlaceBottomSheet(
            latlng = state.selectedUnmappedPlaceLatLng ?: LatLng(0.0, 0.0),
            placeDetailsState = placeDetailsState,
            userEmail = userEmail,
            bottomSheetScaffoldState = addPlaceBottomSheetScaffoldState,
            onDismiss = {
                addPlaceBottomSheetScope.launch {
                    addPlaceBottomSheetScaffoldState.hide()
                }
                latestOnPlaceDetailsEvent(PlaceDetailsEvent.SetIsEditingPlace(false))
            },
            onAddNewPlace = { newPlace ->
                latestOnEvent(InclusiMapEvent.OnAddNewMappedPlace(newPlace))
            },
            isEditing = placeDetailsState.isEditingPlace,
            onEditNewPlace = {
                latestOnEvent(InclusiMapEvent.OnUpdateMappedPlace(it))
                latestOnPlaceDetailsEvent(PlaceDetailsEvent.SetCurrentPlace(it))
            },
            onDeletePlace = {
                latestOnEvent(InclusiMapEvent.OnDeleteMappedPlace(it))
                latestOnPlaceDetailsEvent(PlaceDetailsEvent.SetIsEditingPlace(false))
                bottomSheetScope.launch {
                    bottomSheetScaffoldState.hide()
                }
                addPlaceBottomSheetScope.launch {
                    addPlaceBottomSheetScaffoldState.hide()
                }
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
            addPlaceBottomSheetScaffoldState.hide()
        }
    }

    DisposableEffect(state.allMappedPlaces.isEmpty() || state.useAppWithoutInternet && isInternetAvailable) {
        latestOnEvent(InclusiMapEvent.OnLoadPlaces)
        onDispose { }
    }

    DisposableEffect(locationPermission.status) {
        latestOnEvent(InclusiMapEvent.SetLocationPermissionGranted(locationPermission.status == PermissionStatus.Granted))
        onDispose { }
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
                        3000,
                    )
                }.await()
                bottomSheetScaffoldState.show()
            }
        }
    }
}
