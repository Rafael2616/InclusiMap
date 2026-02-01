package com.rafael.inclusimap.feature.auth.presentation.components

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rafael.inclusimap.feature.auth.domain.model.LoginState
import com.rafael.inclusimap.feature.auth.domain.model.RegisteredUser
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    state: LoginState,
    onGoToRegister: () -> Unit,
    onGoToRecover: () -> Unit,
    onLogin: (RegisteredUser) -> Unit,
    snackBarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    var password by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var canLogin by remember { mutableStateOf(false) }
    var showPassword by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
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
                text = "Bem-vindo de volta!",
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
                    canLogin = false
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(firstItemShape),
                placeholder = {
                    Text(text = "E-mail")
                },
                isError = canLogin && (email.isEmpty() || !state.userAlreadyRegistered),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                ),
                keyboardActions = KeyboardActions(
                    onNext = {
                        focusManager.moveFocus(FocusDirection.Down)
                    },
                ),
                enabled = !state.isRegistering,
            )
            TextField(
                value = password,
                onValueChange = {
                    password = it
                    canLogin = false
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(lastItemShape),
                placeholder = {
                    Text(text = "Senha")
                },
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
                isError = canLogin && (password.isEmpty() || !state.isPasswordCorrect),
                singleLine = true,
                visualTransformation = if (!showPassword) {
                    PasswordVisualTransformation('*')
                } else {
                    VisualTransformation.None
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                    },
                ),
                enabled = !state.isRegistering,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Cadastre-se",
                    fontSize = 12.sp,
                    color = if (state.isRegistering) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier
                        .clickable {
                            if (state.isRegistering) return@clickable
                            onGoToRegister()
                        },
                )
                Text(
                    text = "Esqueceu a senha?",
                    fontSize = 12.sp,
                    color = if (state.isRegistering) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier
                        .clickable {
                            if (state.isRegistering) return@clickable
                            onGoToRecover()
                        },
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (state.isRegistering) {
                CircularProgressIndicator(
                    strokeCap = StrokeCap.Round,
                    modifier = Modifier.size(35.dp),
                )
            }
            Button(
                onClick = {
                    canLogin = true
                    if (email.isEmpty() || password.isEmpty()) {
                        snackBarScope.launch {
                            snackBarHostState.currentSnackbarData?.dismiss()
                            snackBarHostState.showSnackbar("Preencha todos os campos")
                        }
                        return@Button
                    }
                    onLogin(
                        RegisteredUser(
                            email = email,
                            password = password,
                        ),
                    )
                },
                enabled = !state.isRegistering,
            ) {
                Text(text = "Entrar")
            }
        }
    }

    LaunchedEffect(state.isPasswordCorrect, state.isRegistering, canLogin) {
        if (!state.isPasswordCorrect && !state.isRegistering && canLogin) {
            snackBarHostState.currentSnackbarData?.dismiss()
            snackBarHostState.showSnackbar("A senha está incorreta")
        }
    }
    LaunchedEffect(state.userAlreadyRegistered, state.isRegistering, canLogin, email) {
        if (!state.userAlreadyRegistered && !state.isRegistering && canLogin && email.isNotEmpty()) {
            snackBarHostState.currentSnackbarData?.dismiss()
            snackBarHostState.showSnackbar("Não foi encontrado um usuário com esse email")
        }
    }
    LaunchedEffect(state.isLoggedIn, canLogin) {
        if (state.isLoggedIn && canLogin) {
            snackBarHostState.currentSnackbarData?.dismiss()
            snackBarHostState.showSnackbar("Logado com sucesso!")
        }
    }
    LaunchedEffect(state.networkError, canLogin) {
        if (state.networkError && canLogin) {
            snackBarHostState.currentSnackbarData?.dismiss()
            snackBarHostState.showSnackbar("Não foi possível se conectar ao servidor")
        }
    }
}
