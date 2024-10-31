package com.rafael.inclusimap.feature.map.map.domain

import androidx.compose.ui.graphics.Color

val NORTH_RANGE = 0f..1f
val NORTH_RANGE2 = 359f..360f
val TILT_RANGE = 0f..1f

internal fun Float.inNorthRange() = this in NORTH_RANGE || this in NORTH_RANGE2

internal fun greenColor(isDarkThemeOn: Boolean) = if (isDarkThemeOn) Color.Green else Color(0xFF007373)
