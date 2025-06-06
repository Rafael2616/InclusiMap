package com.rafael.inclusimap.feature.intro.presentation.dialogs

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewDynamicColors
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.rafael.inclusimap.core.domain.model.AccessibilityChipItem
import com.rafael.inclusimap.core.resources.draws.GoogleMapsPin

@Composable
fun AppIntroDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    userName: String? = null,
) {
    val orientation = LocalConfiguration.current.orientation
    val isLandscape = orientation == Configuration.ORIENTATION_LANDSCAPE

    Dialog(
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
        ),
        onDismissRequest = {
            onDismiss()
        },
    ) {
        Card(
            modifier = modifier
                .navigationBarsPadding()
                .statusBarsPadding()
                .fillMaxWidth(if (isLandscape) 0.55f else 0.9f)
                .wrapContentHeight()
                .clip(RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp),
            ),
        ) {
            LazyColumn(
                Modifier
                    .padding(16.dp)
                    .padding(top = 4.dp),
                horizontalAlignment = Alignment.Start,
            ) {
                if (userName != null) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp),
                                        ),
                                        tileMode = TileMode.Clamp,
                                    ),
                                    RoundedCornerShape(16.dp),
                                ),
                        ) {
                            Text(
                                text = "Bem vindo ao InclusiMap,\n${userName.split(" ")[0]}",
                                fontSize = 24.sp,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.W400,
                                modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
                            )
                        }
                    }
                }
                item {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier
                            .height(460.dp),
                    ) {
                        item {
                            Text(
                                text = "Como funciona o sistema de acessibilidade do app?",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(8.dp),
                            )
                        }
                        AccessibilityChipItem.get().forEach { item ->
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    GoogleMapsPin(item.color)
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth(),
                                        horizontalAlignment = Alignment.Start,
                                    ) {
                                        Text(
                                            text = item.name,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                        )
                                        Text(
                                            text = item.description,
                                            fontSize = 14.sp,
                                            lineHeight = 18.sp,
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier
                            .fillMaxWidth(),
                    ) {
                        Button(
                            onClick = {
                                onDismiss()
                            },
                            modifier = Modifier,
                        ) {
                            Text(
                                text = "Entendi",
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview
@PreviewLightDark
@PreviewDynamicColors
@Composable
private fun AppIntroDialogPreview() {
    AppIntroDialog(
        onDismiss = {},
        userName = "Rafael",
    )
}
