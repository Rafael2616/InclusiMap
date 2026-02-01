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
import com.rafael.inclusimap.feature.settings.domain.model.SettingsEvent
import com.rafael.inclusimap.feature.settings.domain.model.SettingsState
import com.rafael.inclusimap.feature.settings.presentation.components.Preferences
import com.rafael.inclusimap.feature.settings.presentation.components.SettingsTopBar
import com.svenjacobs.reveal.RevealCanvasState

@Composable
fun SettingsScreen(
    isLoggedIn: Boolean,
    state: SettingsState,
    showFirstTimeAnimation: Boolean?,
    revealCanvasState: RevealCanvasState,
    onEvent: (SettingsEvent) -> Unit,
    userProfilePicture: ByteArray?,
    onNavigateBack: () -> Unit,
    onGoToLoginScreen: (Boolean) -> Unit,
    onGoToAboutAppScreen: () -> Unit,
    appIntroDialog: @Composable (onDismiss: () -> Unit) -> Unit,
    clearBackStack: () -> Unit,
) {
    val latestOnEvent by rememberUpdatedState(onEvent)
    val latestClearBackStack by rememberUpdatedState(clearBackStack)
    var showAppIntro by remember { mutableStateOf(false) }

    SettingsTopBar(
        onNavigateBack,
        userProfilePicture,
        state,
        latestOnEvent,
        revealCanvasState,
        showFirstTimeAnimation,
    ) { innerPadding ->
        Preferences(
            innerPadding,
            state,
            latestOnEvent,
            onAppIntroEvent = {
                showAppIntro = it
            },
            onGoToLoginScreen,
            onGoToAboutAppScreen,
        )
    }

    AnimatedVisibility(showAppIntro) {
        appIntroDialog({
            showAppIntro = false
        })
    }

    LaunchedEffect(isLoggedIn) {
        if (!isLoggedIn) {
            latestOnEvent(SettingsEvent.ShowLogoutDialog(false))
            latestOnEvent(SettingsEvent.ShowDeleteAccountDialog(false))
            latestOnEvent(SettingsEvent.SetIsProfileSettingsTipShown(false))
            latestClearBackStack()
        }
    }
}
