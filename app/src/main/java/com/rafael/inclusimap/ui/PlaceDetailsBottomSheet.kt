package com.rafael.inclusimap.ui

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.staggeredgrid.LazyHorizontalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import androidx.compose.ui.util.fastForEach
import com.rafael.inclusimap.data.GoogleDriveService
import com.rafael.inclusimap.data.toColor
import com.rafael.inclusimap.data.toMessage
import com.rafael.inclusimap.domain.AccessibleLocalMarker
import com.rafael.inclusimap.domain.Comment
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
    var userComment by remember { mutableStateOf("") }
    var updatedLocalMarker by remember { mutableStateOf(localMarker) }
    var userAcessibilityRate by remember { mutableStateOf(0) }
    var trySendComment by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.IO) {
            val sharedFolders = driveService.listFiles("18C_8JhqLKaLUVif_Vh1_nl0LzfF5zVYM")
            val localMarkerFolder = sharedFolders.find { it.name == localMarker.title }?.id

            if (localMarkerFolder.isNullOrEmpty()) {
                allImagesLoaded = true
                return@launch
            }
            val localMarkerFiles = driveService.listFiles(localMarkerFolder)
            localMarkerFiles.fastForEach {
                try {
                    val fileContent =
                        driveService.driveService.files().get(it.id).executeMediaAsInputStream()
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
                val accessibilityAverage by remember(trySendComment) {
                    mutableStateOf(
                        updatedLocalMarker.comments?.map { it.accessibilityRate }?.average()
                            ?.toFloat()
                    )
                }
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
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
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
                            rows = StaggeredGridCells.Fixed(if (localMarkerImages.size < 5) 1 else 2),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(
                                    when (localMarkerImages.size) {
                                        in 1..4 -> 170.dp
                                        in 5..Int.MAX_VALUE -> 330.dp
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
                                                .padding(4.dp)
                                                .clip(RoundedCornerShape(20.dp))
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
                        text = "Comentários" + " (${updatedLocalMarker.comments?.size ?: 0})",
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
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Qual o nível de acessibilidade do local?",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                (1..3).forEach {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .padding(1.5.dp)
                                            .clip(CircleShape)
                                            .then(
                                                if (userAcessibilityRate != 0 && userAcessibilityRate >= it) {
                                                    Modifier.background(
                                                        userAcessibilityRate
                                                            .toFloat()
                                                            .toColor()
                                                    )
                                                } else Modifier
                                            )
                                            .border(
                                                1.25.dp,
                                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                                CircleShape
                                            )
                                            .clickable {
                                                userAcessibilityRate = it
                                            }
                                    )
                                }
                            }
                            TextField(
                                value = userComment,
                                onValueChange = {
                                    userComment = it
                                    trySendComment = false
                                },
                                modifier = Modifier
                                    .fillMaxWidth(),
                                placeholder = {
                                    Text(text = "Adicione um comentário sobre a acessibilidade desse local")
                                },
                                maxLines = 6,
                                shape = RoundedCornerShape(16.dp),
                                trailingIcon = {
                                    IconButton(
                                        onClick = {
                                            trySendComment = true
                                            if (userComment.isNotEmpty() && userAcessibilityRate != 0) {
                                                updatedLocalMarker.comments =
                                                    updatedLocalMarker.comments?.plus(
                                                        Comment(
                                                            postDate = "21/08/2024",
                                                            id = updatedLocalMarker.comments?.size?.plus(
                                                                1
                                                            ) ?: 1,
                                                            name = "<Sem Nome>",
                                                            body = userComment,
                                                            email = "",
                                                            accessibilityRate = userAcessibilityRate,
                                                        )
                                                    )
                                                trySendComment = false
                                                userComment = ""
                                                userAcessibilityRate = 0
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Send,
                                            contentDescription = null
                                        )
                                    }
                                },
                                isError =
                                (userAcessibilityRate == 0 || userComment.isEmpty()) && trySendComment,
                            )
                            Spacer(Modifier.height(8.dp))
                            updatedLocalMarker.comments?.forEachIndexed { index, comment ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Start,
                                ) {
                                    Text(
                                        text = comment.name,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    for (i in 1..comment.accessibilityRate) {
                                        Box(
                                            modifier = Modifier
                                                .size(15.dp)
                                                .padding(1.5.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    comment.accessibilityRate
                                                        .toFloat()
                                                        .toColor()
                                                )
                                                .border(
                                                    1.25.dp,
                                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                                    CircleShape
                                                )
                                        )
                                    }
                                }
                                Text(
                                    text = comment.body,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Normal,
                                )
                                if (index != updatedLocalMarker.comments!!.size - 1) {
                                    HorizontalDivider()
                                }
                            }
                            if (updatedLocalMarker.comments.isNullOrEmpty()) {
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
    }
}