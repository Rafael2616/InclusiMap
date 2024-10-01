package com.rafael.inclusimap.feature.settings.presentation.components.preferences

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.NoAccounts
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.rafael.inclusimap.core.settings.domain.model.SettingsEvent
import com.rafael.inclusimap.feature.settings.presentation.components.templates.IconPreference

@Composable
fun DeleteAccountPreference(
    onEvent: (SettingsEvent) -> Unit,
) {
    IconPreference(
        title = "Excluir conta",
        leadingIcon = Icons.Outlined.NoAccounts,
        trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
        leadingIconColor = MaterialTheme.colorScheme.error,
        onClick = {
            onEvent(SettingsEvent.ShowDeleteAccountDialog(true))
        },
    )
}
