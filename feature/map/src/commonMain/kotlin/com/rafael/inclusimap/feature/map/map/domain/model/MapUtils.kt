package com.rafael.inclusimap.feature.map.map.domain.model

import androidx.compose.ui.graphics.Color
import com.rafael.libs.maps.interop.model.MapBitmapDescriptorFactory

val NORTH_RANGE = 0f..1f
val NORTH_RANGE2 = 359f..360f
val TILT_RANGE = 0f..1f

internal fun Float.inNorthRange() = this in NORTH_RANGE || this in NORTH_RANGE2

internal fun greenColor(isDarkThemeOn: Boolean) = if (isDarkThemeOn) Color.Green else Color(0xFF007373)

internal fun Float.toMessage(): String = when (this) {
    in 1f..1.666f -> "Sem Acessibilidade"
    in 1.666f..2.333f -> "Acessibilidade\nModerada"
    in 2.333f..3f -> "Local Acessível"
    else -> "Sem dados de\nacessibilidade"
}

internal fun Float.toHUE(): Float = when (this) {
    in 1f..1.666f -> MapBitmapDescriptorFactory.HUE_RED.value
    in 1.666f..2.333f -> MapBitmapDescriptorFactory.HUE_YELLOW.value
    in 2.333f..3f -> MapBitmapDescriptorFactory.HUE_GREEN.value
    else -> 195f
}
