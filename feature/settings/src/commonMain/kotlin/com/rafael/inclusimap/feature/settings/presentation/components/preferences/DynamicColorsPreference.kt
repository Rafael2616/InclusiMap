package com.rafael.inclusimap.feature.settings.presentation.components.preferences

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.runtime.Composable
import com.rafael.inclusimap.feature.settings.domain.model.SettingsEvent
import com.rafael.inclusimap.feature.settings.domain.model.SettingsState
import com.rafael.inclusimap.feature.settings.presentation.components.templates.SwitchPreference

@Composable
fun DynamicColorsPreference(
    onEvent: (SettingsEvent) -> Unit,
    state: SettingsState,
) {
    SwitchPreference(
        title = "Cores dinâmicas",
        leadingIcon = Icons.Outlined.Palette,
        isChecked = state.isDynamicColorsOn,
        onCheckedChange = { onEvent(SettingsEvent.ToggleIsDynamicColorsOn) },
    )
}
