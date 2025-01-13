package com.rafael.inclusimap.feature.auth.presentation.dialogs

import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.outlined.NoAccounts
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.rafael.inclusimap.core.domain.model.DeleteProcess
import com.rafael.inclusimap.feature.auth.domain.model.LoginState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteAccountConfirmationDialog(
    loginState: LoginState,
    onDismissRequest: () -> Unit,
    onDeleteAccount: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    var keepContributions by remember { mutableStateOf(true) }
    var deleteProcessStarted by remember { mutableStateOf(true) }
    val orientation = LocalConfiguration.current.orientation
    val isLandscape = orientation == Configuration.ORIENTATION_LANDSCAPE
    var showPasswordConfirmationDialog by remember { mutableStateOf(false) }

    BasicAlertDialog(
        onDismissRequest = {
            if (deleteProcessStarted) {
                onDismissRequest()
            }
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
        ),
        modifier = modifier.fillMaxWidth(if (isLandscape) 0.5f else 0.82f),
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
                    Icons.Outlined.NoAccounts,
                    contentDescription = "No account",
                    modifier = Modifier
                        .size(45.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = "Deseja excluir sua conta?",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    text = "Esta ação não pode ser desfeita!",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.error,
                )
                Spacer(modifier = Modifier.height(40.dp))
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Manter suas contribuições",
                            fontSize = 16.sp,
                            color = LocalContentColor.current.copy(alpha = 0.85f),
                        )
                        Checkbox(
                            checked = keepContributions,
                            onCheckedChange = {
                                keepContributions = it
                            },
                            enabled = deleteProcessStarted,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Apagar tudo",
                            fontSize = 14.sp,
                            color = LocalContentColor.current.copy(alpha = 0.85f),
                        )
                        Checkbox(
                            checked = !keepContributions,
                            onCheckedChange = {
                                keepContributions = !it
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = MaterialTheme.colorScheme.error,
                            ),
                            enabled = deleteProcessStarted,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (!loginState.isDeletingAccount && !loginState.isLoginOut) {
                        OutlinedButton(
                            onClick = {
                                onDismissRequest()
                            },
                        ) {
                            Text(text = "Voltar")
                        }
                    } else {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                fontSize = 14.sp,
                                text = if (!loginState.isLoginOut) {
                                    when (loginState.deleteStep) {
                                        DeleteProcess.NO_OP -> "Iniciando..."
                                        DeleteProcess.DELETING_USER_INFO -> "Deletando suas informações..."
                                        DeleteProcess.DELETING_USER_COMMENTS -> "Deletando seus comentários..."
                                        DeleteProcess.DELETING_USER_LOCAL_MARKERS -> "Deletando locais cadastrados..."
                                        DeleteProcess.DELETING_USER_IMAGES -> "Deletando imagens postadas..."
                                        DeleteProcess.SUCCESS -> "Saindo..."
                                        DeleteProcess.ERROR -> "Ocorreu um erro, tente novamente!"
                                    }
                                } else {
                                    "Saindo..."
                                },
                            )
                            CircularProgressIndicator(
                                modifier = Modifier.size(30.dp),
                            )
                        }
                    }
                    if (!loginState.isDeletingAccount && !loginState.isLoginOut) {
                        OutlinedButton(
                            onClick = {
                                deleteProcessStarted = false
                                showPasswordConfirmationDialog = true
                            },
                            colors = ButtonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError,
                                disabledContainerColor = MaterialTheme.colorScheme.errorContainer,
                                disabledContentColor = MaterialTheme.colorScheme.onErrorContainer,
                            ),
                        ) {
                            Text(text = "Excluir")
                        }
                    }
                }
            }
        }
    }
    val context = LocalContext.current
    if (loginState.networkError && loginState.deleteStep == DeleteProcess.ERROR) {
        Toast.makeText(
            context,
            "Ocorreu um erro durante a exclusão, tente novamente!",
            Toast.LENGTH_LONG,
        ).show()
    }
    if (loginState.deleteStep == DeleteProcess.SUCCESS && !deleteProcessStarted) {
        Toast.makeText(context, "Conta deletada com sucesso!", Toast.LENGTH_LONG).show()
    }

    AnimatedVisibility(showPasswordConfirmationDialog) {
        DeleteAccountPasswordConfirmationDialog(
            loginState = loginState,
            onDismissRequest = {
                showPasswordConfirmationDialog = false
            },
            onPasswordConfirmed = {
                showPasswordConfirmationDialog = false
                onDeleteAccount(keepContributions)
            },
        )
    }
}
