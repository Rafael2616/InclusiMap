package com.rafael.inclusimap.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rafael.inclusimap.R
import com.rafael.inclusimap.data.toColor
import com.rafael.inclusimap.data.toMessage
import com.rafael.inclusimap.domain.AccessibleLocalMarker
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceDetainsBottomSheet(
    localMarker: AccessibleLocalMarker,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bottomSheetScaffoldState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    ModalBottomSheet(
        sheetState = bottomSheetScaffoldState,
        onDismissRequest = {
            scope.launch {
                bottomSheetScaffoldState.hide()
            }
            onDismiss()
        },
        properties = ModalBottomSheetProperties(
            shouldDismissOnBackPress = true,
        )
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = localMarker.title,
                        fontSize = 24.sp,
                    )
                    Text(
                        text = localMarker.description,
                        fontSize = 16.sp,
                    )
                }
                val accessibilityAverage = localMarker.comments?.map { it.accessibilityRate }?.average()?.toFloat()
                Box(
                    modifier = Modifier
                        .height(45.dp)
                        .widthIn(120.dp, 150.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(accessibilityAverage?.toColor() ?: Color.Gray)
                        .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.Center,

                ) {
                    Text(
                        text = accessibilityAverage?.toMessage() ?: "Sem dados de\nacessibilidade",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp,
                    )
                }
            }

            Text(
                text = "Imagens de ${localMarker.title}",
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(vertical = 4.dp)
            )
            repeat(2) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth(),

                    ) {
                    items(3) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_launcher_foreground),
                            contentDescription = null,
                            modifier = Modifier
                                .height(160.dp)
                                .padding(6.dp)
                                .clip(RoundedCornerShape(24.dp))
                        )
                    }
                }
            }
            Text(
                text = "Comentários",
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(vertical = 4.dp)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceColorAtElevation(30.dp))
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    localMarker.comments?.forEachIndexed { index, comment ->
                        Text(
                            text = comment.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = comment.body,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                        )
                        if (index != localMarker.comments.size - 1) {
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}
