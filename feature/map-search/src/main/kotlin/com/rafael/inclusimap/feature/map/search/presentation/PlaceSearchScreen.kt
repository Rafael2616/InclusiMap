package com.rafael.inclusimap.feature.map.search.presentation

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.model.LatLng
import com.rafael.inclusimap.core.domain.model.AccessibleLocalMarker
import com.rafael.inclusimap.core.domain.model.util.toColor
import com.rafael.inclusimap.core.resources.icons.GoogleMapsPin

@Composable
fun PlaceSearchScreen(
    matchingPlaces: List<AccessibleLocalMarker>,
    query: String,
    onPlaceClick: (LatLng) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start,
    ) {
        if (matchingPlaces.isNotEmpty()) {
            Text(
                text = "Resultados da pesquisa:",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 12.dp),
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceColorAtElevation(10.dp))
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
            if (matchingPlaces.isEmpty() && query.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
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
                            text = "Nenhum local encontrado para a pesquisa  $query",
                            maxLines = 2,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } else {
                matchingPlaces.forEachIndexed { index, place ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp)
                            .clickable {
                                onPlaceClick(
                                    LatLng(
                                        place.position.first,
                                        place.position.second,
                                    ),
                                )
                            }
                            .padding(horizontal = 6.dp, vertical = 8.dp),
                    ) {
                        Text(
                            text = place.title,
                            fontSize = 14.sp,
                            modifier = Modifier.weight(1f),
                        )
                        GoogleMapsPin(
                            pinColor = place.comments.map { it.accessibilityRate }.average()
                                .toFloat()
                                .toColor(),
                            pinSize = 46.dp,
                        )
                    }
                    if (index < matchingPlaces.size - 1) {
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
