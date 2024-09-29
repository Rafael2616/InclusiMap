package com.rafael.inclusimap.settings.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.navigation.NavController
import com.rafael.inclusimap.domain.LoginState
import com.rafael.inclusimap.settings.domain.model.SettingsEvent
import com.rafael.inclusimap.settings.domain.model.SettingsState
import com.rafael.inclusimap.settings.presentation.components.Preferences
import com.rafael.tictactoe.feature.settings.presentation.components.SettingsTopBar

@Composable
fun SettingsScreen(
    navController: NavController,
    state: SettingsState,
    onEvent: (SettingsEvent) -> Unit,
    loginState: LoginState,
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
            loginState = loginState,
            onDismissRequest = {
                latestOnEvent(SettingsEvent.ShowLogoutDialog(false))
            },
            onLogout = {
                onLogout()
            }
        )
    }
}
