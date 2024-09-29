package com.rafael.inclusimap.settings.domain.model

import androidx.compose.runtime.Stable

@Stable
data class SettingsState(
    val isDarkThemeOn: Boolean = true,
    val isDynamicColorsOn: Boolean = true,
    val isFollowingSystemOn: Boolean = true,
    val isAboutShown: Boolean = false,
    val appVersion: String = "1.0",
)
