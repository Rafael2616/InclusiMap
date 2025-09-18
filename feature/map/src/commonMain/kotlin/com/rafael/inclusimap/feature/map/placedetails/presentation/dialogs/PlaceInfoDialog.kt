package com.rafael.inclusimap.feature.map.placedetails.presentation.dialogs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowOutward
import androidx.compose.material.icons.outlined.CopyAll
import androidx.compose.material.icons.outlined.Report
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.rafael.inclusimap.core.ui.isLandscape
import com.rafael.inclusimap.core.util.map.formatDate
import com.rafael.inclusimap.core.util.map.model.AccessibleLocalMarker
import com.rafael.inclusimap.feature.map.placedetails.domain.util.openInGoogleMap
import com.rafael.inclusimap.feature.report.domain.model.Report
import com.rafael.inclusimap.feature.report.presentation.dialogs.PlaceReportDialog
import com.rafael.libs.maps.interop.model.MapsLatLng
import kotlinx.coroutines.launch

@Composable
fun PlaceInfoDialog(
    currentPlace: AccessibleLocalMarker,
    isInternetAvailable: Boolean,
    googleMapsPlaceId: String?,
    onDismiss: () -> Unit,
    onReport: (Report) -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    var showReportDialog by remember { mutableStateOf(false) }
    LocalClipboard.current
    val scope = rememberCoroutineScope()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
        ),
    ) {
        Card(
            modifier = modifier
                .navigationBarsPadding()
                .fillMaxWidth(if (isLandscape) 0.55f else 0.9f)
                .clip(RoundedCornerShape(24.dp)),
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = currentPlace.title,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Normal,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row {
                            Text(
                                text = "Adicionado em: ",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                lineHeight = 18.sp,
                            )
                            Text(
                                text = currentPlace.time.split(".")[0].formatDate() ?: "",
                                fontSize = 14.sp,
                                maxLines = 2,
                                lineHeight = 18.sp,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        ElevatedButton(
                            onClick = {
                                showReportDialog = true
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp),
                            ),
                            enabled = isInternetAvailable,
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(2.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Report,
                                    contentDescription = null,
                                    modifier = Modifier.size(30.dp),
                                    tint = if (isInternetAvailable) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    text = "Reportar",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = if (isInternetAvailable) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Coordenadas:",
                        fontSize = 14.sp,
                        maxLines = 1,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 16.sp,
                    )
                    IconButton(
                        onClick = {
                            scope.launch {
                                // Todo
//                                clipboardManager.setClipEntry(
//                                    ClipEntry(
//                                        ClipData.newPlainText(
//                                            "Coordenadas",
//                                            "${currentPlace.position.first.toFloat()}, ${currentPlace.position.second.toFloat()}",
//                                        ),
//                                    ),
//                                )
                            }
                        },
                        modifier = Modifier
                            .size(24.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CopyAll,
                            contentDescription = null,
                        )
                    }
                    Text(
                        text = "(${currentPlace.position.first.toFloat()}, ${currentPlace.position.second.toFloat()})",
                        fontSize = 12.sp,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        lineHeight = 16.sp,
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Endereço:",
                        fontSize = 14.sp,
                        maxLines = 1,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 16.sp,
                    )
                    IconButton(
                        onClick = {
                            scope.launch {
//                                clipboardManager.setClipEntry(
//                                    ClipEntry(
//                                        ClipData.newPlainText(
//                                            "Endereço",
//                                            currentPlace.address + " - ${currentPlace.locatedIn}",
//                                        ),
//                                    ),
//                                )
                            }
                        },
                        modifier = Modifier
                            .size(24.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CopyAll,
                            contentDescription = null,
                        )
                    }
                    Text(
                        text = currentPlace.address + " - ${currentPlace.locatedIn}",
                        fontSize = 14.sp,
                        maxLines = 2,
                        fontWeight = FontWeight.Normal,
                        lineHeight = 16.sp,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            openInGoogleMap(
                                latLng = MapsLatLng(
                                    currentPlace.position.first.toDouble(),
                                    currentPlace.position.second.toDouble(),
                                ),
                                placeID = googleMapsPlaceId,
                            )
                        },
                ) {
                    Text(
                        text = "Ver no Google Maps",
                        fontSize = 14.sp,
                        maxLines = 1,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 2.dp),
                    )
                    Icon(
                        imageVector = Icons.Outlined.ArrowOutward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
        }
    }

    AnimatedVisibility(showReportDialog) {
        PlaceReportDialog(
            localMarker = currentPlace,
            onDismiss = { showReportDialog = false },
            onReport = { onReport(it) },
            snackbarHostState = snackbarHostState,
        )
    }
}
