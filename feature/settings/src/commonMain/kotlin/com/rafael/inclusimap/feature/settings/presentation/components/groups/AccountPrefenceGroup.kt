package com.rafael.inclusimap.feature.settings.presentation.components.groups

import androidx.compose.runtime.Composable
import com.rafael.inclusimap.feature.settings.domain.model.SettingsEvent
import com.rafael.inclusimap.feature.settings.presentation.components.preferences.DeleteAccountPreference
import com.rafael.inclusimap.feature.settings.presentation.components.preferences.LogoutPreference
import com.rafael.inclusimap.feature.settings.presentation.components.preferences.UpdatePasswordPreference
import com.rafael.inclusimap.feature.settings.presentation.components.templates.PreferenceGroup

@Composable
fun AccountPreferenceGroup(
    onEvent: (SettingsEvent) -> Unit,
    onGoToLoginScreen: (Boolean) -> Unit,
) {
    PreferenceGroup(
        heading = "Conta",
    ) {
        UpdatePasswordPreference(onGoToLoginScreen)
        LogoutPreference(onEvent)
        DeleteAccountPreference(onEvent)
    }
}
