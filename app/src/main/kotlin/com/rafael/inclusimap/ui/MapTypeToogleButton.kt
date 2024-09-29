package com.rafael.inclusimap.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Check
import androidx.compose.material.icons.twotone.Map
import androidx.compose.material.icons.twotone.Satellite
import androidx.compose.material.icons.twotone.SatelliteAlt
import androidx.compose.material.icons.twotone.Terrain
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
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.google.maps.android.compose.MapType
import com.rafael.inclusimap.data.getMapTypeName

@Composable
fun MapTypeToogleButton(
    selectedMapType: MapType,
    onMapTypeChange: (MapType) -> Unit,
) {
    var showMapTypes by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp)
            .padding(bottom = 24.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        FloatingActionButton(
            onClick = { showMapTypes = !showMapTypes },
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .size(60.dp)
                .background(MaterialTheme.colorScheme.primaryContainer),
            shape = RoundedCornerShape(16.dp),
        ) {
            Icon(
                imageVector = Icons.TwoTone.Map,
                contentDescription = "Tipos de Mapa",
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(bottom = 30.dp),
            contentAlignment = Alignment.TopEnd,
        ) {
            DropdownMenu(
                expanded = showMapTypes,
                onDismissRequest = { showMapTypes = false },
                shape = MaterialTheme.shapes.medium,
                offset = DpOffset(10.dp, (370).dp),
            ) {
                val mapTypes = listOf(
                    MapType.NORMAL,
                    MapType.SATELLITE,
                    MapType.TERRAIN,
                    MapType.HYBRID,
                )
                mapTypes.forEach { mapType ->
                    DropdownMenuItem(
                        onClick = {
                            onMapTypeChange(mapType)
                            showMapTypes = false

                        },
                        text = {
                            Text(
                                text = mapType.getMapTypeName(),
                            )
                        },
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        trailingIcon = {
                            Icon(
                                imageVector = when (mapType) {
                                    MapType.NORMAL -> Icons.TwoTone.Map
                                    MapType.SATELLITE -> Icons.TwoTone.SatelliteAlt
                                    MapType.TERRAIN -> Icons.TwoTone.Terrain
                                    else -> Icons.TwoTone.Satellite
                                },
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        leadingIcon = {
                            if (selectedMapType == mapType) {
                                Icon(
                                    imageVector = Icons.TwoTone.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}