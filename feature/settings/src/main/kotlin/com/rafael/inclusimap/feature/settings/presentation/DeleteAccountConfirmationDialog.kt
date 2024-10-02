package com.rafael.inclusimap.feature.settings.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NoAccounts
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.rafael.inclusimap.core.domain.model.DeleteProcess

@Suppress("ktlint:compose:modifier-not-used-at-root")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteAccountConfirmationDialog(
    isDeleting: Boolean,
    isLoginOut: Boolean,
    deleteStep: DeleteProcess,
    onDismissRequest: () -> Unit,
    onDeleteAccount: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    var keepContributions by remember { mutableStateOf(true) }
    var deleteProcessStarted by remember { mutableStateOf(true) }

    BasicAlertDialog(
        onDismissRequest = {
            if (deleteProcessStarted) {
                onDismissRequest()
            }
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
        ),
        modifier = Modifier.fillMaxWidth(0.82f),
    ) {
        Card(
            modifier = modifier
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surfaceColorAtElevation(10.dp)),
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = "Manter suas contribuições")
                    Checkbox(
                        checked = keepContributions,
                        onCheckedChange = {
                            keepContributions = it
                        },
                        enabled = deleteProcessStarted
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = "Apagar tudo")
                    Checkbox(
                        checked = !keepContributions,
                        onCheckedChange = {
                            keepContributions = !it
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.error,
                        ),
                        enabled = deleteProcessStarted
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (!isDeleting && !isLoginOut) {
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
                                text = if (!isLoginOut) {
                                    when (deleteStep) {
                                        DeleteProcess.NO_OP -> "Iniciando..."
                                        DeleteProcess.DELETING_USER_INFO -> "Deletando suas informações..."
                                        DeleteProcess.DELETING_USER_COMMENTS -> "Deletando seus comentários..."
                                        DeleteProcess.DELETING_USER_LOCAL_MARKERS -> "Deletando seus locais cadastrados..."
                                        DeleteProcess.DELETING_USER_IMAGES -> "Deletando suas imagens postadas..."
                                        DeleteProcess.SUCCESS -> "Conta deletada!"
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
                    if (!isDeleting && !isLoginOut) {
                        OutlinedButton(
                            onClick = {
                                deleteProcessStarted = false
                                onDeleteAccount(keepContributions)
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
}
