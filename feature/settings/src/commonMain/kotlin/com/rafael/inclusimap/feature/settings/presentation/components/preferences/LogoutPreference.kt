package com.rafael.inclusimap.feature.settings.presentation.components.preferences

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.twotone.Logout
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.rafael.inclusimap.feature.settings.domain.model.SettingsEvent
import com.rafael.inclusimap.feature.settings.presentation.components.templates.IconPreference

@Composable
fun LogoutPreference(
    onEvent: (SettingsEvent) -> Unit,
) {
    IconPreference(
        title = "Desconectar",
        leadingIcon = Icons.AutoMirrored.TwoTone.Logout,
        trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
        leadingIconColor = MaterialTheme.colorScheme.error,
        onClick = {
            onEvent(SettingsEvent.ShowLogoutDialog(true))
        },
    )
}
