package com.rafael.inclusimap.feature.map.presentation

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.MapType
import com.rafael.inclusimap.core.settings.domain.model.SettingsState
import com.rafael.inclusimap.feature.map.domain.InclusiMapEvent
import com.rafael.inclusimap.feature.map.domain.InclusiMapState
import com.rafael.inclusimap.feature.map.search.domain.model.SearchEvent
import com.rafael.inclusimap.feature.map.search.domain.model.SearchState

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun InclusiMapScaffold(
    state: InclusiMapState,
    searchState: SearchState,
    settingsState: SettingsState,
    onEvent: (InclusiMapEvent) -> Unit,
    searchEvent: (SearchEvent) -> Unit,
    onMapTypeChange: (MapType) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToContributions: () -> Unit,
    onTravelToPlace: (LatLng) -> Unit,
    onFullScreenModeChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    isFullScreenMode: Boolean = false,
    content: @Composable () -> Unit,
) {
    val items = listOf(
        NavigationBarItem(
            selected = !searchState.expanded && !state.isContributionsScreen,
            onClick = {
                searchEvent(SearchEvent.SetExpanded(false))
                onEvent(InclusiMapEvent.SetIsContributionsScreen(false))
            },
            icon = Icons.Default.Explore,
            name = "Explorar",
        ),
        NavigationBarItem(
            selected = searchState.expanded && !state.isContributionsScreen,
            onClick = {
                searchEvent(SearchEvent.SetExpanded(true))
                onEvent(InclusiMapEvent.SetIsContributionsScreen(false))
            },
            icon = Icons.Default.Search,
            name = "Pesquisar",
        ),
        NavigationBarItem(
            selected = state.isContributionsScreen,
            onClick = {
                onEvent(InclusiMapEvent.SetIsContributionsScreen(true))
                onNavigateToContributions()
            },
            icon = Icons.Default.AddLocationAlt,
            name = "Contribuições",
        ),
    )

    Scaffold(
        modifier = modifier
            .fillMaxSize(),
        floatingActionButton = {
            if (state.isMapLoaded) {
                MapTypeToggleButton(
                    settingsState.mapType,
                    onMapTypeChange = { onMapTypeChange(it) },
                    isFullScreenMode = isFullScreenMode,
                    onFullScreenModeChange = { onFullScreenModeChange(it) },
                )
            }
        },
        topBar = {
            if (!isFullScreenMode) {
                PlaceSearchLayout(
                    state = state,
                    searchState = searchState,
                    onSearchEvent = searchEvent,
                    settingsState = settingsState,
                    onNavigateToSettings = onNavigateToSettings,
                    onTravelToPlace = onTravelToPlace,
                )
            }
        },
        bottomBar = {
            if (!isFullScreenMode) {
                NavigationBar {
                    items.forEach { item ->
                        NavigationBarItem(
                            selected = item.selected,
                            onClick = item.onClick,
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = null,
                                )
                            },
                            label = {
                                Text(
                                    item.name,
                                )
                            },
                        )
                    }
                }
            }
        },
    ) {
        content()
    }
}

data class NavigationBarItem(
    val selected: Boolean,
    val onClick: () -> Unit,
    val icon: ImageVector,
    val name: String,
)
