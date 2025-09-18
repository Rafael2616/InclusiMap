package com.rafael.inclusimap.feature.settings.presentation.components.preferences

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Password
import androidx.compose.runtime.Composable
import com.rafael.inclusimap.feature.settings.presentation.components.templates.IconPreference

@Composable
fun UpdatePasswordPreference(
    onGoToLoginScreen: (Boolean) -> Unit,
) {
    IconPreference(
        title = "Alterar senha",
        leadingIcon = Icons.Default.Password,
        trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
        onClick = { onGoToLoginScreen(true) },
    )
}
