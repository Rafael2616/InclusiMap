package com.rafael.inclusimap.feature.settings.presentation.components.preferences

import androidx.compose.runtime.Composable
import com.rafael.inclusimap.feature.settings.domain.model.SettingsEvent
import com.rafael.inclusimap.feature.settings.domain.model.SettingsState
import com.rafael.inclusimap.feature.settings.presentation.components.templates.MultiSelectionPreference
import com.rafael.libs.maps.interop.model.GoogleMapType

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
