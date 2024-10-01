package com.rafael.inclusimap.feature.settings.presentation.components.preferences

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.Code
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.rafael.inclusimap.core.navigation.Destination
import com.rafael.inclusimap.feature.settings.presentation.components.templates.IconPreference

@Composable
fun OpenSourceLicensesPreference(
    navController: NavController,
) {
    IconPreference(
        title = "Licenças de código aberto",
        leadingIcon = Icons.Outlined.Code,
        trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
        onClick = { navController.navigate(Destination.LibraryScreen) },
    )
}
