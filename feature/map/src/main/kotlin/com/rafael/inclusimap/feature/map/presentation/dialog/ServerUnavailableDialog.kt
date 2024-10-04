package com.rafael.inclusimap.feature.map.presentation.dialog

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NoCell
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlin.system.exitProcess

@Composable
fun ServerUnavailableDialog(
    onRetry: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { },
        icon = {
            Icon(
                imageVector = Icons.Outlined.NoCell,
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
            Text(text = "Erro no servidor!")
        },
        text = {
            Text(
                text = "Falha ao conectar ao servidor! Este pode ser um problema temporário, considere tentar novamente",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
        },
    )
}
