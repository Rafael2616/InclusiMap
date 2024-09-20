package com.rafael.inclusimap.data

import androidx.compose.ui.graphics.Color

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