package com.rafael.inclusimap.feature.map.map.presentation.dialog

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DatasetLinked
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.rafael.inclusimap.core.util.exitProcess

@Composable
fun ServerUnavailableDialog(
    onRetry: () -> Unit,
    isRetrying: Boolean,
    isServerAvailable: Boolean,
    isInternetAvailable: Boolean,
    snackBarHostState: SnackbarHostState,
) {
    AlertDialog(
        onDismissRequest = { },
        icon = {
            Icon(
                imageVector = Icons.Outlined.DatasetLinked,
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
            TextButton(
                enabled = isInternetAvailable && !isRetrying,
                onClick = onRetry,
            ) {
                Text(text = if (isRetrying) "Reconectando..." else "Reconectar")
            }
        },
        title = {
            Text(text = "Servidor indisponível")
        },
        text = {
            Text(
                text = "Falha ao conectar ao serviço! Este pode ser um problema temporário, considere tentar reconectar",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp),
    )

    LaunchedEffect(isRetrying, isServerAvailable) {
        if (!isRetrying && !isServerAvailable) {
            snackBarHostState.currentSnackbarData?.dismiss()
            snackBarHostState.showSnackbar("Falha ao conectar ao serviço!")
        }
    }
}
