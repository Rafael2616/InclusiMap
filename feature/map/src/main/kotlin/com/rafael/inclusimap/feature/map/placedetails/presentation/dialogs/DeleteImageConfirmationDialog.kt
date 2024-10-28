package com.rafael.inclusimap.feature.map.placedetails.presentation.dialogs

import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun DeleteImageConfirmationDialog(
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
    isDeletingImage: Boolean,
    isDeleted: Boolean,
    isInternetAvailable: Boolean,
    modifier: Modifier = Modifier,
) {
    val orientation = LocalConfiguration.current.orientation
    val isLandscape = orientation == Configuration.ORIENTATION_LANDSCAPE
    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnClickOutside = false,
            dismissOnBackPress = false,
        ),
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth(if (isLandscape) 0.5f else 0.8f)
                .clip(RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp),
            ),
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            ) {
                Text(
                    text = "Confirmação",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = "Deseja apagar essa foto?",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    lineHeight = 20.sp,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                    modifier = Modifier
                        .fillMaxWidth(),
                ) {
                    if (!isDeletingImage) {
                        OutlinedButton(
                            onClick = onDismiss,
                        ) {
                            Text(text = "Manter")
                        }
                        Button(
                            onClick = onDelete,
                            enabled = isInternetAvailable,
                            colors = ButtonDefaults.buttonColors()
                                .copy(
                                    containerColor = if (isInternetAvailable) {
                                        MaterialTheme.colorScheme.error
                                    } else MaterialTheme.colorScheme.errorContainer,
                                    contentColor = if (isInternetAvailable) {
                                        MaterialTheme.colorScheme.onError
                                    } else MaterialTheme.colorScheme.onErrorContainer,
                                ),
                        ) {
                            Text(text = "Remover")
                        }
                    } else {
                        Text(
                            text = "Removendo imagem \nAguarde... ",
                            fontSize = 12.sp,
                            lineHeight = 14.sp,
                            color = LocalContentColor.current.copy(alpha = 0.8f),
                        )
                        CircularProgressIndicator(
                            modifier = Modifier.size(30.dp),
                            strokeCap = StrokeCap.Round,
                        )
                    }
                }
            }
        }
    }

    if (isDeleted) {
        Toast.makeText(
            context,
            "Imagem removida!",
            Toast.LENGTH_SHORT,
        ).show()
        onDismiss()
    }
}
