package com.rafael.inclusimap.feature.settings.presentation.components.groups

import androidx.compose.runtime.Composable
import com.rafael.inclusimap.core.settings.domain.model.SettingsEvent
import com.rafael.inclusimap.core.settings.domain.model.SettingsState
import com.rafael.inclusimap.feature.settings.presentation.components.preferences.DarkThemePreference
import com.rafael.inclusimap.feature.settings.presentation.components.preferences.DynamicColorsPreference
import com.rafael.inclusimap.feature.settings.presentation.components.preferences.FollowSystemPreference
import com.rafael.inclusimap.feature.settings.presentation.components.templates.PreferenceGroup

@Composable
fun ThemePreferenceGroup(
    onEvent: (SettingsEvent) -> Unit,
    state: SettingsState,
) {
    PreferenceGroup(heading = "Tema") {
        FollowSystemPreference(onEvent, state)
        DarkThemePreference(onEvent, state)
        DynamicColorsPreference(onEvent, state)
    }
}
