package com.rafael.inclusimap.core.domain.model.util

import androidx.compose.ui.graphics.Color
import com.google.android.gms.maps.model.BitmapDescriptorFactory

fun Float.toColor(): Color = when (this) {
    in 1f..1.666f -> Color.Red
    in 1.666f..2.333f -> Color.Yellow
    in 2.333f..3f -> Color.Green
    else -> Color.Gray
}

fun Float.toMessage(): String = when (this) {
    in 1f..1.666f -> "Sem Acessibilidade"
    in 1.666f..2.333f -> "Acessibilidade\nModerada"
    in 2.333f..3f -> "Local Acessível"
    else -> "Sem dados de\nacessibilidade"
}

fun Float.toHUE(): Float = when (this) {
    in 1f..1.666f -> BitmapDescriptorFactory.HUE_RED
    in 1.666f..2.333f -> BitmapDescriptorFactory.HUE_YELLOW
    in 2.333f..3f -> BitmapDescriptorFactory.HUE_GREEN
    else -> 195f
}

fun String.extractUserEmail(): String? = try {
    this.split("_")[1].split("-")[0]
} catch (e: Exception) {
    null
}
