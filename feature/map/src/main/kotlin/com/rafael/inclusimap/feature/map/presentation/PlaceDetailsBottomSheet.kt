package com.rafael.inclusimap.feature.map.presentation

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imeNestedScroll
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.staggeredgrid.LazyHorizontalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.twotone.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rafael.inclusimap.core.domain.model.AccessibleLocalMarker
import com.rafael.inclusimap.core.domain.model.toAccessibleLocalMarker
import com.rafael.inclusimap.core.domain.model.toCategoryName
import com.rafael.inclusimap.core.domain.model.util.toColor
import com.rafael.inclusimap.core.domain.model.util.toMessage
import com.rafael.inclusimap.core.domain.util.Constants.MAX_IMAGE_NUMBER
import com.rafael.inclusimap.feature.map.domain.InclusiMapState
import com.rafael.inclusimap.feature.map.domain.PlaceDetailsEvent
import com.rafael.inclusimap.feature.map.domain.PlaceDetailsState
import com.rafael.inclusimap.feature.map.domain.Report
import com.rafael.inclusimap.feature.map.presentation.dialog.FullScreenImageViewDialog
import com.rafael.inclusimap.feature.map.presentation.dialog.PlaceInfoDialog
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalLayoutApi::class,
)
@Composable
fun PlaceDetailsBottomSheet(
    bottomSheetScaffoldState: SheetState,
    userEmail: String,
    userName: String,
    inclusiMapState: InclusiMapState,
    onDismiss: () -> Unit,
    onReport: (Report) -> Unit,
    state: PlaceDetailsState,
    onEvent: (PlaceDetailsEvent) -> Unit,
    onUpdateMappedPlace: (AccessibleLocalMarker) -> Unit,
    modifier: Modifier = Modifier,
) {
    val latestEvent by rememberUpdatedState(onEvent)
    val latestUpdateMappedPlace by rememberUpdatedState(onUpdateMappedPlace)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val focusRequester = remember { FocusRequester() }
    var showPlaceInfo by remember { mutableStateOf(false) }
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia()) { uris ->
            uris.takeIf { it.isNotEmpty() }?.let {
                if (uris.size + state.currentPlace.images.size > MAX_IMAGE_NUMBER) {
                    Toast.makeText(
                        context,
                        "Não foi possível adicionar todas as imagens selecionadas, o limite de imagens por local é $MAX_IMAGE_NUMBER",
                        Toast.LENGTH_SHORT,
                    ).show()
                    return@rememberLauncherForActivityResult
                }
                latestEvent(
                    PlaceDetailsEvent.OnUploadPlaceImages(
                        it,
                        context,
                        state.currentPlace.imageFolderId,
                        inclusiMapState.selectedMappedPlace?.id!!,
                    ),
                )
                if (uris.size <= 1) {
                    Toast.makeText(context, "Imagem adicionada!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "${uris.size} imagens adicionadas!", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    val currentPlace = inclusiMapState.selectedMappedPlace!!
    val accessibilityAverage by remember(
        state.trySendComment,
        state.currentPlace.comments,
        state.isUserCommented,
    ) {
        mutableFloatStateOf(
            state.currentPlace.comments.map { it.accessibilityRate }.average()
                .toFloat(),
        )
    }
    val accessibilityColor by animateColorAsState(
        accessibilityAverage.toColor(),
        label = "",
    )
    val gridHeight by remember { mutableStateOf(260.dp) }
    val imageWidth by remember { mutableStateOf(185.dp) }
    var showFullScreenImageViewer by remember { mutableStateOf(false) }
    var selectedImageIndex by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        latestEvent(PlaceDetailsEvent.SetCurrentPlace(currentPlace))
    }

    LaunchedEffect(state.currentPlace) {
        if (state.currentPlace.toAccessibleLocalMarker() != currentPlace) {
            latestUpdateMappedPlace(state.currentPlace.toAccessibleLocalMarker())
        }
    }

    ModalBottomSheet(
        sheetState = bottomSheetScaffoldState,
        onDismissRequest = {
            onDismiss()
        },
        modifier = Modifier.imeNestedScroll(),
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.weight(0.5f),
                ) {
                    Text(
                        text = state.currentPlace.title,
                        fontSize = 24.sp,
                    )
                    Text(
                        text = state.currentPlace.category?.toCategoryName() ?: "",
                        fontSize = 16.sp,
                    )
                }
                if (state.currentPlace.authorEmail == userEmail) {
                    IconButton(
                        onClick = {
                            latestEvent(PlaceDetailsEvent.SetIsEditingPlace(true))
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = null,
                        )
                    }
                }
                IconButton(
                    onClick = {
                        showPlaceInfo = true
                    },
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                    )
                }
                Box(
                    modifier = Modifier
                        .height(45.dp)
                        .widthIn(120.dp, 150.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(accessibilityColor)
                        .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = accessibilityAverage.toMessage(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (accessibilityAverage.toColor() == Color.Red) Color.White else Color.Black,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp,
                    )
                }
            }
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item {
                    Text(
                        text = "Imagens de ${state.currentPlace.title}",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(bottom = 8.dp),
                    )
                    LazyHorizontalStaggeredGrid(
                        rows = StaggeredGridCells.Fixed(1),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(gridHeight),
                        verticalArrangement = Arrangement.spacedBy(
                            8.dp,
                            Alignment.CenterVertically,
                        ),
                        horizontalItemSpacing = 8.dp,
                    ) {
                        state.currentPlace.images.forEachIndexed { index, image ->
                            image?.let {
                                item {
                                    var showRemoveImageBtn by remember { mutableStateOf(false) }
                                    Image(
                                        bitmap = it.image,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .width(185.dp)
                                            .height(250.dp)
                                            .clip(RoundedCornerShape(20.dp))
                                            .clickable(
                                                onClick = {
                                                    selectedImageIndex = index
                                                    showFullScreenImageViewer = true
                                                },
                                            ),
                                    )
                                    if (image.userEmail == userEmail) {
                                        Box(
                                            modifier = Modifier
                                                .width(185.dp)
                                                .height(250.dp)
                                                .padding(12.dp),
                                        ) {
                                            IconButton(
                                                onClick = {
                                                    latestEvent(
                                                        PlaceDetailsEvent.OnDeletePlaceImage(
                                                            image,
                                                        ),
                                                    )
                                                    showRemoveImageBtn = false
                                                    Toast.makeText(
                                                        context,
                                                        "Imagem removida!",
                                                        Toast.LENGTH_SHORT,
                                                    ).show()
                                                },
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .size(35.dp)
                                                    .clip(RoundedCornerShape(16.dp))
                                                    .background(
                                                        MaterialTheme.colorScheme.surface.copy(
                                                            alpha = 0.5f,
                                                        ),
                                                    ),
                                            ) {
                                                Icon(
                                                    imageVector = Icons.TwoTone.Delete,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(30.dp),
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(260.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                if (state.currentPlace.images.isEmpty() && state.allImagesLoaded) {
                                    Text(
                                        text = "Nenhuma imagem disponível desse local",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        textAlign = TextAlign.Center,
                                        style = TextStyle(
                                            lineHeight = 16.sp,
                                        ),
                                        modifier = Modifier
                                            .width(imageWidth)
                                            .padding(horizontal = 12.dp),
                                    )
                                }
                                if (!state.allImagesLoaded) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .width(imageWidth)
                                            .clip(RoundedCornerShape(24.dp)),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier
                                                .size(50.dp),
                                            strokeCap = StrokeCap.Round,
                                            color = MaterialTheme.colorScheme.primary,
                                            strokeWidth = 5.dp,
                                        )
                                    }
                                }
                                if (state.currentPlace.images.size < MAX_IMAGE_NUMBER) {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .width(imageWidth)
                                            .clip(RoundedCornerShape(24.dp))
                                            .clickable {
                                                launcher.launch(
                                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                                                )
                                            },
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize(),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center,
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.AddAPhoto,
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .size(40.dp),
                                            )
                                            Text(
                                                text = "Adicionar imagem",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Normal,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Comentários" + " (${state.currentPlace.comments.size})",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(vertical = 4.dp),
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(30.dp))
                            .border(
                                1.25.dp,
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                RoundedCornerShape(24.dp),
                            ),
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                if (state.isUserCommented) {
                                    Text(
                                        text = "Sua avaliação:",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        fontFamily = FontFamily.SansSerif,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.weight(1f),
                                    )
                                } else {
                                    Text(
                                        text = "Qual o nível de acessibilidade do local?",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f),
                                    )
                                }
                                val userAccessibilityColor by animateColorAsState(
                                    state.userAccessibilityRate.toFloat().coerceAtLeast(1f)
                                        .toColor(),
                                    label = "",
                                )
                                (1..3).forEach {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .padding(1.5.dp)
                                            .clip(CircleShape)
                                            .then(
                                                if (state.userAccessibilityRate != 0 && state.userAccessibilityRate >= it) {
                                                    Modifier.background(
                                                        userAccessibilityColor
                                                            .copy(
                                                                alpha = if (state.isUserCommented) 0.4f else 1f,
                                                            ),
                                                    )
                                                } else {
                                                    Modifier
                                                },
                                            )
                                            .border(
                                                1.25.dp,
                                                MaterialTheme.colorScheme.onSurface.copy(alpha = if (state.isUserCommented) 0.4f else 0.8f),
                                                CircleShape,
                                            )
                                            .clickable {
                                                latestEvent(
                                                    PlaceDetailsEvent.SetUserAccessibilityRate(it),
                                                )
                                            },
                                    )
                                }
                            }
                            if (!state.isUserCommented) {
                                TextField(
                                    value = state.userComment,
                                    onValueChange = {
                                        latestEvent(PlaceDetailsEvent.SetUserComment(it))
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .focusRequester(focusRequester),
                                    placeholder = {
                                        Text(text = "Adicione um comentário sobre a acessibilidade desse local")
                                    },
                                    maxLines = 6,
                                    shape = RoundedCornerShape(16.dp),
                                    trailingIcon = {
                                        IconButton(
                                            onClick = {
                                                latestEvent(PlaceDetailsEvent.OnSendComment)
                                                if (state.userComment.isEmpty()) {
                                                    Toast.makeText(
                                                        context,
                                                        "O comentário está vazio!",
                                                        Toast.LENGTH_SHORT,
                                                    ).show()
                                                    return@IconButton
                                                }
                                                Toast.makeText(
                                                    context,
                                                    "Comentário adicionado!",
                                                    Toast.LENGTH_SHORT,
                                                ).show()
                                            },
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.Send,
                                                contentDescription = null,
                                            )
                                        }
                                    },
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Text,
                                        capitalization = KeyboardCapitalization.Sentences,
                                        autoCorrectEnabled = true,
                                        imeAction = ImeAction.Send,
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onSend = {
                                            latestEvent(PlaceDetailsEvent.OnSendComment)
                                        },
                                    ),
                                    isError =
                                    (state.userAccessibilityRate == 0 || state.userComment.isEmpty()) && state.trySendComment,
                                )
                            } else {
                                Text(
                                    text = userName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Text(
                                        text = state.userComment,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Normal,
                                        modifier = Modifier.weight(1f),
                                    )
                                    IconButton(
                                        onClick = {
                                            latestEvent(PlaceDetailsEvent.SetIsUserCommented(false))
                                            scope.launch {
                                                async { bottomSheetScaffoldState.expand() }.await()
                                                focusRequester.requestFocus()
                                            }
                                        },
                                        modifier = Modifier
                                            .size(35.dp)
                                            .clip(CircleShape),
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Edit,
                                            contentDescription = "Edit",
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            latestEvent(PlaceDetailsEvent.OnDeleteComment)
                                            Toast.makeText(
                                                context,
                                                "Comentário removido!",
                                                Toast.LENGTH_SHORT,
                                            ).show()
                                        },
                                        modifier = Modifier
                                            .size(35.dp)
                                            .clip(CircleShape),
                                    ) {
                                        Icon(
                                            imageVector = Icons.TwoTone.Delete,
                                            contentDescription = null,
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(30.dp)),
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                        ) {
                            state.currentPlace.comments.filter { comment -> comment.email != userEmail }
                                .forEachIndexed { index, comment ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(
                                            text = comment.name,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.weight(1f),
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
                                                            .toColor(),
                                                    )
                                                    .border(
                                                        1.25.dp,
                                                        MaterialTheme.colorScheme.onSurface.copy(
                                                            alpha = 0.8f,
                                                        ),
                                                        CircleShape,
                                                    ),
                                            )
                                        }
                                    }
                                    Text(
                                        text = comment.body,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Normal,
                                    )
                                    if (index != (state.currentPlace.comments.filter { it.email != userEmail }.size - 1)) {
                                        HorizontalDivider()
                                    }
                                }
                            if (state.currentPlace.comments.isEmpty()) {
                                Text(
                                    text = "Nenhum comentário adicionado até agora",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Normal,
                                )
                            }
                            if (state.currentPlace.comments.size == 1 && state.currentPlace.comments.first().email == userEmail) {
                                Text(
                                    text = "Somente você comentou até agora",
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
    AnimatedVisibility(showPlaceInfo) {
        PlaceInfoDialog(
            localMarker = state.currentPlace.toAccessibleLocalMarker(),
            onDismiss = {
                showPlaceInfo = false
            },
            onReport = onReport,
        )
    }

    AnimatedVisibility(
        showFullScreenImageViewer,
        enter = scaleIn(),
        exit = scaleOut() + fadeOut(),
    ) {
        FullScreenImageViewDialog(
            placeName = state.currentPlace.title,
            images = state.currentPlace.images,
            index = selectedImageIndex,
            onDismiss = {
                showFullScreenImageViewer = false
            }
        )
    }
}
