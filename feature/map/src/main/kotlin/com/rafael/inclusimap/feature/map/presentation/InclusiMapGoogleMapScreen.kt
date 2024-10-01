package com.rafael.inclusimap.feature.map.presentation

import android.Manifest
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.twotone.ManageAccounts
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
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.rafael.inclusimap.core.settings.domain.model.SettingsState
import com.rafael.inclusimap.core.domain.model.util.toHUE
import com.rafael.inclusimap.feature.map.domain.InclusiMapEvent
import com.rafael.inclusimap.feature.map.domain.InclusiMapState
import com.rafael.inclusimap.feature.intro.domain.model.AppIntroState
import com.rafael.inclusimap.feature.intro.presentation.dialogs.AppIntroDialog
import com.rafael.inclusimap.core.resources.R
import com.rafael.inclusimap.feature.map.search.domain.model.SearchEvent
import com.rafael.inclusimap.feature.map.search.domain.model.SearchState
import com.rafael.inclusimap.feature.map.search.presentation.PlaceSearchScreen
import com.rafael.inclusimap.feature.map.domain.PlaceDetailsEvent
import com.rafael.inclusimap.feature.map.domain.PlaceDetailsState
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
    searchState: SearchState,
    onSearchEvent: (SearchEvent) -> Unit,
    settingsState: SettingsState,
    onMapTypeChange: (MapType) -> Unit,
    fusedLocationClient: FusedLocationProviderClient,
    onNavigateToSettings: () -> Unit,
    userName: String,
    userEmail: String,
    modifier: Modifier = Modifier,
) {
    var animateMap by remember { mutableStateOf(false) }
    val cameraPositionState = rememberCameraPositionState()
    val bottomSheetScaffoldState = rememberModalBottomSheetState()
    val bottomSheetScope = rememberCoroutineScope()
    val onPlaceTravelScope = rememberCoroutineScope()
    val addPlaceBottomSheetScaffoldState = rememberModalBottomSheetState()
    val addPlaceBottomSheetScope = rememberCoroutineScope()
    val locationPermission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    val showMarkers by remember(cameraPositionState.isMoving) { mutableStateOf(cameraPositionState.position.zoom >= 15f) }
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(animateMap && !appIntroState.showAppIntro) {
        if (animateMap) {
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
        }
    }
    LaunchedEffect(state.isLocationPermissionGranted) {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                onEvent(
                    InclusiMapEvent.UpdateMapCameraPosition(
                        LatLng(
                            it.latitude,
                            it.longitude,
                        ),
                        true,
                    ).also { pos ->
                        launch {
                            cameraPositionState.animate(
                                update = CameraUpdateFactory.newLatLngZoom(
                                    pos.latLng,
                                    25f,
                                ),
                                durationMs = 3500,
                            )
                        }
                    },
                )
            }
        }
    }

    LaunchedEffect(locationPermission.status) {
        onEvent(InclusiMapEvent.SetLocationPermissionGranted(locationPermission.status == PermissionStatus.Granted))
    }

    Box(modifier = Modifier.fillMaxSize()) {
        SearchBar(
            modifier = Modifier
                .navigationBarsPadding()
                .displayCutoutPadding()
                .statusBarsPadding()
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .clip(RoundedCornerShape(24.dp))
                .semantics { traversalIndex = -1f },
            inputField = {
                SearchBarDefaults.InputField(
                    query = searchState.searchQuery,
                    onQueryChange = {
                        onSearchEvent(SearchEvent.OnSearch(it, state.allMappedPlaces))
                    },
                    onSearch = {
                        expanded = false
                    },
                    expanded = expanded,
                    onExpandedChange = {
                        expanded = it
                    },
                    placeholder = { Text("Pesquise um local aqui") },
                    leadingIcon = {
                        if (expanded) {
                            IconButton(
                                onClick = {
                                    expanded = false
                                    onSearchEvent(SearchEvent.OnSearch("", emptyList()))
                                },
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = null,
                                )
                            }
                        } else {
                            Image(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape),
                                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                                contentDescription = null,
                            )
                        }
                    },
                    trailingIcon = {
                        if (searchState.searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    onSearchEvent(SearchEvent.OnSearch("", emptyList()))
                                },
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Close,
                                    contentDescription = null,
                                )
                            }
                        }
                        if (!expanded) {
                            IconButton(
                                onClick = {
                                    onNavigateToSettings()
                                },
                            ) {
                                Icon(
                                    imageVector = Icons.TwoTone.ManageAccounts,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(35.dp),
                                )
                            }
                        }
                    },
                )
            },
            expanded = expanded,
            onExpandedChange = { expanded = it },
            colors = SearchBarDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surface,
                dividerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
            ),
        ) {
            PlaceSearchScreen(
                matchingPlaces = searchState.matchingPlaces,
                onPlaceClick = {
                    expanded = false
                    onSearchEvent(SearchEvent.OnSearch("", emptyList()))
                    onPlaceTravelScope.launch {
                        cameraPositionState.animate(
                            CameraUpdateFactory.newLatLngZoom(
                                it,
                                20f,
                            ),
                            2500,
                        )
                    }
                },
            )
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
                compassEnabled = true,
                rotationGesturesEnabled = true,
                zoomControlsEnabled = false,
            ),
            cameraPositionState = cameraPositionState,
            onMapClick = {
                println("latitude ${it.latitude}" + "," + it.longitude)
            },
            mapColorScheme = if (settingsState.isDarkThemeOn) ComposeMapColorScheme.DARK else ComposeMapColorScheme.LIGHT,
            onMapLoaded = {
                onEvent(InclusiMapEvent.OnMapLoaded)
                if (!animateMap && !appIntroState.showAppIntro) {
                    cameraPositionState.position = CameraPosition.fromLatLngZoom(
                        state.defaultLocationLatLng,
                        15f,
                    )
                    locationPermission.launchPermissionRequest()
                }
            },
            onMapLongClick = {
                onEvent(InclusiMapEvent.OnUnmappedPlaceSelected(it))
                addPlaceBottomSheetScope.launch {
                    addPlaceBottomSheetScaffoldState.show()
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
                        snippet = place.category,
                        icon = BitmapDescriptorFactory.defaultMarker(
                            accessibilityAverage.toHUE(),
                        ),
                        onClick = {
                            onEvent(InclusiMapEvent.OnMappedPlaceSelected(place))
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
        if (state.isMapLoaded) {
            MapTypeToggleButton(settingsState.mapType, onMapTypeChange = { onMapTypeChange(it) })
        }
    }

    AnimatedVisibility(appIntroState.showAppIntro) {
        AppIntroDialog(
            userName = userName,
            onDismiss = {
                onDismissAppIntro(false)
                animateMap = true
            },
        )
    }

    AnimatedVisibility(bottomSheetScaffoldState.isVisible) {
        PlaceDetailsBottomSheet(
            state = placeDetailsState,
            inclusiMapState = state,
            onEvent = onPlaceDetailsEvent,
            userName = userName,
            userEmail = userEmail,
            bottomSheetScaffoldState = bottomSheetScaffoldState,
            onDismiss = {
                onPlaceDetailsEvent(PlaceDetailsEvent.OnDestroyPlaceDetails)
                bottomSheetScope.launch {
                    bottomSheetScaffoldState.hide()
                }
            },
            onUpdateMappedPlace = { placeUpdated ->
                onEvent(InclusiMapEvent.OnUpdateMappedPlace(placeUpdated))
            },
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
                onPlaceDetailsEvent(PlaceDetailsEvent.SetIsEditingPlace(false))
            },
            onAddNewPlace = { newPlace ->
                onEvent(InclusiMapEvent.OnAddNewMappedPlace(newPlace))
            },
            isEditing = placeDetailsState.isEditingPlace,
            onEditNewPlace = {
                onEvent(InclusiMapEvent.OnUpdateMappedPlace(it))
                onPlaceDetailsEvent(PlaceDetailsEvent.SetCurrentPlace(it))
            },
            onDeletePlace = {
                onEvent(InclusiMapEvent.OnDeleteMappedPlace(it))
                onPlaceDetailsEvent(PlaceDetailsEvent.SetIsEditingPlace(false))
                bottomSheetScope.launch {
                    bottomSheetScaffoldState.hide()
                }
                addPlaceBottomSheetScope.launch {
                    addPlaceBottomSheetScaffoldState.hide()
                }
            },
        )
    }
}
