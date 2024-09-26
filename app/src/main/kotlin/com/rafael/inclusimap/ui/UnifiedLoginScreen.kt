package com.rafael.inclusimap.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imeNestedScroll
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rafael.inclusimap.R
import com.rafael.inclusimap.domain.LoginState
import com.rafael.inclusimap.domain.RegisteredUser
import com.rafael.inclusimap.domain.User
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Composable
fun UnifiedLoginScreen(
    loginState: LoginState,
    onLogin: (RegisteredUser) -> Unit,
    onRegister: (User) -> Unit,
    modifier: Modifier = Modifier,
) {
    var cadastreNewUser by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primaryContainer,
                    ),
                )
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .shadow(
                    elevation = 20.dp,
                    shape = RoundedCornerShape(38.dp),
                    spotColor = MaterialTheme.colorScheme.onSurface,
                    clip = true,
                )
                .clip(RoundedCornerShape(38.dp)),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(20.dp)
            )
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.Top,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = "InclusiMap Logo",
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .size(70.dp),
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
            ) {
                Text(
                    text = "InclusiMap",
                    fontSize = 24.sp,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .fillMaxWidth()
                )
                AnimatedContent(
                    targetState = cadastreNewUser,
                    modifier = Modifier.fillMaxWidth(),
                    label = ""
                ) {
                    if (it) {
                        RegistrationScreen(
                            state = loginState,
                            onRegister = { registredUser -> onRegister(registredUser) },
                            onGoToLogin = { cadastreNewUser = false },
                        )
                    } else {
                        LoginScreen(
                            state = loginState,
                            onLogin = { user -> onLogin(user) },
                            onGoToRegister = { cadastreNewUser = true },
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalUuidApi::class, ExperimentalLayoutApi::class)
@Composable
fun RegistrationScreen(
    state: LoginState,
    onRegister: (User) -> Unit,
    onGoToLogin: () -> Unit,
    defaultRoundedShape: Shape = RoundedCornerShape(12.dp, 12.dp, 0.dp, 0.dp),
) {
    var userName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var canLogin by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val toast = Toast.makeText(
        context,
        if (state.userAlreadyRegistered) "Já existe um usuário com esse email!" else "Preencha todos os campos",
        Toast.LENGTH_SHORT
    )
    val differentPasswordToast =
        Toast.makeText(context, "A senha deve ser igual", Toast.LENGTH_SHORT)
    var showPassword by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
            modifier = Modifier
                .fillMaxWidth()
                .imeNestedScroll()
        ) {
            Text(
                text = "Bem-vindo",
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
            )
            TextField(
                value = userName,
                onValueChange = {
                    userName = it
                    canLogin = false
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(defaultRoundedShape),
                placeholder = {
                    Text(text = "Nome")
                },
                isError = canLogin && userName.isEmpty(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text
                )
            )
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
                isError = canLogin && email.isEmpty() || state.userAlreadyRegistered,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email
                )
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
                    IconButton(onClick = {
                        showPassword = !showPassword
                    }) {
                        Icon(
                            imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (showPassword) "Hide password" else "Show password",
                        )
                    }
                },
                isError = canLogin && password.isEmpty(),
                singleLine = true,
                visualTransformation = if (!showPassword) {
                    PasswordVisualTransformation('*')
                } else VisualTransformation.None,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password
                )
            )
            TextField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    canLogin = false
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(defaultRoundedShape),
                placeholder = {
                    Text(text = "Confirmar senha")
                },
                isError = canLogin && confirmPassword.isEmpty(),
                singleLine = true,
                visualTransformation = if (!showPassword) {
                    PasswordVisualTransformation('*')
                } else VisualTransformation.None,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password
                )
            )
            Text(
                text = "Já tem uma conta? Entrar",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier
                    .clickable {
                        onGoToLogin()
                    }
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (state.isRegistering) {
                CircularProgressIndicator()
            }
            Button(onClick = {
                canLogin = true
                if (userName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                    toast.show()
                    return@Button
                }
                if (password != confirmPassword) {
                    differentPasswordToast.show()
                    return@Button
                }
                onRegister(
                    User(
                        id = Uuid.random().toString(),
                        name = userName,
                        email = email,
                        password = password
                    )
                )

            }) {
                Text(text = "Cadastrar")
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LoginScreen(
    state: LoginState,
    onGoToRegister: () -> Unit,
    onLogin: (RegisteredUser) -> Unit,
    defaultRoundedShape: Shape = RoundedCornerShape(12.dp, 12.dp, 0.dp, 0.dp),
) {
    var password by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var canLogin by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val toast = Toast.makeText(context, "Preencha todos os campos", Toast.LENGTH_SHORT)
    val passwordIncorrectToast =
        Toast.makeText(context, "A senha está incorreta", Toast.LENGTH_SHORT)
    val inexistentUserToast =
        Toast.makeText(context, "Não foi encontrado um usuário com esse email!", Toast.LENGTH_SHORT)

    var showPassword by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
            modifier = Modifier
                .fillMaxWidth()
                .imeNestedScroll()
        ) {
            Text(
                text = "Bem-vindo de volta!",
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
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
                    keyboardType = KeyboardType.Email
                )
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
                    IconButton(onClick = {
                        showPassword = !showPassword
                    }) {
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
                } else VisualTransformation.None,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password
                )
            )
            Text(
                text = "Cadastre-se",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier
                    .clickable {
                        onGoToRegister()
                    }
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (state.isRegistering) {
                CircularProgressIndicator()
            }
            Button(onClick = {
                canLogin = true
                if (email.isEmpty() || password.isEmpty()) {
                    toast.show()
                    return@Button
                }
                onLogin(
                    RegisteredUser(
                        email = email,
                        password = password
                    )
                )
            }) {
                Text(text = "Entrar")
            }
        }
    }
        if (!state.isPasswordCorrect && canLogin) {
            passwordIncorrectToast.show()
        }
        if (!state.userAlreadyRegistered && canLogin) {
            inexistentUserToast.show()
        }
}
