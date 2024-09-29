package com.rafael.inclusimap.settings.domain.model

import androidx.compose.runtime.Stable
import com.google.maps.android.compose.MapType

@Stable
data class SettingsState(
    val isDarkThemeOn: Boolean = true,
    val isDynamicColorsOn: Boolean = true,
    val isFollowingSystemOn: Boolean = true,
    val isAboutShown: Boolean = false,
    val mapType: MapType = MapType.NORMAL,
    val showLogoutDialog: Boolean = false,
    val appVersion: String = "1.0",
)
