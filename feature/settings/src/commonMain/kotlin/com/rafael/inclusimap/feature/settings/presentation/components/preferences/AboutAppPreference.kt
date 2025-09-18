package com.rafael.inclusimap.feature.settings.presentation.components.preferences

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.Info
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.rafael.inclusimap.feature.settings.presentation.components.templates.IconPreference

@Composable
fun AboutAppPreference(
    onGoToAboutAppScreen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconPreference(
        title = "Sobre o InclusiMap©",
        modifier = modifier,
        description = null,
        leadingIcon = Icons.Outlined.Info,
        trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
        onClick = onGoToAboutAppScreen,
    )
}
