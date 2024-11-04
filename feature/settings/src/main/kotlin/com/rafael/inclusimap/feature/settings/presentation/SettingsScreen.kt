package com.rafael.inclusimap.feature.settings.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.navigation.NavController
import com.rafael.inclusimap.core.settings.domain.model.SettingsEvent
import com.rafael.inclusimap.core.settings.domain.model.SettingsState
import com.rafael.inclusimap.feature.intro.presentation.dialogs.AppIntroDialog
import com.rafael.inclusimap.feature.intro.presentation.dialogs.TermsAndConditionsDialog
import com.rafael.inclusimap.feature.settings.presentation.components.Preferences
import com.rafael.inclusimap.feature.settings.presentation.components.SettingsTopBar

@Composable
fun SettingsScreen(
    navController: NavController,
    state: SettingsState,
    onEvent: (SettingsEvent) -> Unit,
    userName: String,
    userEmail: String,
    allowOtherUsersToSeeProfilePicture: Boolean,
    onEditUserName: (String) -> Unit,
    onAddEditProfilePicture: (ImageBitmap) -> Unit,
    onRemoveProfilePicture: () -> Unit,
    onAllowPictureOptedIn: (Boolean) -> Unit,
    isSuccessfulUpdatingUserInfos: Boolean,
    isErrorUpdatingUserInfos: Boolean,
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

    AnimatedVisibility(showAppIntro) {
        AppIntroDialog(
            onDismiss = {
                showAppIntro = false
            },
        )
    }
    AnimatedVisibility(state.showTermsAndConditions) {
        TermsAndConditionsDialog(
            onDismissRequest = {
                latestOnEvent(SettingsEvent.OpenTermsAndConditions(false))
            },
        )
    }
    AnimatedVisibility(state.showProfilePictureSettings) {
        ProfileSettingsDialog(
            onDismiss = {
                latestOnEvent(SettingsEvent.ShowProfilePictureSettings(false))
            },
            onAddUpdatePicture = {
                onAddEditProfilePicture(it)
            },
            onRemovePicture = {
                onRemoveProfilePicture()
            },
            userName = userName,
            userEmail = userEmail,
            allowOtherUsersToSeeProfilePicture = allowOtherUsersToSeeProfilePicture,
            onEditUserName = {
                onEditUserName(it)
            },
            onAllowPictureOptedIn = {
                onAllowPictureOptedIn(it)
            },
            isSuccessfulUpdatingUserInfos = isSuccessfulUpdatingUserInfos,
            isErrorUpdatingUserInfos = isErrorUpdatingUserInfos,
            state = state,
        )
    }
}
