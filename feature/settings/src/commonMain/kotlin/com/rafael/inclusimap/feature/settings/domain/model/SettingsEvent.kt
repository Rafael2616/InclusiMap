package com.rafael.inclusimap.feature.settings.domain.model

import com.rafael.libs.maps.interop.model.MapType

sealed interface SettingsEvent {
    data object ToggleIsDarkThemeOn : SettingsEvent

    data class ToggleIsFollowingSystemOn(
        val isSystemInDarkTheme: Boolean,
    ) : SettingsEvent

    data object ToggleIsDynamicColorsOn : SettingsEvent

    data object ToggleSearchHistoryEnabled : SettingsEvent

    data class SetIsDarkThemeOn(
        val value: Boolean,
    ) : SettingsEvent

    data class SetMapType(
        val type: MapType,
    ) : SettingsEvent

    data class ShowAboutAppCard(
        val value: Boolean,
    ) : SettingsEvent

    data class ShowLogoutDialog(
        val value: Boolean,
    ) : SettingsEvent

    data class ShowDeleteAccountDialog(
        val value: Boolean,
    ) : SettingsEvent

    data class OpenTermsAndConditions(
        val value: Boolean,
    ) : SettingsEvent

    data class ShowProfilePictureSettings(
        val value: Boolean,
    ) : SettingsEvent

    data class SetIsProfileSettingsTipShown(
        val value: Boolean,
    ) : SettingsEvent
}
