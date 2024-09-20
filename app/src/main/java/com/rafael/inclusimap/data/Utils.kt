package com.rafael.inclusimap.data

import androidx.compose.ui.graphics.Color
import com.google.android.gms.maps.model.BitmapDescriptorFactory

fun Float.toColor(): Color = when (this) {
    in 0f..1f -> Color.Red
    in 1f..2f -> Color.Yellow
    in 2f..3f -> Color.Green
    else -> throw IllegalArgumentException("Invalid value for float: $this")
}

fun Float.toMessage(): String = when (this) {
    in 0f..1f -> "Sem Acessibilidade"
    in 1f..2f -> "Acessibilidade\nModerada"
    in 2f..3f -> "Local Acessível"
    else -> throw IllegalArgumentException("Invalid value for float: $this")
}

fun Float.toHUE(): Float = when (this) {
    in 0f..1f -> BitmapDescriptorFactory.HUE_RED
    in 1f..2f -> BitmapDescriptorFactory.HUE_YELLOW
    in 2f..3f -> BitmapDescriptorFactory.HUE_GREEN
    else -> throw IllegalArgumentException("Invalid value for float: $this")

}