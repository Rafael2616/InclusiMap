package com.rafael.libs.maps.interop.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Map
import androidx.compose.material.icons.twotone.Satellite
import androidx.compose.material.icons.twotone.SatelliteAlt
import androidx.compose.material.icons.twotone.Terrain
import androidx.compose.ui.graphics.vector.ImageVector

data class GoogleMapType(
    val type: MapType,
    val name: String,
    val icon: ImageVector,
) {
    companion object {
        fun getMapTypes() = listOf(
            GoogleMapType(
                type = MapType.NORMAL,
                name = "Normal",
                icon = Icons.TwoTone.Map,
            ),
            GoogleMapType(
                type = MapType.SATELLITE,
                name = "Satélite",
                icon = Icons.TwoTone.SatelliteAlt,
            ),
            GoogleMapType(
                type = MapType.TERRAIN,
                name = "Terreno",
                icon = Icons.TwoTone.Terrain,
            ),
            GoogleMapType(
                type = MapType.HYBRID,
                name = "Híbrido",
                icon = Icons.TwoTone.Satellite,
            ),
        )
    }
}
