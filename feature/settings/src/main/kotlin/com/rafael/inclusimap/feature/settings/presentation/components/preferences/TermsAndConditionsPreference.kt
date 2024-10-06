package com.rafael.inclusimap.feature.settings.presentation.components.preferences

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.Ballot
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.rafael.inclusimap.core.settings.domain.model.SettingsEvent
import com.rafael.inclusimap.feature.settings.presentation.components.templates.IconPreference

@Composable
fun TermsAndConditionsPreference(
    onEvent: (SettingsEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    IconPreference(
        title = "Termos e Condições",
        modifier = modifier,
        description = null,
        leadingIcon = Icons.Outlined.Ballot,
        trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
        onClick = {
            onEvent(SettingsEvent.OpenTermsAndConditions(true))
        },
    )
}
