package com.rafael.inclusimap.feature.auth.presentation.components

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imeNestedScroll
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rafael.inclusimap.feature.auth.domain.model.LoginState
import com.rafael.inclusimap.feature.auth.domain.model.RegisteredUser

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LoginScreen(
    state: LoginState,
    onGoToRegister: () -> Unit,
    onLogin: (RegisteredUser) -> Unit,
    modifier: Modifier = Modifier,
    defaultRoundedShape: Shape = RoundedCornerShape(12.dp, 12.dp, 0.dp, 0.dp),
) {
    var password by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var canLogin by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val toast = Toast.makeText(context, "Preencha todos os campos", Toast.LENGTH_SHORT)
    val passwordIncorrectToast =
        Toast.makeText(context, "A senha está incorreta", Toast.LENGTH_LONG)
    val inexistentUserToast =
        Toast.makeText(context, "Não foi encontrado um usuário com esse email!", Toast.LENGTH_LONG)
    var showPassword by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        modifier = modifier,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
            modifier = Modifier
                .fillMaxWidth()
                .imeNestedScroll(),
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
                    .clip(defaultRoundedShape),
                placeholder = {
                    Text(text = "E-mail")
                },
                isError = canLogin && (email.isEmpty() || !state.userAlreadyRegistered),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                ),
            )
            TextField(
                value = password,
                onValueChange = {
                    password = it
                    canLogin = false
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(defaultRoundedShape),
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
            )
            Text(
                text = "Cadastre-se",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier
                    .clickable {
                        onGoToRegister()
                    },
            )
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
                        toast.show()
                        return@Button
                    }
                    onLogin(
                        RegisteredUser(
                            email = email,
                            password = password,
                        ),
                    )
                },
            ) {
                Text(text = "Entrar")
            }
        }
    }
    if (!state.isPasswordCorrect && !state.isRegistering && canLogin) {
        passwordIncorrectToast.show()
    }
    if (!state.userAlreadyRegistered && !state.isRegistering && canLogin) {
        inexistentUserToast.show()
    }
    if (state.isLoggedIn && canLogin) {
        Toast.makeText(context, "Logado com sucesso!", Toast.LENGTH_LONG).show()
    }
    if (state.networkError && canLogin) {
        Toast.makeText(context, "Ocorreu um erro na conexão!", Toast.LENGTH_LONG).show()
    }
}
