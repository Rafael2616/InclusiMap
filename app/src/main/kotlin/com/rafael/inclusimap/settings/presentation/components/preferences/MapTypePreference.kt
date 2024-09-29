package com.rafael.inclusimap.settings.presentation.components.preferences

import androidx.compose.runtime.Composable
import com.rafael.inclusimap.domain.GoogleMapType
import com.rafael.inclusimap.settings.domain.model.SettingsEvent
import com.rafael.inclusimap.settings.domain.model.SettingsState
import com.rafael.inclusimap.settings.presentation.components.templates.MultiSelectionPreference

@Composable
fun MapTypePreference(
    onEvent: (SettingsEvent) -> Unit,
    state: SettingsState,
) {
    MultiSelectionPreference(
        selections = GoogleMapType.getMapTypes(),
        selected = state.mapType,
        onSelectionChange = { onEvent(SettingsEvent.SetMapType(it)) },
    )
}
