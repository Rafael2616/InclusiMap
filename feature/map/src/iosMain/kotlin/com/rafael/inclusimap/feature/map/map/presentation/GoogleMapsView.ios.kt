package com.rafael.inclusimap.feature.map.map.presentation

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.interop.UIKitView
import cocoapods.GoogleMaps.GMSCameraPosition
import cocoapods.GoogleMaps.GMSMapStyle
import cocoapods.GoogleMaps.GMSMapView
import cocoapods.GoogleMaps.GMSMapViewDelegateProtocol
import cocoapods.GoogleMaps.GMSMarker
import com.rafael.inclusimap.core.util.map.model.Location
import com.rafael.inclusimap.feature.map.map.domain.MapStyle
import com.rafael.inclusimap.feature.map.map.domain.model.InclusiMapEvent
import com.rafael.inclusimap.feature.map.map.domain.model.InclusiMapState
import com.rafael.inclusimap.feature.map.map.domain.model.RevealKeys
import com.rafael.inclusimap.feature.map.map.domain.model.TILT_RANGE
import com.rafael.inclusimap.feature.map.map.domain.model.inNorthRange
import com.rafael.libs.maps.interop.model.MapBitmapDescriptorFactory
import com.rafael.libs.maps.interop.model.MapType
import com.rafael.libs.maps.interop.model.MapsLatLng
import com.rafael.libs.maps.interop.model.toLatLng
import com.rafael.libs.maps.interop.model.toMarkerIcon
import com.svenjacobs.reveal.RevealState
import kotlin.time.Duration.Companion.seconds
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import platform.CoreGraphics.CGRect
import platform.CoreLocation.CLLocationCoordinate2D
import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.Foundation.NSObject
import platform.UIKit.UIView

@OptIn(ExperimentalMaterial3Api::class, ExperimentalForeignApi::class)
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
    val systemDefault = MapStyle.SYSTEM_DEFAULT
    val cameraPosition = GMSCameraPosition.cameraWithLatitude(
        latitude = cameraPosition.latitude,
        longitude = cameraPosition.longitude,
        zoom = cameraPosition.zoom,
    )
    var firstTimeAnimation by remember { mutableStateOf<Boolean?>(null) }
    val latestOnEvent by rememberUpdatedState(onEvent)
    val latestOpenPlaceDetailsBottomSheet by rememberUpdatedState(openPlaceDetailsBottomSheet)
    val latestRequestLocationPermission by rememberUpdatedState(requestLocationPermission)
    val latestOnSetPresentationMode by rememberUpdatedState(onSetPresentationMode)
    val onPlaceTravelScope = rememberCoroutineScope()

    UIKitView(
        factory = {
            val gmsMapView = GMSMapView()
            gmsMapView.camera = camera

            gmsMapView.delegate = object : NSObject(), GMSMapViewDelegateProtocol {
                override fun mapView(mapView: GMSMapView, didTapAtCoordinate: CLLocationCoordinate2D) {
                    onEvent(
                        InclusiMapEvent.OnUnmappedPlaceSelected(
                            MapsLatLng(didTapAtCoordinate.latitude, didTapAtCoordinate.longitude),
                        ),
                    )
                    if (isInternetAvailable) {
                        addPlaceBottomSheetScope.launch {
                            addPlaceBottomSheetState.show()
                        }
                    }
                }
            }

            gmsMapView
        },
        onResize = { view: UIView, rect: CValue<CGRect> ->
            view.setFrame(rect)
        },
        update = { uiView ->
            val gmsMapView = uiView
            gmsMapView.clear()

            // Add markers
            state.allMappedPlaces.forEach { place ->
                val marker = GMSMarker()
                marker.position = CLLocationCoordinate2DMake(place.position.first, place.position.second)
                marker.title = place.title
                marker.icon = MapBitmapDescriptorFactory.HUE_RED.toMarkerIcon()
                marker.map = gmsMapView
            }

            gmsMapView.mapStyle = when {
                isFollowingSystemOn -> GMSMapStyle.styleWithJSONString(systemDefault, null)
                isDarkThemeOn -> GMSMapStyle.styleWithJSONString(MapStyle.DARK, null)
                else -> GMSMapStyle.styleWithJSONString(MapStyle.LIGHT, null)
            }
        },
        modifier = modifier,
    )

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
            cameraPosition = GMSCameraPosition(
                state.currentLocation?.target?.toLatLng() ?: state.defaultLocationLatLng.toLatLng(),
                state.currentLocation?.bearing?.toFloat() ?: 0f,
                state.currentLocation?.tilt?.toFloat() ?: 0f,
                state.currentLocation?.zoom?.toFloat() ?: 15f,
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
    DisposableEffect(!cameraPositionState.isMoving) {
        if (!cameraPositionState.isMoving && cameraPosition != defaultPos) {
            latestOnEvent(InclusiMapEvent.UpdateMapState(cameraPosition.toMapsCameraPosition()))
        }
        onDispose { }
    }

    LaunchedEffect(!cameraPosition.isMoving) {
        if (!cameraPosition.isMoving && !isFindNorthBtnClicked && cameraPosition.tilt in TILT_RANGE && cameraPosition.bearing.toDouble().inNorthRange()) {
            delay(800)
            isNorth = true
        }
    }

    DisposableEffect(cameraPosition.bearing, cameraPosition.tilt) {
        if (!cameraPosition.bearing.toDouble().inNorthRange() || cameraPosition.tilt !in TILT_RANGE) {
            isNorth = false
        }
        onDispose { }
    }
}
