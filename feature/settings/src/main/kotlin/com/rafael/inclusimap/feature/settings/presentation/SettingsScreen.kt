package com.rafael.inclusimap.feature.settings.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.navigation.NavController
import com.rafael.inclusimap.core.settings.domain.model.SettingsEvent
import com.rafael.inclusimap.feature.settings.presentation.components.Preferences
import com.rafael.inclusimap.feature.settings.presentation.components.SettingsTopBar

@Composable
fun SettingsScreen(
    isLoginOut: Boolean,
    navController: NavController,
    state: com.rafael.inclusimap.core.settings.domain.model.SettingsState,
    onEvent: (SettingsEvent) -> Unit,
    onLogout: () -> Unit,
) {
    val latestOnEvent by rememberUpdatedState(onEvent)

    SettingsTopBar(
        navController,
        state,
        latestOnEvent,
    ) { innerPadding ->
        Preferences(
            innerPadding,
            state,
            latestOnEvent,
            navController,
        )
    }
    if (state.showLogoutDialog) {
        LogoutConfirmationDialog(
            isLoginOut = isLoginOut,
            onDismissRequest = {
                latestOnEvent(SettingsEvent.ShowLogoutDialog(false))
            },
            onLogout = {
                onLogout()
            },
        )
    }
}
