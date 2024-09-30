package com.rafael.inclusimap.feature.settings.presentation.components.preferences

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BrightnessMedium
import androidx.compose.runtime.Composable
import com.rafael.inclusimap.core.settings.domain.model.SettingsEvent
import com.rafael.inclusimap.core.settings.domain.model.SettingsState
import com.rafael.inclusimap.feature.settings.presentation.components.templates.SwitchPreference

@Composable
fun FollowSystemPreference(
    onEvent: (SettingsEvent) -> Unit,
    state: SettingsState,
) {
    val isSystemInDarkTheme = isSystemInDarkTheme()
    SwitchPreference(
        title = "Siga o sistema",
        leadingIcon = Icons.Outlined.BrightnessMedium,
        isChecked = state.isFollowingSystemOn,
        onCheckedChange = { onEvent(SettingsEvent.ToggleIsFollowingSystemOn(isSystemInDarkTheme)) },
    )
}
