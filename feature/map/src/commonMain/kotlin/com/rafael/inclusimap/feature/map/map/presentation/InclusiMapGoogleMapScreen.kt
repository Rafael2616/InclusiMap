package com.rafael.inclusimap.feature.map.map.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rafael.inclusimap.core.util.map.model.Location
import com.rafael.inclusimap.core.util.network.InternetConnectionState
import com.rafael.inclusimap.feature.map.map.domain.model.InclusiMapEvent
import com.rafael.inclusimap.feature.map.map.domain.model.InclusiMapState
import com.rafael.inclusimap.feature.map.map.domain.model.MapConstants.PLACE_DEMO_ID
import com.rafael.inclusimap.feature.map.map.domain.model.RevealKeys
import com.rafael.inclusimap.feature.map.map.presentation.components.AddEditPlaceBottomSheet
import com.rafael.inclusimap.feature.map.map.presentation.components.AddPlaceRevelation
import com.rafael.inclusimap.feature.map.map.presentation.components.OverlayContent
import com.rafael.inclusimap.feature.map.map.presentation.components.PlaceDetailsRevelation
import com.rafael.inclusimap.feature.map.map.presentation.dialog.PlacesNotLoadedDialog
import com.rafael.inclusimap.feature.map.map.presentation.dialog.PlacesNotUpdatedDialog
import com.rafael.inclusimap.feature.map.map.presentation.dialog.ServerConnectionErrorDialog
import com.rafael.inclusimap.feature.map.map.presentation.dialog.ServerUnavailableDialog
import com.rafael.inclusimap.feature.map.placedetails.domain.model.PlaceDetailsEvent
import com.rafael.inclusimap.feature.map.placedetails.domain.model.PlaceDetailsState
import com.rafael.inclusimap.feature.map.placedetails.presentation.PlaceDetailsBottomSheet
import com.rafael.inclusimap.feature.report.domain.model.Report
import com.rafael.inclusimap.feature.report.domain.model.ReportState
import com.rafael.libs.maps.interop.model.MapType
import com.rafael.libs.maps.interop.model.MapsLatLng
import com.svenjacobs.reveal.Reveal
import com.svenjacobs.reveal.RevealCanvasState
import com.svenjacobs.reveal.rememberRevealState
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InclusiMapGoogleMapScreen(
    state: InclusiMapState,
    onEvent: (InclusiMapEvent) -> Unit,
    placeDetailsState: PlaceDetailsState,
    onPlaceDetailsEvent: (PlaceDetailsEvent) -> Unit,
    revealCanvasState: RevealCanvasState,
    onDismissAppIntro: (Boolean) -> Unit,
    onUpdateSearchHistory: (String) -> Unit,
    onTryReconnect: () -> Unit,
    isServerAvailable: Boolean,
    isCheckingServerAvailability: Boolean,
    userName: String,
    userEmail: String,
    isPresentationMode: Boolean,
    userProfilePicture: ByteArray?,
    isFullScreenMode: Boolean,
    onReport: (Report) -> Unit,
    reportState: ReportState,
    location: Location?,
    allowedShowUserProfilePicture: suspend (String) -> Boolean,
    downloadUserProfilePicture: suspend (String) -> ByteArray?,
    onSetPresentationMode: (Boolean) -> Unit,
    isFollowingSystemOn: Boolean,
    isDarkThemeOn: Boolean,
    selectedMapType: MapType,
    showAppIntro: Boolean,
    appIntroDialog: @Composable ((userName: String, onDismiss: () -> Unit) -> Unit),
    snackbarHostState: SnackbarHostState,
    requestLocationPermission: suspend () -> Unit,
    modifier: Modifier = Modifier,
) {
    val revealState = rememberRevealState()
    val scope = rememberCoroutineScope()
    val addPlaceBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val addPlaceBottomSheetScope = rememberCoroutineScope()
    val latestOnEvent by rememberUpdatedState(onEvent)
    val latestOnPlaceDetailsEvent by rememberUpdatedState(onPlaceDetailsEvent)
    val latestOnDismissAppIntro by rememberUpdatedState(onDismissAppIntro)
    val internetState = remember { InternetConnectionState() }
    val isInternetAvailable by internetState.state.collectAsStateWithLifecycle()
    var openPlaceDetailsBottomSheet by rememberSaveable { mutableStateOf(false) }
    var firstTimeAnimation by remember { mutableStateOf<Boolean?>(null) }

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
                        onSetPresentationMode(false)
                    }

                    RevealKeys.PLACE_DETAILS_TIP -> {
                        latestOnEvent(
                            InclusiMapEvent.OnMappedPlaceSelected(
                                state.allMappedPlaces.find { it.id == PLACE_DEMO_ID }
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
                        onSetPresentationMode(false)
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
            GoogleMapsView(
                state = state,
                onEvent = latestOnEvent,
                addPlaceBottomSheetState = addPlaceBottomSheetState,
                addPlaceBottomSheetScope = addPlaceBottomSheetScope,
                showAppIntro = showAppIntro,
                onUpdateSearchHistory = onUpdateSearchHistory,
                isInternetAvailable = isInternetAvailable,
                isFollowingSystemOn = isFollowingSystemOn,
                isDarkThemeOn = isDarkThemeOn,
                selectedMapType = selectedMapType,
                isPresentationMode = isPresentationMode,
                openPlaceDetailsBottomSheet = {
                    openPlaceDetailsBottomSheet = it
                },
                requestLocationPermission = requestLocationPermission,
                revealState = revealState,
                snackbarHostState = snackbarHostState,
                onSetPresentationMode = onSetPresentationMode,
                location = location,
                isFullScreenMode = isFullScreenMode,
            )
            if (isPresentationMode) {
                AddPlaceRevelation(revealState)
                PlaceDetailsRevelation(revealState)
            }
        }
    }

    AnimatedVisibility(showAppIntro && firstTimeAnimation != true) {
        appIntroDialog(userName) {
            latestOnEvent(InclusiMapEvent.ResetState)
            latestOnDismissAppIntro(false)
            firstTimeAnimation = true
        }
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
            snackBarHostState = snackbarHostState,
        )
    }

    AnimatedVisibility(addPlaceBottomSheetState.isVisible || placeDetailsState.isEditingPlace) {
        AddEditPlaceBottomSheet(
            latlng = state.selectedUnmappedPlaceLatLng ?: MapsLatLng(0.0, 0.0),
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
                latestOnPlaceDetailsEvent(PlaceDetailsEvent.SetCurrentPlace(it, userEmail))
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
            snackBarHostState = snackbarHostState,
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
        if (!showAppIntro) firstTimeAnimation = false
        onDispose { }
    }

    LaunchedEffect(firstTimeAnimation) {
        if (firstTimeAnimation == true) {
            delay(500L)
            revealState.reveal(RevealKeys.PLACE_DETAILS_TIP)
        }
    }

    DisposableEffect(state.allMappedPlaces, state.useAppWithoutInternet, isInternetAvailable) {
        if (state.allMappedPlaces.isEmpty() || state.useAppWithoutInternet && isInternetAvailable) {
            latestOnEvent(InclusiMapEvent.OnLoadPlaces)
        }
        onDispose { }
    }
}
