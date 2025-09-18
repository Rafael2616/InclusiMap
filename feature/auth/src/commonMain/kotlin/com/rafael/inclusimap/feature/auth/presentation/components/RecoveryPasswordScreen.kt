package com.rafael.inclusimap.feature.auth.presentation.components

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
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rafael.inclusimap.feature.auth.domain.model.LoginState
import com.rafael.inclusimap.feature.auth.domain.utils.isValidEmail
import com.rafael.inclusimap.feature.auth.presentation.viewmodel.formatInMinutes
import kotlinx.coroutines.launch

@Composable
fun RecoveryPasswordScreen(
    state: LoginState,
    onCancel: () -> Unit,
    onSendRecoverEmail: (String) -> Unit,
    onValidateToken: (String) -> Unit,
    onResetProcess: () -> Unit,
    snackBarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    var email by remember { mutableStateOf(state.user?.email.orEmpty()) }
    var receivedToken by remember { mutableStateOf("") }
    var canUpdate by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    var isValidEmail by remember { mutableStateOf(true) }
    val firstItemShape = RoundedCornerShape(12.dp, 12.dp, 0.dp, 0.dp)
    val lastItemShape = RoundedCornerShape(0.dp, 0.dp, 12.dp, 12.dp)
    val snackBarScope = rememberCoroutineScope()

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
                    isValidEmail = it.isValidEmail()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(firstItemShape),
                placeholder = {
                    Text(text = "E-mail")
                },
                isError = canUpdate && email.isEmpty() || !isValidEmail,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next,
                ),
                enabled = if (state.isLoggedIn) false else !state.isEmailSent,
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
                        .clip(lastItemShape),
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
                    snackBarScope.launch {
                        if (email.isEmpty()) {
                            snackBarHostState.currentSnackbarData?.dismiss()
                            snackBarHostState.showSnackbar("Preencha todos os campos")
                            return@launch
                        }
                        if (!isValidEmail) {
                            snackBarHostState.currentSnackbarData?.dismiss()
                            snackBarHostState.showSnackbar("O email é inválido")
                            return@launch
                        }
                        if (state.isEmailSent) {
                            onValidateToken(receivedToken)
                        } else {
                            onSendRecoverEmail(email)
                        }
                    }
                },
                enabled = if (state.isEmailSent) !state.isValidatingToken else !state.isSendingEmail,
            ) {
                Text(text = if (state.isEmailSent) "Validar" else "Enviar")
            }
        }
    }

    DisposableEffect(state.isTokenValidated, state.isTokenValid) {
        if (!state.isTokenValid && state.isTokenValidated && receivedToken.isNotEmpty()) {
            snackBarScope.launch {
                snackBarHostState.currentSnackbarData?.dismiss()
                snackBarHostState.showSnackbar("Token inválido")
            }
        }
        onDispose { }
    }

    DisposableEffect(state.isEmailSent) {
        canUpdate = false
        onDispose { }
    }

    val latestOnResetProcess by rememberUpdatedState(onResetProcess)
    DisposableEffect(state.tokenExpirationTimer == 0L) {
        latestOnResetProcess()
        onDispose { }
    }

    DisposableEffect(state.userExists) {
        if (!state.userExists && email.isNotEmpty()) {
            snackBarScope.launch {
                snackBarHostState.currentSnackbarData?.dismiss()
                snackBarHostState.showSnackbar("Não existe um usuário cadastrado com esse email")
            }
        }
        onDispose { }
    }
}
