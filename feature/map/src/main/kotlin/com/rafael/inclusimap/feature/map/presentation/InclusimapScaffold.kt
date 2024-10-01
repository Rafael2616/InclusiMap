package com.rafael.inclusimap.feature.map.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
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
import com.google.maps.android.compose.MapType
import com.rafael.inclusimap.core.settings.domain.model.SettingsState
import com.rafael.inclusimap.feature.map.domain.InclusiMapState
import com.rafael.inclusimap.feature.map.search.domain.model.SearchEvent
import com.rafael.inclusimap.feature.map.search.domain.model.SearchState

@Composable
fun InclusiMapScaffold(
    state: InclusiMapState,
    searchState: SearchState,
    settingsState: SettingsState,
    searchEvent: (SearchEvent) -> Unit,
    onMapTypeChange: (MapType) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val items = listOf(
        NavigationBarItem(
            selected = !searchState.expanded,
            onClick = {
                searchEvent(SearchEvent.SetExpanded(false))
            },
            icon = Icons.Default.Explore,
            name = "Explorar",
        ),
        NavigationBarItem(
            selected = searchState.expanded,
            onClick = {
                searchEvent(SearchEvent.SetExpanded(true))
            },
            icon = Icons.Default.Search,
            name = "Pesquisar",
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
                )
            }
        },
        bottomBar = {
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
        },
    ) { innerPadding ->
        content()
    }
}

data class NavigationBarItem(
    val selected: Boolean,
    val onClick: () -> Unit,
    val icon: ImageVector,
    val name: String,
)
