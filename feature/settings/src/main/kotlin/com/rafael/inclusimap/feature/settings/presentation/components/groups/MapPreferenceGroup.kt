package com.rafael.inclusimap.feature.settings.presentation.components.groups

import androidx.compose.runtime.Composable
import com.rafael.inclusimap.core.settings.domain.model.SettingsEvent
import com.rafael.inclusimap.core.settings.domain.model.SettingsState
import com.rafael.inclusimap.feature.settings.presentation.components.preferences.MapTypePreference
import com.rafael.inclusimap.feature.settings.presentation.components.preferences.SearchHistoryEnabledPreference
import com.rafael.inclusimap.feature.settings.presentation.components.templates.PreferenceGroup

@Composable
fun MapPreferenceGroup(
    onEvent: (SettingsEvent) -> Unit,
    state: SettingsState,
) {
    PreferenceGroup(
        heading = "Mapa",
    ) {
        MapTypePreference(onEvent, state)
        SearchHistoryEnabledPreference(onEvent, state)
    }
}
