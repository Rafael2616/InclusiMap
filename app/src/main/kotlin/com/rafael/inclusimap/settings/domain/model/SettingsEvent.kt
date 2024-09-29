package com.rafael.inclusimap.settings.domain.model

sealed interface SettingsEvent {
    data object ToggleIsDarkThemeOn : SettingsEvent
    data class ToggleIsFollowingSystemOn(val isSystemInDarkTheme: Boolean) : SettingsEvent
    data object ToggleIsDynamicColorsOn : SettingsEvent
    data class SetIsDarkThemeOn(val value: Boolean) : SettingsEvent
    data class ShowAboutAppCard(val value: Boolean) : SettingsEvent
}
