package com.rafael.inclusimap.settings.presentation.components.preferences

import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Password
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.rafael.inclusimap.navigation.Destination
import com.rafael.inclusimap.settings.presentation.components.templates.IconPreference

@Composable
fun UpdatePasswordPreference(
    navController: NavController,
) {
    val context = LocalContext.current
    IconPreference(
        title = "Atualizar senha",
        leadingIcon = Icons.Default.Password,
        trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
        onClick = { Toast.makeText(context, "Atualizar senha: Em breve...", Toast.LENGTH_SHORT).show() },
    )
}
