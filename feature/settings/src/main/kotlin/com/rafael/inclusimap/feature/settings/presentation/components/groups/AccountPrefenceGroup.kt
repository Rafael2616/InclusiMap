package com.rafael.inclusimap.feature.settings.presentation.components.groups

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.rafael.inclusimap.core.settings.domain.model.SettingsEvent
import com.rafael.inclusimap.feature.settings.presentation.components.preferences.DeleteAccountPreference
import com.rafael.inclusimap.feature.settings.presentation.components.preferences.LogoutPreference
import com.rafael.inclusimap.feature.settings.presentation.components.preferences.UpdatePasswordPreference
import com.rafael.inclusimap.feature.settings.presentation.components.templates.PreferenceGroup

@Composable
fun AccountPreferenceGroup(
    onEvent: (SettingsEvent) -> Unit,
    navController: NavController,
) {
    PreferenceGroup(
        heading = "Conta",
    ) {
        UpdatePasswordPreference(navController)
        LogoutPreference(onEvent)
        DeleteAccountPreference(onEvent)
    }
}
