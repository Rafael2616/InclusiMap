package com.rafael.inclusimap.feature.map.map.domain

val NORTH_RANGE = 0f..1f
val NORTH_RANGE2 = 359f..360f
val TILT_RANGE = 0f..1f

internal fun Float.inNorthRange(): Boolean = this in NORTH_RANGE || this in NORTH_RANGE2
