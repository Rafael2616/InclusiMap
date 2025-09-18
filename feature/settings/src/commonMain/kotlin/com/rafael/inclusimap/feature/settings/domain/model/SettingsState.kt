package com.rafael.inclusimap.feature.settings.domain.model

import androidx.compose.runtime.Stable
import com.rafael.libs.maps.interop.model.MapType

@Stable
data class SettingsState(
    val isDarkThemeOn: Boolean = true,
    val isDynamicColorsOn: Boolean = true,
    val isFollowingSystemOn: Boolean = true,
    val isAboutShown: Boolean = false,
    val mapType: MapType = MapType.NORMAL,
    val showLogoutDialog: Boolean = false,
    val showDeleteAccountDialog: Boolean = false,
    val showTermsAndConditions: Boolean = false,
    val showProfilePictureSettings: Boolean = false,
    val appVersion: String = "1.0",
    val searchHistoryEnabled: Boolean = true,
    val isProfileSettingsTipShown: Boolean = false,
)
