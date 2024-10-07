package com.rafael.inclusimap.feature.map.presentation.dialog

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlin.system.exitProcess

@Composable
fun PlacesNotLoadedDialog(
    onRetry: () -> Unit,
) {
    AlertDialog(
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
                onClick = {
                    exitProcess(0)
                },
            ) {
                Text(
                    text = "Sair do app",
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
            Text(text = "Erro!")
        },
        text = {
            Text(
                text = "Falha ao carregar os locais. Verifique sua conexão com a internet e tente novamente",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp),
    )
}
