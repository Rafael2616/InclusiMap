package com.rafael.inclusimap.feature.settings.presentation.components.groups

import androidx.compose.runtime.Composable
import com.rafael.inclusimap.feature.settings.domain.model.SettingsEvent
import com.rafael.inclusimap.feature.settings.domain.model.SettingsState
import com.rafael.inclusimap.feature.settings.presentation.components.preferences.SearchHistoryEnabledPreference
import com.rafael.inclusimap.feature.settings.presentation.components.templates.PreferenceGroup

@Composable
fun MapSearchPreferenceGroup(
    onEvent: (SettingsEvent) -> Unit,
    state: SettingsState,
) {
    PreferenceGroup(
        heading = "Pesquisa",
    ) {
        SearchHistoryEnabledPreference(onEvent, state)
    }
}
