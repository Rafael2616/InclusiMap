package com.rafael.inclusimap.core.ui

import androidx.compose.ui.Modifier
import platform.UIKit.UIDevice
import platform.UIKit.UIDeviceOrientation

actual val isLandscape: Boolean
    get() = UIDevice.currentDevice.orientation == UIDeviceOrientation.UIDeviceOrientationLandscapeLeft ||
        UIDevice.currentDevice.orientation == UIDeviceOrientation.UIDeviceOrientationLandscapeRight

// iOS hasn't dynamic colors support
actual val isDynamicColorAvailable: Boolean = false

// iOS hasn't an api to this
actual fun Modifier.imeNestedScrollCompat(): Modifier = this
