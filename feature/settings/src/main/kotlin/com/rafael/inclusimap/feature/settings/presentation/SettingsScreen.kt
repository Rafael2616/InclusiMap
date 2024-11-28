package com.rafael.inclusimap.feature.settings.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.navigation.NavController
import com.rafael.inclusimap.core.navigation.Destination
import com.rafael.inclusimap.core.settings.domain.model.SettingsEvent
import com.rafael.inclusimap.core.settings.domain.model.SettingsState
import com.rafael.inclusimap.feature.intro.presentation.dialogs.AppIntroDialog
import com.rafael.inclusimap.feature.settings.presentation.components.Preferences
import com.rafael.inclusimap.feature.settings.presentation.components.SettingsTopBar
import com.svenjacobs.reveal.RevealCanvasState

@Composable
fun SettingsScreen(
    navController: NavController,
    isLoggedIn: Boolean,
    state: SettingsState,
    revealCanvasState: RevealCanvasState,
    onEvent: (SettingsEvent) -> Unit,
    userProfilePicture: ImageBitmap?,
) {
    val latestOnEvent by rememberUpdatedState(onEvent)
    var showAppIntro by remember { mutableStateOf(false) }

    SettingsTopBar(
        navController,
        userProfilePicture,
        state,
        latestOnEvent,
        revealCanvasState,
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

    AnimatedVisibility(showAppIntro) {
        AppIntroDialog(
            onDismiss = {
                showAppIntro = false
            },
        )
    }

    LaunchedEffect(isLoggedIn) {
        if (!isLoggedIn) {
            onEvent(SettingsEvent.ShowLogoutDialog(false))
            onEvent(SettingsEvent.ShowDeleteAccountDialog(false))
            onEvent(SettingsEvent.SetIsProfileSettingsTipShown(false))
            navController.clearBackStack(Destination.MapHost)
        }
    }
}
