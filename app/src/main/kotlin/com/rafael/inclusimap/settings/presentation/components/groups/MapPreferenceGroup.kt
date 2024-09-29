package com.rafael.inclusimap.settings.presentation.components.groups

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.rafael.inclusimap.settings.domain.model.SettingsEvent
import com.rafael.inclusimap.settings.domain.model.SettingsState
import com.rafael.inclusimap.settings.presentation.components.preferences.MapTypePreference
import com.rafael.inclusimap.settings.presentation.components.templates.PreferenceGroup

@Composable
fun MapPreferenceGroup(
    onEvent: (SettingsEvent) -> Unit,
    state: SettingsState,
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    PreferenceGroup(
        heading = "Mapa",
        modifier = modifier.padding(bottom = 8.dp),
    ) {
        MapTypePreference(onEvent, state)
    }
}
