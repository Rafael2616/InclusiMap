package com.rafael.inclusimap.feature.map.map.presentation.dialog

import android.content.res.Configuration
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SignalWifiStatusbarConnectedNoInternet4
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties

@Composable
fun PlacesNotUpdatedDialog(
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
) {
    val orientation = LocalConfiguration.current.orientation
    val isLandscape = orientation == Configuration.ORIENTATION_LANDSCAPE

    AlertDialog(
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
        ),
        onDismissRequest = { },
        icon = {
            Icon(
                imageVector = Icons.Outlined.SignalWifiStatusbarConnectedNoInternet4,
                contentDescription = "Close",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(30.dp),
            )
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
            ) {
                Text(
                    text = "Continuar offline",
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onRetry) {
                Text("Tentar novamente")
            }
        },
        title = {
            Text(text = "Aviso")
        },
        text = {
            Text(
                text = "Falha ao atualizar os locais. Verifique sua conexão com a internet e tente novamente ou navegue offline",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp),
        modifier = Modifier.fillMaxWidth(if (isLandscape) 0.5f else 0.8f),
    )
}
