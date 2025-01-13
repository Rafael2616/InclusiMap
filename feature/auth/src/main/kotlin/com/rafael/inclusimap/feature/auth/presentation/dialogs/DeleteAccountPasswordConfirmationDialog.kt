package com.rafael.inclusimap.feature.auth.presentation.dialogs

import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Password
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.rafael.inclusimap.feature.auth.domain.model.LoginState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteAccountPasswordConfirmationDialog(
    loginState: LoginState,
    onDismissRequest: () -> Unit,
    onPasswordConfirmed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val orientation = LocalConfiguration.current.orientation
    val isLandscape = orientation == Configuration.ORIENTATION_LANDSCAPE
    var insertedPassword by remember { mutableStateOf("") }
    var isPasswordVerified by remember { mutableStateOf(false) }

    BasicAlertDialog(
        onDismissRequest = {
                onDismissRequest()
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
        ),
        modifier = modifier.fillMaxWidth(if (isLandscape) 0.5f else 0.7f),
    ) {
        Card(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp),
            ),
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    Icons.Outlined.Password,
                    contentDescription = "Password",
                    modifier = Modifier
                        .size(45.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = "Confirmar senha",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleLarge,
                )
                TextField(
                    value = insertedPassword,
                    onValueChange = {
                        isPasswordVerified = false
                        insertedPassword = it
                    },
                    isError = isPasswordVerified && insertedPassword != loginState.user?.password,
                    shape = RoundedCornerShape(12.dp, 12.dp, 0.dp, 0.dp),
                )
                Spacer(modifier = Modifier.height(30.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                        OutlinedButton(
                            onClick = {
                                onDismissRequest()
                            },
                        ) {
                            Text(text = "Voltar")
                        }
                        OutlinedButton(
                            onClick = {
                                isPasswordVerified = true
                                if (insertedPassword == loginState.user?.password) {
                                    onPasswordConfirmed()
                                }
                            },
                            colors = ButtonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError,
                                disabledContainerColor = MaterialTheme.colorScheme.errorContainer,
                                disabledContentColor = MaterialTheme.colorScheme.onErrorContainer,
                            ),
                        ) {
                            Text(text = "Confirmar e excluir")
                        }
                    }
                }
        }
    }

    val context = LocalContext.current
    DisposableEffect(isPasswordVerified, insertedPassword) {
        if (isPasswordVerified && insertedPassword != loginState.user?.password)
        Toast.makeText(context, "A senha está incorreta!", Toast.LENGTH_LONG).show()
        onDispose { }
    }
}
