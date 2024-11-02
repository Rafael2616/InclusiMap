package com.rafael.inclusimap.feature.auth.presentation.components

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rafael.inclusimap.feature.auth.domain.model.LoginState
import com.rafael.inclusimap.feature.auth.domain.utils.isValidEmail
import com.rafael.inclusimap.feature.auth.presentation.viewmodel.formatInMinutes

@Composable
fun RecoveryPasswordScreen(
    state: LoginState,
    onCancel: () -> Unit,
    onSendRecoverEmail: (String) -> Unit,
    onValidateToken: (String) -> Unit,
    onResetProcess: () -> Unit,
    modifier: Modifier = Modifier,
    defaultRoundedShape: Shape = RoundedCornerShape(12.dp, 12.dp, 0.dp, 0.dp),
) {
    var email by remember { mutableStateOf(state.user?.email.orEmpty()) }
    var receivedToken by remember { mutableStateOf("") }
    var canUpdate by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val toast = Toast.makeText(context, "Preencha todos os campos", Toast.LENGTH_SHORT)
    var showPassword by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    var isValidEmail by remember { mutableStateOf(true) }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        modifier = modifier,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
            modifier = Modifier
                .fillMaxWidth(),
        ) {
            Text(
                text = "Alterar senha",
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(4.dp))
            TextField(
                value = email,
                onValueChange = {
                    email = it
                    canUpdate = false
                    isValidEmail = isValidEmail(it)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(defaultRoundedShape),
                placeholder = {
                    Text(text = "Email")
                },
                isError = canUpdate && email.isEmpty() || !isValidEmail,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next,
                ),
                trailingIcon = {
                    IconButton(
                        onClick = {
                            showPassword = !showPassword
                        },
                    ) {
                        Icon(
                            imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (showPassword) "Hide password" else "Show password",
                        )
                    }
                },
                enabled = !state.isUpdatingPassword && !state.isEmailSent || !state.isLoggedIn,
            )
            if (state.isEmailSent) {
                TextField(
                    value = receivedToken,
                    onValueChange = {
                        receivedToken = it
                        canUpdate = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(defaultRoundedShape),
                    placeholder = {
                        Text(text = "Código de verificação")
                    },
                    isError = !state.isTokenValid && state.isTokenValidated && receivedToken.isNotEmpty() || canUpdate && receivedToken.isEmpty(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                        },
                    ),
                    enabled = !state.isUpdatingPassword,
                )
            }
            Text(
                text = if (state.isEmailSent) "Expira em: ${state.tokenExpirationTimer?.formatInMinutes()}" else "Um email será enviado para essa conta com um código de validação",
                fontSize = 12.sp,
                lineHeight = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier
                    .fillMaxWidth(),
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (state.isSendingEmail || state.isValidatingToken) {
                CircularProgressIndicator(
                    strokeCap = StrokeCap.Round,
                    modifier = Modifier.size(35.dp),
                )
            } else {
                OutlinedButton(
                    onClick = {
                        onCancel()
                    },
                ) {
                    Text(text = "Cancelar")
                }
            }
            Button(
                onClick = {
                    canUpdate = true
                    if (email.isEmpty()) {
                        toast.show()
                        return@Button
                    }
                    if (!isValidEmail) {
                        Toast.makeText(context, "O email é inválida", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (state.isEmailSent) {
                        onValidateToken(receivedToken)
                    } else {
                        onSendRecoverEmail(email)
                    }
                },
                enabled = !state.isUpdatingPassword,
            ) {
                Text(text = if (state.isEmailSent) "Validar" else "Enviar")
            }
        }
    }

    DisposableEffect(state.isTokenValidated, state.isTokenValid) {
       if(!state.isTokenValid && state.isTokenValidated && receivedToken.isNotEmpty()) {
        Toast.makeText(context, "Token incorreto ou expirado! Tente novamente", Toast.LENGTH_LONG)
            .show()
       }
        if (state.isTokenValid && state.isTokenValidated && receivedToken.isNotEmpty()) {
            Toast.makeText(context, "Token verificado!", Toast.LENGTH_LONG).show()
        }
        onDispose { }
    }

    DisposableEffect(state.isEmailSent) {
        canUpdate = false
        onDispose { }
    }

    DisposableEffect(state.tokenExpirationTimer == 0L) {
        onResetProcess()
        onDispose {  }
    }
}
