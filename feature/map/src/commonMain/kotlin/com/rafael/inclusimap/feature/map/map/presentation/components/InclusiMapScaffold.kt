package com.rafael.inclusimap.feature.map.map.presentation.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AddLocationAlt
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import com.rafael.inclusimap.feature.map.map.domain.model.InclusiMapState
import com.rafael.inclusimap.feature.map.map.domain.model.NavigationBarItem
import com.rafael.inclusimap.feature.map.map.presentation.MapTypeToggleButton
import com.rafael.inclusimap.feature.map.search.domain.model.SearchEvent
import com.rafael.inclusimap.feature.map.search.domain.model.SearchState
import com.rafael.inclusimap.feature.map.search.presentation.PlaceSearchLayout
import com.rafael.libs.maps.interop.model.MapType

@Composable
fun InclusiMapScaffold(
    state: InclusiMapState,
    searchState: SearchState,
    userProfilePicture: ByteArray?,
    searchEvent: (SearchEvent) -> Unit,
    onMapTypeChange: (MapType) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToContributions: () -> Unit,
    onNavigateToExplore: (fromContributionScreen: Boolean) -> Unit,
    onTravelToPlace: (String) -> Unit,
    onFullScreenModeChange: (Boolean) -> Unit,
    isFullScreenMode: Boolean,
    isSearchHistoryEnabled: Boolean,
    selectedMapType: MapType,
    modifier: Modifier = Modifier,
    content: @Composable (PaddingValues, Boolean) -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    val items = listOf(
        NavigationBarItem(
            selected = !searchState.expanded && !state.isContributionsScreen,
            onClick = {
                onNavigateToExplore(state.isContributionsScreen)
                searchEvent(SearchEvent.SetExpanded(false))
            },
            icon = Icons.Outlined.Explore,
            name = "Explorar",
        ),
        NavigationBarItem(
            selected = searchState.expanded && !state.isContributionsScreen,
            onClick = {
                onNavigateToExplore(state.isContributionsScreen)
                searchEvent(SearchEvent.SetExpanded(true))
            },
            icon = Icons.Default.Search,
            name = "Pesquisar",
        ),
        NavigationBarItem(
            selected = state.isContributionsScreen,
            onClick = {
                searchEvent(SearchEvent.SetExpanded(false))
                onNavigateToContributions()
            },
            icon = Icons.Outlined.AddLocationAlt,
            name = "Contribuições",
        ),
    )

    Scaffold(
        modifier = modifier
            .fillMaxSize(),
        floatingActionButton = {
            if (state.isMapLoaded && !state.isContributionsScreen) {
                MapTypeToggleButton(
                    selectedMapType = selectedMapType,
                    onMapTypeChange = { onMapTypeChange(it) },
                    isFullScreenMode = isFullScreenMode,
                    onFullScreenModeChange = { onFullScreenModeChange(it) },
                )
            }
        },
        topBar = {
            if (!isFullScreenMode && !state.isContributionsScreen) {
                PlaceSearchLayout(
                    searchState = searchState,
                    onSearchEvent = searchEvent,
                    onNavigateToSettings = onNavigateToSettings,
                    onTravelToPlace = onTravelToPlace,
                    focusRequester = focusRequester,
                    allMappedPlaces = state.allMappedPlaces,
                    profilePicture = userProfilePicture,
                    isHistoryEnabled = isSearchHistoryEnabled,
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
                                Text(item.name)
                            },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        content(innerPadding, isFullScreenMode)
    }
}
