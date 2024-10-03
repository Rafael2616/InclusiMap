package com.rafael.inclusimap.feature.auth.presentation.components

import android.widget.Toast
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
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rafael.inclusimap.feature.auth.domain.model.LoginState
import com.rafael.inclusimap.feature.auth.domain.utils.isValidPassword

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UpdatePasswordScreen(
    state: LoginState,
    onCancel: () -> Unit,
    onUpdatePassword: (String) -> Unit,
    popBackStack: () -> Unit,
    modifier: Modifier = Modifier,
    defaultRoundedShape: Shape = RoundedCornerShape(12.dp, 12.dp, 0.dp, 0.dp),
) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var canUpdate by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val toast = Toast.makeText(context, "Preencha todos os campos", Toast.LENGTH_SHORT)
    var showPassword by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    var isValidPassword by remember { mutableStateOf(true) }

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
                text = "Olá!, ${state.user?.name?.split(" ")?.first()}",
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(4.dp))
            TextField(
                value = password,
                onValueChange = {
                    password = it
                    canUpdate = false
                    isValidPassword = isValidPassword(it)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(defaultRoundedShape),
                placeholder = {
                    Text(text = "Nova senha")
                },
                isError = canUpdate && (password.isEmpty() || state.isSamePassword) || !isValidPassword,
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
            )
            TextField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    canUpdate = false
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(defaultRoundedShape),
                placeholder = {
                    Text(text = "Confirmar nova senha")
                },
                isError = canUpdate && confirmPassword.isEmpty() || (password != confirmPassword && confirmPassword.isNotEmpty()),
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
                text = "A senha deve conter pelo menos 8 dígitos, sendo: 1 letra maiuscula, 1 caractere especial e 1 número",
                fontSize = 10.sp,
                lineHeight = 12.sp,
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
            if (state.isUpdatingPassword) {
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
                    if (password.isEmpty() || confirmPassword.isEmpty()) {
                        toast.show()
                        return@Button
                    }
                    if (password != confirmPassword) {
                        Toast.makeText(context, "A senha deve ser igual", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (!isValidPassword) {
                        Toast.makeText(context, "A senha é inválida", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    onUpdatePassword(password)
                },
            ) {
                Text(text = "Atualizar")
            }
        }
    }

    if (state.isSamePassword && canUpdate) {
        Toast.makeText(context, "A nova senha não pode ser igual a atual!", Toast.LENGTH_LONG)
            .show()
    }
    if (state.networkError && canUpdate) {
        Toast.makeText(context, "Ocorreu um erro na conexão!", Toast.LENGTH_LONG).show()
    }
    if (state.isPasswordChanged) {
        Toast.makeText(context, "Senha atualizada com sucesso!", Toast.LENGTH_LONG).show()
        popBackStack()
    }
}
