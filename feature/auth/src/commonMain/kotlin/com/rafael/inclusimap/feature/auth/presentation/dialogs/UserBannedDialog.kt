package com.rafael.inclusimap.feature.auth.presentation.dialogs

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NoAccounts
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
import com.rafael.inclusimap.core.util.exitProcess

@Composable
fun UserBannedDialog(onLogin: () -> Unit) {
    AlertDialog(
        onDismissRequest = { },
        icon = {
            Icon(
                imageVector = Icons.Outlined.NoAccounts,
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
                onClick = onLogin,
            ) {
                Text(text = "Fazer login")
            }
        },
        title = {
            Text(text = "Aviso de banimento")
        },
        text = {
            Text(
                text = "Sua conta foi permanentemente banida de usar o aplicativo InclusiMap\nIsso ocorre devido a violação de uma ou mais condições dos termos de uso do aplicativo",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp),
    )
}
