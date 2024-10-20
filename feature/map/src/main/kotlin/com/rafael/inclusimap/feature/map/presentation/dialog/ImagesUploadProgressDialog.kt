package com.rafael.inclusimap.feature.map.presentation.dialog

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun ImagesUploadProgressDialog(
    imagesSize: Int?,
    currentUploadedImageSize: Int?,
    isUploadingImages: Boolean,
    isErrorUploadingImages: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    Dialog(
        onDismissRequest = {
            onDismiss()
        },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
        ),
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp),
            ),
        ) {
            Column(
                modifier = Modifier
                    .padding(bottom = 16.dp, top = 8.dp)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
            ) {
                Text(
                    text = "Adicionando imagem $currentUploadedImageSize de $imagesSize",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                LinearProgressIndicator(
                    progress = {
                        (currentUploadedImageSize?.toFloat()
                            ?.div(imagesSize?.toFloat() ?: 1f)) ?: 0f
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
    if (currentUploadedImageSize == imagesSize && !isUploadingImages) {
        imagesSize?.let {
            if (imagesSize <= 1) {
                Toast.makeText(context, "Imagem adicionada!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "$imagesSize imagens adicionadas!", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        onDismiss()
    }
    if (currentUploadedImageSize != imagesSize && !isUploadingImages && isErrorUploadingImages) {
        Toast.makeText(
            context,
            "Algumas imagens não foram adicionadas",
            Toast.LENGTH_SHORT,
        ).show()
        onDismiss()
    }
}
