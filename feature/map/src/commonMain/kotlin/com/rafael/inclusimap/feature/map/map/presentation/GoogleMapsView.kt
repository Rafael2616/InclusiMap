package com.rafael.inclusimap.feature.map.map.presentation

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.rafael.inclusimap.core.util.map.model.Location
import com.rafael.inclusimap.feature.map.map.domain.model.InclusiMapEvent
import com.rafael.inclusimap.feature.map.map.domain.model.InclusiMapState
import com.rafael.libs.maps.interop.model.MapType
import com.svenjacobs.reveal.RevealState
import kotlinx.coroutines.CoroutineScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
expect fun BoxScope.GoogleMapsView(
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
    snackbarHostState: androidx.compose.material3.SnackbarHostState,
    onSetPresentationMode: (Boolean) -> Unit,
    location: Location?,
    isFullScreenMode: Boolean,
    modifier: Modifier = Modifier,
)
