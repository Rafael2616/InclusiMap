package com.rafael.inclusimap.settings.presentation.components.preferences

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Password
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.rafael.inclusimap.navigation.Destination
import com.rafael.inclusimap.settings.presentation.components.templates.IconPreference

@Composable
fun UpdatePasswordPreference(
    navController: NavController,
) {
    IconPreference(
        title = "Atualizar senha",
        leadingIcon = Icons.Default.Password,
        trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
        onClick = { navController.navigate(Destination.LoginScreen( isEditPasswordMode = true)) },
    )
}
