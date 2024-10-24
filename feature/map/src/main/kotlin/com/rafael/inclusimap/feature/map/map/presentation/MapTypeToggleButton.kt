package com.rafael.inclusimap.feature.map.map.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Check
import androidx.compose.material.icons.twotone.Fullscreen
import androidx.compose.material.icons.twotone.FullscreenExit
import androidx.compose.material.icons.twotone.Layers
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.maps.android.compose.MapType
import com.rafael.inclusimap.feature.map.map.domain.GoogleMapType

@Composable
fun MapTypeToggleButton(
    selectedMapType: MapType,
    onMapTypeChange: (MapType) -> Unit,
    isFullScreenMode: Boolean,
    onFullScreenModeChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showMapTypes by remember { mutableStateOf(false) }
    val mapTypes = GoogleMapType.getMapTypes()
    Column(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.End,
    ) {
        FloatingActionButton(
            onClick = { onFullScreenModeChange(!isFullScreenMode) },
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .size(45.dp),
            shape = RoundedCornerShape(16.dp),
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            Icon(
                imageVector = if (isFullScreenMode) Icons.TwoTone.FullscreenExit else Icons.TwoTone.Fullscreen,
                contentDescription = "Immersive mode",
                modifier = Modifier.size(30.dp),
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (!isFullScreenMode) {
            FloatingActionButton(
                onClick = { showMapTypes = !showMapTypes },
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .size(60.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                Icon(
                    imageVector = Icons.TwoTone.Layers,
                    contentDescription = "Tipos de Mapa",
                    modifier = Modifier.size(40.dp),
                )
            }
        }
        Box(
            modifier = Modifier.wrapContentSize(),
            contentAlignment = Alignment.CenterEnd,
        ) {
            DropdownMenu(
                expanded = showMapTypes,
                onDismissRequest = { showMapTypes = false },
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.background(MaterialTheme.colorScheme.surface),
            ) {
                Text(
                    text = "Tipo de mapa",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
                )
                mapTypes.forEach { mapType ->
                    DropdownMenuItem(
                        onClick = {
                            onMapTypeChange(mapType.type)
                            showMapTypes = false
                        },
                        text = {
                            Text(
                                text = mapType.name,
                            )
                        },
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        trailingIcon = {
                            Icon(
                                imageVector = mapType.icon,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                            )
                        },
                        leadingIcon = {
                            if (selectedMapType == mapType.type) {
                                Icon(
                                    imageVector = Icons.TwoTone.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                )
                            }
                        },
                    )
                }
            }
        }
    }
}
