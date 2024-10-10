package com.rafael.inclusimap.core.settings.domain.model

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.ImageBitmap
import com.google.maps.android.compose.MapType

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
    val profilePicture: ImageBitmap? = null,
    val showProfilePictureSettings: Boolean = false,
    val appVersion: String = "1.0",
)
