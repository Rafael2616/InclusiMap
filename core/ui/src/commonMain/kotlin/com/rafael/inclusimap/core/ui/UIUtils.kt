package com.rafael.inclusimap.core.ui

import androidx.compose.ui.Modifier

expect val isLandscape: Boolean

expect val isDynamicColorAvailable: Boolean

expect fun Modifier.imeNestedScrollCompat(): Modifier
