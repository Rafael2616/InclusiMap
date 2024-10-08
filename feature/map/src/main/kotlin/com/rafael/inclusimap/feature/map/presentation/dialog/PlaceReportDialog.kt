package com.rafael.inclusimap.feature.map.presentation.dialog

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imeNestedScroll
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.rafael.inclusimap.core.domain.model.AccessibleLocalMarker
import com.rafael.inclusimap.feature.map.domain.Report
import com.rafael.inclusimap.feature.map.domain.ReportType
import com.rafael.inclusimap.feature.map.domain.toText

@Composable
@OptIn(ExperimentalLayoutApi::class)
fun PlaceReportDialog(
    localMarker: AccessibleLocalMarker,
    onDismiss: () -> Unit,
    onReport: (Report) -> Unit,
    modifier: Modifier = Modifier,
) {
    var reportType by remember { mutableStateOf(ReportType.LOCAL) }
    var report by remember { mutableStateOf("") }
    val maxReportLength by remember { mutableIntStateOf(250) }
    val minReportLength by remember { mutableIntStateOf(15) }
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
        ),
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(24.dp))
                .imeNestedScroll(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp),
            ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "Realizar um report",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    Text(
                        text = "Local: ",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                    )
                    Text(
                        text = localMarker.title,
                        fontWeight = FontWeight.Normal,
                        fontSize = 18.sp,
                    )
                }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(2.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp),
                    ),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                    ) {
                        Text(
                            text = "O que você deseja reportar?",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        ReportType.entries.forEach { type ->
                            Row(
                                modifier = Modifier.height(35.dp),
                            ) {
                                Text(
                                    text = type.toText(),
                                    modifier = Modifier.weight(1f),
                                )
                                Checkbox(
                                    checked = reportType == type,
                                    onCheckedChange = {
                                        reportType = type
                                    },
                                )
                            }
                        }
                    }
                }
                TextField(
                    value = report,
                    onValueChange = {
                        if (it.length <= maxReportLength) {
                            report = it
                        }
                    },
                    label = {
                        Text(text = "Descreva o report aqui!")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(20.dp),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(
                            20.dp,
                        ),
                    ),
                    trailingIcon = {
                        Row {
                            Text(
                                text = "${report.length}",
                                fontSize = 12.sp,
                                color = if (report.length < minReportLength && report.isNotEmpty()) MaterialTheme.colorScheme.error else LocalContentColor.current,
                            )
                            Text(
                                text = "/$maxReportLength",
                                fontSize = 12.sp,
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        autoCorrectEnabled = true,
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                        }
                    ),
                    shape = RoundedCornerShape(topEnd = 12.dp, topStart = 12.dp),
                )
                Text(
                    text = "Antes de enviar, certifique-se de que o conteúdo do report está de acordo com as políticas do aplicativo!",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    lineHeight = 12.sp,
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Button(
                        onClick = onDismiss,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    ) {
                        Text(text = "Cancelar")
                    }
                    IconButton(
                        onClick = {
                            if (report.isEmpty()) {
                                Toast.makeText(
                                    context,
                                    "Report não pode estar vazio!",
                                    Toast.LENGTH_SHORT,
                                ).show()
                                return@IconButton
                            }
                            if (report.length < minReportLength) {
                                Toast.makeText(
                                    context,
                                    "Report deve conter pelo menos $minReportLength caracteres!",
                                    Toast.LENGTH_SHORT,
                                ).show()
                                return@IconButton
                            }
                            Toast.makeText(
                                context,
                                "Enviando report...",
                                Toast.LENGTH_SHORT,
                            ).show()
                            onReport(
                                Report(
                                    type = reportType,
                                    content = report,
                                    reportedLocal = localMarker,
                                    user = null, // User will handled in the viewmodel
                                ),
                            )
                            onDismiss()
                        },
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.Send,
                            contentDescription = "Enviar",
                        )
                    }
                }
            }
        }
    }
}
