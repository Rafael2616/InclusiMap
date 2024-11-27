package com.rafael.inclusimap.feature.map.map.presentation

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.rafael.inclusimap.core.domain.network.InternetConnectionState
import com.rafael.inclusimap.core.navigation.types.Location
import com.rafael.inclusimap.core.settings.domain.model.SettingsState
import com.rafael.inclusimap.core.ui.components.OverlayText
import com.rafael.inclusimap.feature.intro.domain.model.AppIntroState
import com.rafael.inclusimap.feature.intro.presentation.dialogs.AppIntroDialog
import com.rafael.inclusimap.feature.map.map.domain.InclusiMapEvent
import com.rafael.inclusimap.feature.map.map.domain.InclusiMapState
import com.rafael.inclusimap.feature.map.map.domain.TILT_RANGE
import com.rafael.inclusimap.feature.map.map.domain.inNorthRange
import com.rafael.inclusimap.feature.map.map.domain.toHUE
import com.rafael.inclusimap.feature.map.map.presentation.dialog.PlacesNotLoadedDialog
import com.rafael.inclusimap.feature.map.map.presentation.dialog.PlacesNotUpdatedDialog
import com.rafael.inclusimap.feature.map.map.presentation.dialog.ServerConnectionErrorDialog
import com.rafael.inclusimap.feature.map.map.presentation.dialog.ServerUnavailableDialog
import com.rafael.inclusimap.feature.map.placedetails.domain.model.PlaceDetailsEvent
import com.rafael.inclusimap.feature.map.placedetails.domain.model.PlaceDetailsState
import com.rafael.inclusimap.feature.map.placedetails.presentation.PlaceDetailsBottomSheet
import com.rafael.inclusimap.feature.report.domain.model.Report
import com.rafael.inclusimap.feature.report.domain.model.ReportState
import com.svenjacobs.reveal.Reveal
import com.svenjacobs.reveal.RevealCanvasState
import com.svenjacobs.reveal.RevealOverlayArrangement
import com.svenjacobs.reveal.RevealOverlayScope
import com.svenjacobs.reveal.RevealShape
import com.svenjacobs.reveal.RevealState
import com.svenjacobs.reveal.rememberRevealState
import com.svenjacobs.reveal.revealable
import com.svenjacobs.reveal.shapes.balloon.Arrow
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InclusiMapGoogleMapScreen(
    state: InclusiMapState,
    onEvent: (InclusiMapEvent) -> Unit,
    placeDetailsState: PlaceDetailsState,
    onPlaceDetailsEvent: (PlaceDetailsEvent) -> Unit,
    appIntroState: AppIntroState,
    revealCanvasState: RevealCanvasState,
    onDismissAppIntro: (Boolean) -> Unit,
    onUpdateSearchHistory: (String) -> Unit,
    onTryReconnect: () -> Unit,
    settingsState: SettingsState,
    isServerAvailable: Boolean,
    isCheckingServerAvailability: Boolean,
    userName: String,
    userEmail: String,
    userProfilePicture: ImageBitmap?,
    isFullScreenMode: Boolean,
    onReport: (Report) -> Unit,
    reportState: ReportState,
    location: Location?,
    allowedShowUserProfilePicture: suspend (String) -> Boolean,
    downloadUserProfilePicture: suspend (String) -> ImageBitmap?,
    cameraPositionState: CameraPositionState,
    modifier: Modifier = Modifier,
) {
    val revealState = rememberRevealState()
    val scope = rememberCoroutineScope()
    val onPlaceTravelScope = rememberCoroutineScope()
    val addPlaceBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val addPlaceBottomSheetScope = rememberCoroutineScope()
    val showMarkers by remember(
        !cameraPositionState.isMoving,
        state.allMappedPlaces,
        state.isMapLoaded,
    ) { mutableStateOf(cameraPositionState.position.zoom >= 15f) }
    val latestOnEvent by rememberUpdatedState(onEvent)
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        latestOnEvent(InclusiMapEvent.SetLocationPermissionGranted(isGranted))
    }
    val latestOnPlaceDetailsEvent by rememberUpdatedState(onPlaceDetailsEvent)
    val latestOnDismissAppIntro by rememberUpdatedState(onDismissAppIntro)
    val context = LocalContext.current
    val internetState = remember { InternetConnectionState(context) }
    val isInternetAvailable by internetState.state.collectAsStateWithLifecycle()
    var firstTimeAnimation by remember { mutableStateOf<Boolean?>(null) }
    var openPlaceDetailsBottomSheet by rememberSaveable { mutableStateOf(false) }
    var isNorth by remember(state.isMapLoaded) {
        mutableStateOf(
            (state.currentLocation?.bearing?.inNorthRange() == true) && state.currentLocation.tilt in TILT_RANGE,
        )
    }
    var isFindNorthBtnClicked by remember { mutableStateOf(false) }
    var isPresentationMode by remember { mutableStateOf(false) }
    val isSystemInDarkTheme = isSystemInDarkTheme()

    Reveal(
        revealCanvasState = revealCanvasState,
        revealState = revealState,
        onRevealableClick = { key ->
            scope.launch {
                when (key) {
                    RevealKeys.ADD_PLACE_TIP -> {
                        delay(1.seconds)
                        addPlaceBottomSheetState.show()
                        revealState.hide()
                        isPresentationMode = false
                    }

                    RevealKeys.PLACE_DETAILS_TIP -> {
                        latestOnEvent(
                            InclusiMapEvent.OnMappedPlaceSelected(
                                state.allMappedPlaces.find { it.id == "fd9aa418-bc04-46fe-8974-f0bb8c400969" }
                                    ?: return@launch,
                            ),
                        )
                        openPlaceDetailsBottomSheet = true
                        revealState.hide()
                    }
                }
            }
        },
        onOverlayClick = { key ->
            scope.launch {
                when (key) {
                    RevealKeys.ADD_PLACE_TIP -> {
                        revealState.hide()
                        isPresentationMode = false
                    }

                    RevealKeys.PLACE_DETAILS_TIP -> revealState.reveal(RevealKeys.ADD_PLACE_TIP)
                }
            }
        },
        overlayContent = { key -> if (isPresentationMode) OverlayContent(key) },
    ) {
        Box(
            modifier = modifier
                .fillMaxSize(),
        ) {
            GoogleMap(
                modifier = Modifier
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
                    settingsState.isFollowingSystemOn && isSystemInDarkTheme -> ComposeMapColorScheme.DARK
                    settingsState.isFollowingSystemOn && !isSystemInDarkTheme -> ComposeMapColorScheme.LIGHT
                    settingsState.isDarkThemeOn -> ComposeMapColorScheme.DARK
                    else -> ComposeMapColorScheme.DARK
                },
                onMapLoaded = {
                    latestOnEvent(InclusiMapEvent.OnMapLoad)
                    if (!appIntroState.showAppIntro) {
                        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
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
                            mutableFloatStateOf(
                                place.comments.map { it.accessibilityRate }.average().toFloat(),
                            )
                        }
                        if (isPresentationMode && state.allMappedPlaces.find { it.id == "fd9aa418-bc04-46fe-8974-f0bb8c400969" } != place) return@forEach
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
                                openPlaceDetailsBottomSheet = true
                                false
                            },
                            visible = showMarkers,
                        )
                    }
                }
            }
            if (isPresentationMode) {
                AddPlaceRevelation(revealState)
                PlaceDetailsRevelation(revealState)
            }
            if (!isFullScreenMode && !isNorth && state.isMapLoaded) {
                FindNorthWidget(
                    cameraPositionState = cameraPositionState,
                    settingsState = settingsState,
                    onFind = {
                        isFindNorthBtnClicked = true
                        onPlaceTravelScope.launch {
                            async {
                                cameraPositionState.animate(
                                    CameraUpdateFactory.newCameraPosition(
                                        with(cameraPositionState.position) {
                                            CameraPosition(
                                                target,
                                                zoom,
                                                0f,
                                                0f,
                                            )
                                        },
                                    ),
                                )
                            }.await()
                            delay(800)
                            isNorth = true
                            isFindNorthBtnClicked = false
                        }
                    },
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
                if (isPresentationMode) {
                    scope.launch {
                        revealState.reveal(RevealKeys.ADD_PLACE_TIP)
                    }
                }
            },
            onUpdateMappedPlace = { placeUpdated ->
                latestOnEvent(InclusiMapEvent.OnUpdateMappedPlace(placeUpdated))
            },
            onReport = onReport,
            reportState = reportState,
            downloadUserProfilePicture = downloadUserProfilePicture,
            allowedShowUserProfilePicture = allowedShowUserProfilePicture,
            userPicture = userProfilePicture,
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
                if (!isPresentationMode) {
                    latestOnEvent(InclusiMapEvent.OnAddNewMappedPlace(newPlace))
                }
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
        ServerConnectionErrorDialog(
            onRetry = {
                latestOnEvent(InclusiMapEvent.OnFailToConnectToServer(false))
            },
        )
    }

    AnimatedVisibility(!isServerAvailable) {
        ServerUnavailableDialog(
            isInternetAvailable = isInternetAvailable,
            isRetrying = isCheckingServerAvailability,
            isServerAvailable = isServerAvailable,
            onRetry = { onTryReconnect() },
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

    DisposableEffect(Unit) {
        latestOnEvent(InclusiMapEvent.LoadCachedPlaces)
        latestOnEvent(InclusiMapEvent.GetCurrentState)
        if (!appIntroState.showAppIntro) firstTimeAnimation = false
        onDispose { }
    }

    DisposableEffect(state.allMappedPlaces, state.useAppWithoutInternet, isInternetAvailable) {
        if (state.allMappedPlaces.isEmpty() || state.useAppWithoutInternet && isInternetAvailable) {
            latestOnEvent(InclusiMapEvent.OnLoadPlaces)
        }
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
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            isPresentationMode = true
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
                            LatLng(position.first, position.second), 18f,
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

enum class RevealKeys {
    ADD_PLACE_TIP,
    PLACE_DETAILS_TIP,
}

@Composable
fun BoxScope.Revelation(
    revealState: RevealState,
    key: RevealKeys,
    alignment: Alignment,
    shape: RevealShape,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .align(alignment)
            .statusBarsPadding()
            .revealable(
                key,
                revealState,
                shape,
            ),
    )
}

@Composable
fun BoxScope.PlaceDetailsRevelation(
    revealState: RevealState,
    modifier: Modifier = Modifier,
) {
    Revelation(
        revealState,
        RevealKeys.ADD_PLACE_TIP,
        Alignment.TopEnd,
        RevealShape.Circle,
        modifier
            .padding(top = 200.dp, end = 80.dp)
            .size(50.dp)
            .clip(CircleShape),
    )
}

@Composable
fun BoxScope.AddPlaceRevelation(
    revealState: RevealState,
    modifier: Modifier = Modifier,
) {
    Revelation(
        revealState,
        RevealKeys.PLACE_DETAILS_TIP,
        Alignment.Center,
        RevealShape.RoundRect(16.dp),
        modifier
            .padding(bottom = 70.dp)
            .fillMaxWidth(0.55f)
            .height(200.dp),
    )
}

@Composable
fun RevealOverlayScope.OverlayContent(
    key: Any,
    modifier: Modifier = Modifier,
) {
    when (key) {
        RevealKeys.ADD_PLACE_TIP -> OverlayText(
            text = "Clique e segure para\nadicionar novos locais",
            arrow = Arrow.bottom(),
            modifier = modifier
                .align(verticalArrangement = RevealOverlayArrangement.Top),
        )

        RevealKeys.PLACE_DETAILS_TIP -> OverlayText(
            text = "Clique no marcador para\nver os detalhes do local",
            arrow = Arrow.bottom(),
            modifier = Modifier
                .align(verticalArrangement = RevealOverlayArrangement.Top),
        )
    }
}
