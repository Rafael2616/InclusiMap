package com.rafael.inclusimap.feature.settings.presentation.components.preferences

import androidx.compose.runtime.Composable
import com.rafael.inclusimap.core.settings.domain.model.SettingsEvent
import com.rafael.inclusimap.core.settings.domain.model.SettingsState
import com.rafael.inclusimap.feature.map.domain.GoogleMapType
import com.rafael.inclusimap.feature.settings.presentation.components.templates.MultiSelectionPreference

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
