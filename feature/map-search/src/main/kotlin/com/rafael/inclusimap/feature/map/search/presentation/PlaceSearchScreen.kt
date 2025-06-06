package com.rafael.inclusimap.feature.map.search.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rafael.inclusimap.core.domain.model.AccessibleLocalMarker
import com.rafael.inclusimap.core.domain.util.toColor
import com.rafael.inclusimap.core.resources.draws.GoogleMapsPin
import com.rafael.inclusimap.feature.map.search.domain.model.SearchState
import com.rafael.inclusimap.feature.map.search.presentation.dialogs.AboutHistoryDialog

@Composable
fun PlaceSearchScreen(
    state: SearchState,
    allMappedPlaces: List<AccessibleLocalMarker>,
    isHistoryEnabled: Boolean,
    onPlaceClick: (String) -> Unit,
    onLoadHistory: () -> Unit,
    onDeleteHistory: () -> Unit,
    onRemoveFromHistory: (String) -> Unit,
    onRemoveInexistentPlacesFromHistory: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val shouldShowHistoryUI =
        state.matchingPlaces.isEmpty() && state.searchQuery.isEmpty() && state.placesHistory.isNotEmpty() && isHistoryEnabled
    var showAboutHistoryDialog by remember { mutableStateOf(false) }
    val latestOnLoadHistory by rememberUpdatedState(onLoadHistory)
    val latestOnDeleteHistory by rememberUpdatedState(onDeleteHistory)

    LaunchedEffect(Unit) {
        if (isHistoryEnabled) {
            latestOnLoadHistory()
        } else {
            latestOnDeleteHistory()
        }
    }
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start,
    ) {
        if (state.matchingPlaces.isNotEmpty()) {
            Text(
                text = "Resultados da pesquisa:",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 12.dp),
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
        if (shouldShowHistoryUI) {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Visto recentemente:",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                )
                IconButton(
                    onClick = {
                        showAboutHistoryDialog = true
                    },
                    modifier = Modifier
                        .size(22.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 2.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp))
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onVerticalDrag = { _, _ ->
                            focusManager.clearFocus()
                        },
                    )
                    detectTapGestures(
                        onTap = { focusManager.clearFocus() },
                    )
                }
                .animateContentSize(),
        ) {
            // History UI
            if (shouldShowHistoryUI) {
                state.placesHistory.forEachIndexed { index, place ->
                    if (place !in allMappedPlaces.map { it.id }) {
                        onRemoveInexistentPlacesFromHistory(place)
                        return@forEachIndexed
                    }
                    val placeStored =
                        allMappedPlaces.find { it.id == place } ?: return@forEachIndexed
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(62.dp)
                                .clickable {
                                    onPlaceClick(placeStored.id ?: return@clickable)
                                }
                                .padding(6.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.History,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                            )
                            Column(
                                modifier = Modifier
                                    .padding(start = 4.dp)
                                    .weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp),
                            ) {
                                Text(
                                    text = placeStored.title,
                                    maxLines = 1,
                                    fontSize = 14.sp,
                                    lineHeight = 16.sp,
                                )
                                Text(
                                    text = placeStored.address + " - " + placeStored.locatedIn,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 14.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            GoogleMapsPin(
                                pinColor = placeStored.comments.map { it.accessibilityRate }
                                    .average()
                                    .toFloat()
                                    .toColor(),
                                pinSize = 46.dp,
                            )
                            IconButton(
                                onClick = {
                                    onRemoveFromHistory(placeStored.id ?: return@IconButton)
                                },
                                modifier = Modifier.size(24.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Close,
                                    contentDescription = null,
                                )
                            }
                        }
                        if (index < state.placesHistory.size - 1) {
                            HorizontalDivider(
                                thickness = 3.dp,
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            }
            // Search Results UI
            if (state.matchingPlaces.isEmpty() && state.searchQuery.isNotEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(
                                4.dp,
                                Alignment.CenterVertically,
                            ),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = null,
                                modifier = Modifier.size(35.dp),
                            )
                            Text(
                                text = "Nenhum local encontrado para a pesquisa: ${state.searchQuery}",
                                maxLines = 2,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            } else {
                state.matchingPlaces.forEachIndexed { index, place ->
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(62.dp)
                                .clickable {
                                    onPlaceClick(place.id ?: return@clickable)
                                }
                                .padding(6.dp),
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(start = 4.dp)
                                    .weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp),
                            ) {
                                Text(
                                    text = place.title,
                                    maxLines = 1,
                                    fontSize = 14.sp,
                                    lineHeight = 18.sp,
                                )
                                Text(
                                    text = place.address + " - " + place.locatedIn,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 14.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            GoogleMapsPin(
                                pinColor = place.comments.map { it.accessibilityRate }.average()
                                    .toFloat()
                                    .toColor(),
                                pinSize = 46.dp,
                            )
                        }
                        if (index < state.matchingPlaces.size - 1) {
                            HorizontalDivider(
                                thickness = 3.dp,
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            }
        }
    }

    AnimatedVisibility(showAboutHistoryDialog) {
        AboutHistoryDialog(
            onDismiss = { showAboutHistoryDialog = false },
        )
    }
}
