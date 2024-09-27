package com.rafael.inclusimap.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rafael.inclusimap.R
import com.rafael.inclusimap.data.isValidEmail
import com.rafael.inclusimap.data.isValidPassword
import com.rafael.inclusimap.domain.LoginState
import com.rafael.inclusimap.domain.RegisteredUser
import com.rafael.inclusimap.domain.User
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UnifiedLoginScreen(
    loginState: LoginState,
    onLogin: (RegisteredUser) -> Unit,
    onRegister: (User) -> Unit,
    modifier: Modifier = Modifier,
) {
    var cadastreNewUser by remember { mutableStateOf(false) }

    Box(
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
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .imeNestedScroll()
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
    val toast = Toast.makeText(context, "Preencha todos os campos", Toast.LENGTH_SHORT)
    val invalidEmailToast = Toast.makeText(context, "O email é inválido", Toast.LENGTH_SHORT)
    val invalidPasswordToast = Toast.makeText(context, "A senha é inválida", Toast.LENGTH_SHORT)
    val differentPasswordToast =
        Toast.makeText(context, "A senha deve ser igual", Toast.LENGTH_LONG)
    var showPassword by remember { mutableStateOf(false) }
    var isValidPassword by remember { mutableStateOf(true) }
    var isValidEmail by remember { mutableStateOf(true) }
    val existentUserToast =
        Toast.makeText(
            context,
            "Já existe um usuário cadastrado com esse email!",
            Toast.LENGTH_LONG
        )

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
                    Text(text = "Nome completo")
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
                    isValidEmail = isValidEmail(it)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(defaultRoundedShape),
                placeholder = {
                    Text(text = "E-mail")
                },
                isError = canLogin && email.isEmpty() || state.userAlreadyRegistered || !isValidEmail,
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
                    isValidPassword = isValidPassword(it)
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
                isError = canLogin && password.isEmpty() || !isValidPassword,
                singleLine = true,
                visualTransformation = if (!showPassword) {
                    PasswordVisualTransformation('*')
                } else VisualTransformation.None,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    capitalization = KeyboardCapitalization.Words
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
                isError = canLogin && confirmPassword.isEmpty() || password != confirmPassword,
                singleLine = true,
                visualTransformation = if (!showPassword) {
                    PasswordVisualTransformation('*')
                } else VisualTransformation.None,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    capitalization = KeyboardCapitalization.Words
                )
            )
            Text(
                text = "A senha deve conter pelo menos 8 dígitos, sendo: 1 letra maiuscula, 1 caractere especial e 1 número",
                fontSize = 10.sp,
                lineHeight = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier
                    .fillMaxWidth()
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
                CircularProgressIndicator(
                    strokeCap = StrokeCap.Round,
                    modifier = Modifier.size(30.dp)
                )
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
                if (!isValidEmail) {
                    invalidEmailToast.show()
                    return@Button
                }
                if (!isValidPassword) {
                    invalidPasswordToast.show()
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
    if (state.userAlreadyRegistered && !state.isRegistering && canLogin) {
        existentUserToast.show()
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
        Toast.makeText(context, "A senha está incorreta", Toast.LENGTH_LONG)
    val inexistentUserToast =
        Toast.makeText(context, "Não foi encontrado um usuário com esse email!", Toast.LENGTH_LONG)

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
                    keyboardType = KeyboardType.Email,
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
                    keyboardType = KeyboardType.Password,
                    capitalization = KeyboardCapitalization.Words
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
                CircularProgressIndicator(
                    strokeCap = StrokeCap.Round,
                    modifier = Modifier.size(30.dp)
                )
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
    if (!state.isPasswordCorrect && !state.isRegistering && canLogin) {
        passwordIncorrectToast.show()
    }
    if (!state.userAlreadyRegistered && !state.isRegistering && canLogin) {
        inexistentUserToast.show()
    }
}
