package com.rafael.inclusimap.feature.settings.presentation.components.preferences

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.runtime.Composable
import com.rafael.inclusimap.core.settings.domain.model.SettingsEvent
import com.rafael.inclusimap.core.settings.domain.model.SettingsState
import com.rafael.inclusimap.feature.settings.presentation.components.templates.SwitchPreference

@Composable
fun SearchHistoryEnabledPreference(
    onEvent: (SettingsEvent) -> Unit,
    state: SettingsState,
) {
    SwitchPreference(
        title = "Histórico de pesquisa",
        leadingIcon = Icons.Outlined.History,
        isChecked = state.searchHistoryEnabled,
        onCheckedChange = {
            onEvent(SettingsEvent.ToggleSearchHistoryEnabled)
        },
    )
}
