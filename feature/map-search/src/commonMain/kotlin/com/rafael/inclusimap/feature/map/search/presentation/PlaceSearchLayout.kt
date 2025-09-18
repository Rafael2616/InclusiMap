package com.rafael.inclusimap.feature.map.search.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.unit.dp
import com.rafael.inclusimap.core.resources.Res
import com.rafael.inclusimap.core.resources.ic_splash
import com.rafael.inclusimap.core.ui.isLandscape
import com.rafael.inclusimap.core.util.map.model.AccessibleLocalMarker
import com.rafael.inclusimap.feature.map.search.domain.model.SearchEvent
import com.rafael.inclusimap.feature.map.search.domain.model.SearchState
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceSearchLayout(
    searchState: SearchState,
    isHistoryEnabled: Boolean,
    allMappedPlaces: List<AccessibleLocalMarker>,
    onSearchEvent: (SearchEvent) -> Unit,
    onNavigateToSettings: () -> Unit,
    onTravelToPlace: (String) -> Unit,
    focusRequester: FocusRequester,
    profilePicture: ImageBitmap?,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .then(
                if (searchState.expanded) {
                    Modifier.pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                onSearchEvent(SearchEvent.SetExpanded(false))
                                onSearchEvent(SearchEvent.OnSearch("", emptyList()))
                            },
                        )
                    }
                } else {
                    Modifier
                },
            ),
        contentAlignment = Alignment.TopCenter,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(if (isLandscape) 0.8f else 0.6f),
            contentAlignment = Alignment.TopCenter,
        ) {
            SearchBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .displayCutoutPadding()
                    .padding(horizontal = 8.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .semantics { traversalIndex = -1f },
                inputField = {
                    SearchBarDefaults.InputField(
                        modifier = Modifier.focusRequester(focusRequester),
                        query = searchState.searchQuery,
                        onQueryChange = {
                            onSearchEvent(SearchEvent.OnSearch(it, allMappedPlaces))
                        },
                        onSearch = {
                            onSearchEvent(SearchEvent.SetExpanded(false))
                        },
                        expanded = searchState.expanded,
                        onExpandedChange = {
                            onSearchEvent(SearchEvent.SetExpanded(it))
                        },
                        placeholder = { Text("Pesquise um local aqui") },
                        leadingIcon = {
                            if (searchState.expanded) {
                                IconButton(
                                    onClick = {
                                        onSearchEvent(SearchEvent.SetExpanded(false))
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
                                        .size(45.dp)
                                        .clip(CircleShape),
                                    painter = painterResource(Res.drawable.ic_splash),
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
                            if (!searchState.expanded) {
                                if (profilePicture != null) {
                                    Image(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .clickable {
                                                onNavigateToSettings()
                                            },
                                        bitmap = profilePicture,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                    )
                                } else {
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
                            }
                        },
                    )
                },
                expanded = searchState.expanded,
                onExpandedChange = { onSearchEvent(SearchEvent.SetExpanded(it)) },
                colors = SearchBarDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    dividerColor = MaterialTheme.colorScheme.surface,
                ),
            ) {
                PlaceSearchScreen(
                    state = searchState,
                    allMappedPlaces = allMappedPlaces,
                    onPlaceClick = {
                        onSearchEvent(SearchEvent.SetExpanded(false))
                        onTravelToPlace(it)
                        onSearchEvent(SearchEvent.OnSearch("", emptyList()))
                        if (isHistoryEnabled) {
                            onSearchEvent(SearchEvent.UpdateHistory(it))
                        }
                    },
                    onLoadHistory = {
                        onSearchEvent(SearchEvent.LoadHistory)
                    },
                    onRemoveFromHistory = {
                        onSearchEvent(SearchEvent.DeleteFromHistory(it))
                    },
                    isHistoryEnabled = isHistoryEnabled,
                    onDeleteHistory = {
                        onSearchEvent(SearchEvent.ClearHistory)
                    },
                    onRemoveInexistentPlacesFromHistory = {
                        onSearchEvent(SearchEvent.RemoveNonexistentPlaceFromHistory(it))
                    },
                )
            }
        }
        DisposableEffect(searchState.expanded) {
            if (searchState.expanded) {
                focusRequester.requestFocus()
            }
            onDispose { }
        }
    }
}
