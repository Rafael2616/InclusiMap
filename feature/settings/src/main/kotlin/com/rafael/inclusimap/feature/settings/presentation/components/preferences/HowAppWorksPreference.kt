package com.rafael.inclusimap.feature.settings.presentation.components.preferences

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.runtime.Composable
import com.rafael.inclusimap.feature.settings.presentation.components.templates.IconPreference

@Composable
fun HowAppWorksPreference(
    onAppIntroEvent: (Boolean) -> Unit,
) {
    IconPreference(
        title = "Como o app funciona?",
        leadingIcon = Icons.AutoMirrored.Outlined.HelpOutline,
        trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
        onClick = { onAppIntroEvent(true) },
    )
}
