package com.rafael.inclusimap.feature.settings.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import com.rafael.inclusimap.core.domain.model.DeleteProcess
import com.rafael.inclusimap.core.settings.domain.model.SettingsEvent
import com.rafael.inclusimap.core.settings.domain.model.SettingsState
import com.rafael.inclusimap.feature.intro.presentation.dialogs.AppIntroDialog
import com.rafael.inclusimap.feature.settings.presentation.components.Preferences
import com.rafael.inclusimap.feature.settings.presentation.components.SettingsTopBar

@Composable
fun SettingsScreen(
    isLoginOut: Boolean,
    navController: NavController,
    state: SettingsState,
    onEvent: (SettingsEvent) -> Unit,
    onLogout: () -> Unit,
    onDeleteAccount: (Boolean) -> Unit,
    isDeleting: Boolean,
    deleteStep: DeleteProcess,
) {
    val latestOnEvent by rememberUpdatedState(onEvent)
    var showAppIntro by remember { mutableStateOf(false) }

    SettingsTopBar(
        navController,
        state,
        latestOnEvent,
    ) { innerPadding ->
        Preferences(
            innerPadding,
            state,
            latestOnEvent,
            onAppIntroEvent = {
                showAppIntro = it
            },
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

    if (state.showDeleteAccountDialog) {
        DeleteAccountConfirmationDialog(
            isDeleting = isDeleting,
            deleteStep = deleteStep,
            isLoginOut = isLoginOut,
            onDeleteAccount = { keepContributions ->
               onDeleteAccount(keepContributions)
            },
            onDismissRequest = {
                latestOnEvent(SettingsEvent.ShowDeleteAccountDialog(false))
            },
        )
    }

    if (showAppIntro) {
        AppIntroDialog(
            onDismiss = {
                showAppIntro = false
            },
        )
    }
}
