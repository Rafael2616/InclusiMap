package com.rafael.inclusimap.feature.auth.presentation.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
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
import com.rafael.inclusimap.feature.auth.domain.model.User
import com.rafael.inclusimap.feature.auth.domain.utils.isValidEmail
import com.rafael.inclusimap.feature.auth.domain.utils.isValidPassword
import com.rafael.inclusimap.feature.intro.presentation.dialogs.TermsAndConditionsDialog
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Composable
fun RegistrationScreen(
    state: LoginState,
    onRegister: (User) -> Unit,
    onGoToLogin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var userName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var canLogin by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val minNameLength by remember { mutableIntStateOf(3) }
    var showPassword by remember { mutableStateOf(false) }
    var isValidPassword by remember { mutableStateOf(true) }
    var isValidEmail by remember { mutableStateOf(true) }
    val focusManager = LocalFocusManager.current
    var termsAndConditionsAccepted by remember { mutableStateOf(true) }
    var showTermsAndConditionsDialog by remember { mutableStateOf(false) }
    var userAlreadyRegistered by remember(state.userAlreadyRegistered) { mutableStateOf(state.userAlreadyRegistered) }
    val firstItemShape = RoundedCornerShape(12.dp, 12.dp, 0.dp, 0.dp)
    val lastItemShape = RoundedCornerShape(0.dp, 0.dp, 12.dp, 12.dp)

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
                text = "Bem-vindo",
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth(),
            )
            TextField(
                value = userName,
                onValueChange = {
                    if (it.length <= 30) {
                        userName = it
                    }
                    canLogin = false
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(firstItemShape),
                placeholder = {
                    Text(text = "Nome completo")
                },
                isError = canLogin && (userName.isEmpty() || userName.length < minNameLength),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next,
                ),
                enabled = !state.isRegistering,
                trailingIcon = {
                    Row {
                        Text(
                            text = userName.length.toString(),
                            fontSize = 10.sp,
                            color = if (userName.length < minNameLength && userName.isNotEmpty()) MaterialTheme.colorScheme.error else LocalContentColor.current,
                        )
                        Text(
                            text = "/30",
                            fontSize = 10.sp,
                        )
                    }
                },
            )
            TextField(
                value = email,
                onValueChange = {
                    email = it
                    canLogin = false
                    isValidEmail = isValidEmail(it)
                },
                modifier = Modifier
                    .fillMaxWidth(),
                placeholder = {
                    Text(text = "E-mail")
                },
                isError = canLogin && (email.isEmpty() || userAlreadyRegistered) || !isValidEmail,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                ),
                enabled = !state.isRegistering,
            )
            TextField(
                value = password,
                onValueChange = {
                    password = it
                    canLogin = false
                    isValidPassword = isValidPassword(it)
                },
                modifier = Modifier
                    .fillMaxWidth(),
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
                isError = canLogin && password.isEmpty() || !isValidPassword,
                singleLine = true,
                visualTransformation = if (!showPassword) {
                    PasswordVisualTransformation('*')
                } else {
                    VisualTransformation.None
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next,
                ),
                enabled = !state.isRegistering,
            )
            TextField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    canLogin = false
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(lastItemShape),
                placeholder = {
                    Text(text = "Confirmar senha")
                },
                isError = canLogin && confirmPassword.isEmpty() || (password != confirmPassword && confirmPassword.isNotEmpty()),
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
            Text(
                text = "A senha deve conter pelo menos 8 dígitos, sendo: 1 letra maiuscula, 1 caractere especial e 1 número",
                fontSize = 10.sp,
                lineHeight = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier
                    .fillMaxWidth(),
            )
            Text(
                text = "Já tem uma conta? Entrar",
                fontSize = 12.sp,
                color = if (state.isRegistering) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier
                    .clickable {
                        if (state.isRegistering) return@clickable
                        onGoToLogin()
                    },
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(25.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Li e aceito os ",
                    fontSize = 12.sp,
                )
                Text(
                    text = "Termos e condições",
                    fontSize = 12.sp,
                    color = if (state.isRegistering) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier
                        .clickable {
                            if (state.isRegistering) return@clickable
                            showTermsAndConditionsDialog = true
                        },
                )
                Checkbox(
                    checked = termsAndConditionsAccepted,
                    onCheckedChange = {
                        termsAndConditionsAccepted = !termsAndConditionsAccepted
                        canLogin = false
                    },
                    colors = CheckboxDefaults.colors(
                        uncheckedColor = if (canLogin && !termsAndConditionsAccepted) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    ),
                    enabled = !state.isRegistering,
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
                enabled = !state.isRegistering,
                onClick = {
                    canLogin = true
                    userAlreadyRegistered = false
                    if (userName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                        Toast.makeText(context, "Preencha todos os campos", Toast.LENGTH_SHORT)
                            .show()
                        return@Button
                    }
                    if (userName.length < minNameLength) {
                        Toast.makeText(
                            context,
                            "O nome precisa ter no mínimo $minNameLength letras",
                            Toast.LENGTH_SHORT,
                        ).show()
                        return@Button
                    }
                    if (password != confirmPassword) {
                        Toast.makeText(context, "A senha deve ser igual", Toast.LENGTH_LONG)
                            .show()
                        return@Button
                    }
                    if (!isValidEmail) {
                        Toast.makeText(context, "O email é inválido", Toast.LENGTH_SHORT)
                            .show()
                        return@Button
                    }
                    if (!isValidPassword) {
                        Toast.makeText(context, "A senha é inválida", Toast.LENGTH_SHORT)
                            .show()
                        return@Button
                    }
                    if (!termsAndConditionsAccepted) {
                        Toast.makeText(context, "Aceite os termos e condições", Toast.LENGTH_SHORT)
                            .show()
                        return@Button
                    }
                    onRegister(
                        User(
                            id = Uuid.random().toString(),
                            name = userName,
                            email = email,
                            password = password,
                            showProfilePictureOptedIn = true,
                            isBanned = false,
                        ),
                    )
                    showTermsAndConditionsDialog = false
                },
            ) {
                Text(text = "Cadastrar")
            }
        }
    }

    DisposableEffect(userAlreadyRegistered, state.isRegistering) {
        if (userAlreadyRegistered && !state.isRegistering && canLogin && email.isNotEmpty()) {
            Toast.makeText(
                context,
                "Já existe um usuário cadastrado com esse email!",
                Toast.LENGTH_LONG,
            ).show()
        }
        onDispose { }
    }
    DisposableEffect(state.isLoggedIn, canLogin) {
        if (state.isLoggedIn && canLogin) {
            Toast.makeText(context, "Registrado com sucesso!", Toast.LENGTH_LONG).show()
        }
        onDispose { }
    }
    DisposableEffect(state.networkError, canLogin) {
        if (state.networkError && canLogin) {
            Toast.makeText(context, "Ocorreu um erro na conexão!", Toast.LENGTH_LONG).show()
        }
        onDispose { }
    }

    AnimatedVisibility(showTermsAndConditionsDialog) {
        TermsAndConditionsDialog(
            onDismissRequest = {
                showTermsAndConditionsDialog = false
            },
        )
    }
}
