package com.rafael.inclusimap.ui

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.staggeredgrid.LazyHorizontalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rafael.inclusimap.data.GoogleDriveService
import com.rafael.inclusimap.data.toColor
import com.rafael.inclusimap.data.toMessage
import com.rafael.inclusimap.domain.AccessibleLocalMarker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceDetailsBottomSheet(
    driveService: GoogleDriveService,
    localMarker: AccessibleLocalMarker,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bottomSheetScaffoldState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    val localMarkerImages = remember { mutableStateListOf<ImageBitmap?>() }
    var allImagesLoaded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.IO) {
            val sharedFolders = driveService.listFiles("18C_8JhqLKaLUVif_Vh1_nl0LzfF5zVYM")
            val localMarkerFolder = sharedFolders.find { it.name == localMarker.title }?.id

            if (localMarkerFolder.isNullOrEmpty()) {
                allImagesLoaded = true
                return@launch
            }
            val localMarkerFiles = driveService.listFiles(localMarkerFolder)
            localMarkerFiles.forEach {
                val fileContent =
                    driveService.driveService.files().get(it.id).executeMediaAsInputStream()
                try {
                    val options = BitmapFactory.Options()
                    options.inSampleSize = 3
                    BitmapFactory.decodeStream(fileContent, null, options)
                        ?.asImageBitmap()?.also { image ->
                            localMarkerImages += image
                            if (localMarkerImages.size == localMarkerFiles.size) {
                                allImagesLoaded = true
                            }
                        }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

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
                val accessibilityAverage =
                    localMarker.comments?.map { it.accessibilityRate }?.average()?.toFloat()
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
                        color = if (accessibilityAverage?.toColor() == Color.Red) Color.White else Color.Black,
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
            if (localMarkerImages.isNotEmpty()) {
                LazyHorizontalStaggeredGrid(
                    rows = StaggeredGridCells.Fixed(if (localMarkerImages.size < 4) 1 else 2),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(
                            when (localMarkerImages.size) {
                                in 1..3 -> 170.dp
                                in 4..Int.MAX_VALUE -> 320.dp
                                else -> 50.dp
                            }
                        )
                ) {
                    localMarkerImages.forEach { image ->
                        image?.let {
                            item {
                                Image(
                                    bitmap = it,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .width(140.dp)
                                        .height(170.dp)
                                        .padding(6.dp)
                                        .clip(RoundedCornerShape(24.dp))
                                )
                            }
                        }
                    }
                    if (!allImagesLoaded) {
                        item {
                            Box(
                                modifier = Modifier
                                    .width(140.dp)
                                    .height(170.dp)
                                    .clip(RoundedCornerShape(24.dp)),
                                contentAlignment = Alignment.Center,
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .size(50.dp),
                                    strokeCap = StrokeCap.Round,
                                    color = MaterialTheme.colorScheme.primary,
                                    strokeWidth = 5.dp
                                )
                            }
                        }
                    }
                }
            }
            if (localMarkerImages.isEmpty() && !allImagesLoaded) {
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(50.dp),
                        strokeCap = StrokeCap.Round,
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 5.dp
                    )
                }
            }
            if (localMarkerImages.isEmpty() && allImagesLoaded) {
                Text(
                    text = "Nenhuma imagem disponível desse local",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.fillMaxWidth()
                )
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
                    if (localMarker.comments.isNullOrEmpty()) {
                        Text(
                            text = "Nenhum comentário adicionado até agora",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                        )
                    }
                }
            }
        }
    }
}
