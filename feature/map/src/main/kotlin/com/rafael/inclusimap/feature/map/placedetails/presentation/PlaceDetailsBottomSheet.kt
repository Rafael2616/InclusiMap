package com.rafael.inclusimap.feature.map.placedetails.presentation

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.twotone.Delete
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rafael.inclusimap.core.domain.model.AccessibleLocalMarker
import com.rafael.inclusimap.core.domain.model.PlaceImage
import com.rafael.inclusimap.core.domain.model.icon
import com.rafael.inclusimap.core.domain.model.toAccessibleLocalMarker
import com.rafael.inclusimap.core.domain.model.toCategoryName
import com.rafael.inclusimap.core.domain.model.toResourceIcon
import com.rafael.inclusimap.core.domain.model.util.formatDate
import com.rafael.inclusimap.core.domain.model.util.removeTime
import com.rafael.inclusimap.core.domain.model.util.toColor
import com.rafael.inclusimap.core.domain.model.util.toMessage
import com.rafael.inclusimap.core.domain.network.InternetConnectionState
import com.rafael.inclusimap.core.domain.util.Constants.MAX_IMAGE_NUMBER
import com.rafael.inclusimap.feature.map.map.domain.InclusiMapState
import com.rafael.inclusimap.feature.map.placedetails.domain.model.PlaceDetailsEvent
import com.rafael.inclusimap.feature.map.placedetails.domain.model.PlaceDetailsState
import com.rafael.inclusimap.feature.map.placedetails.presentation.dialogs.AccessibilityResourcesSelectionDialog
import com.rafael.inclusimap.feature.map.placedetails.presentation.dialogs.DeleteImageConfirmationDialog
import com.rafael.inclusimap.feature.map.placedetails.presentation.dialogs.FullScreenImageViewDialog
import com.rafael.inclusimap.feature.map.placedetails.presentation.dialogs.ImagesUploadProgressDialog
import com.rafael.inclusimap.feature.map.placedetails.presentation.dialogs.PlaceInfoDialog
import com.rafael.inclusimap.feature.map.placedetails.presentation.dialogs.UnsavedCommentDialog
import com.rafael.inclusimap.feature.report.domain.model.Report
import com.rafael.inclusimap.feature.report.domain.model.ReportState
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceDetailsBottomSheet(
    userEmail: String,
    userName: String,
    userPicture: ImageBitmap?,
    inclusiMapState: InclusiMapState,
    onDismiss: () -> Unit,
    onReport: (Report) -> Unit,
    state: PlaceDetailsState,
    onEvent: (PlaceDetailsEvent) -> Unit,
    onUpdateMappedPlace: (AccessibleLocalMarker) -> Unit,
    reportState: ReportState,
    allowedShowUserProfilePicture: suspend (String) -> Boolean,
    downloadUserProfilePicture: suspend (String) -> ImageBitmap?,
    modifier: Modifier = Modifier,
) {
    val placeDetailsBottomSheetScope = rememberCoroutineScope()
    val placeDetailsBottomSheetState = rememberModalBottomSheetState()
    val latestEvent by rememberUpdatedState(onEvent)
    val latestUpdateMappedPlace by rememberUpdatedState(onUpdateMappedPlace)
    val context = LocalContext.current
    var showPlaceInfo by remember { mutableStateOf(false) }
    var showFullScreenImageViewer by remember { mutableStateOf(false) }
    var selectedImageIndex by remember { mutableIntStateOf(0) }
    val internetState = remember { InternetConnectionState(context) }
    val isInternetAvailable by internetState.state.collectAsStateWithLifecycle()
    var showToast by remember { mutableStateOf(false) }
    var showUploadImagesProgressDialog by remember { mutableStateOf(false) }
    var showAccessibilityResourcesSelectionDialog by remember { mutableStateOf(false) }
    val currentPlace by remember { mutableStateOf(inclusiMapState.selectedMappedPlace) }
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
    val accessibilityColor by animateColorAsState(accessibilityAverage.toColor(), label = "")

    DisposableEffect(Unit) {
        currentPlace?.let { place ->
            latestEvent(PlaceDetailsEvent.SetCurrentPlace(place))
        }
        onDispose {}
    }

    LaunchedEffect(state.currentPlace) {
        if (state.currentPlace.toAccessibleLocalMarker() != currentPlace) {
            latestUpdateMappedPlace(state.currentPlace.toAccessibleLocalMarker())
        }
    }

    ModalBottomSheet(
        sheetState = placeDetailsBottomSheetState,
        onDismissRequest = {
            placeDetailsBottomSheetScope.launch {
                placeDetailsBottomSheetState.hide()
            }.invokeOnCompletion {
                if (!placeDetailsBottomSheetState.isVisible) {
                    onDismiss()
                }
            }
        },
        properties = rememberModalBottomSheetProperties(shouldDismissOnBackPress = true),
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
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
                        fontSize = 23.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = state.currentPlace.category?.toCategoryName() ?: "",
                            fontSize = 16.sp,
                        )
                        state.currentPlace.category?.let { category ->
                            Icon(
                                imageVector = category.icon(),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                }
                if (state.currentPlace.authorEmail == userEmail) {
                    IconButton(
                        onClick = {
                            latestEvent(PlaceDetailsEvent.SetIsEditingPlace(true))
                        },
                        enabled = isInternetAvailable,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
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
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                item {
                    ImageSection(
                        state = state,
                        isInternetAvailable = isInternetAvailable,
                        onEvent = onEvent,
                        userEmail = userEmail,
                        onShowFullScreenImageViewer = {
                            showFullScreenImageViewer = true
                            selectedImageIndex = it
                        },
                        onShowUploadImagesProgress = {
                            showUploadImagesProgressDialog = true
                        },
                    )
                }
                item {
                    AccessibilityResourcesSection(
                        isInternetAvailable = isInternetAvailable,
                        state = state,
                        onAddAccessibilityResource = {
                            showAccessibilityResourcesSelectionDialog = true
                        },
                    )
                }
                item {
                    CommentSection(
                        state = state,
                        isInternetAvailable = isInternetAvailable,
                        onEvent = onEvent,
                        userPicture = userPicture,
                        userName = userName,
                        userEmail = userEmail,
                        bottomSheetState = placeDetailsBottomSheetState,
                        allowedShowUserProfilePicture = allowedShowUserProfilePicture,
                        downloadUserProfilePicture = downloadUserProfilePicture,
                        onShouldShowUnsavedCommentDialog = {
                            //  showUnsavedCommentDialog = it
                        },
                    )
                }
            }
        }
    }

    AnimatedVisibility(showPlaceInfo) {
        PlaceInfoDialog(
            currentPlace = state.currentPlace.toAccessibleLocalMarker(),
            onDismiss = {
                showPlaceInfo = false
            },
            onReport = onReport,
            isInternetAvailable = isInternetAvailable,
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
            },
        )
    }

    if (reportState.isReported && showToast) {
        showToast = false
        Toast.makeText(context, "Report enviado!", Toast.LENGTH_SHORT).show()
    }
    if (reportState.isError && !reportState.isReported && showToast) {
        showToast = false
        Toast.makeText(context, "Ocorreu um erro ao enviar report", Toast.LENGTH_SHORT)
            .show()
    }

    // Do not enable this while the issue that causes the sheet to be hide with back press even it is disable is solved
    /*showUnsavedCommentDialog && isDismissing*/
    AnimatedVisibility(false) {
        UnsavedCommentDialog(
            onDismiss = {
                onDismiss()
            },
            onContinue = {
                // isDismissing = false
            },
        )
    }

    AnimatedVisibility(showUploadImagesProgressDialog) {
        ImagesUploadProgressDialog(
            imagesSize = state.imagesToUploadSize,
            currentUploadedImageSize = state.imagesUploadedSize,
            isUploadingImages = state.isUploadingImages,
            isErrorUploadingImages = state.isErrorUploadingImages,
            onDismiss = {
                showUploadImagesProgressDialog = false
            },
        )
    }

    AnimatedVisibility(showAccessibilityResourcesSelectionDialog) {
        AccessibilityResourcesSelectionDialog(
            isInternetAvailable = isInternetAvailable,
            state = state,
            onDismiss = {
                showAccessibilityResourcesSelectionDialog = false
            },
            onUpdateAccessibilityResources = {
                latestEvent(PlaceDetailsEvent.OnUpdatePlaceAccessibilityResources(it))
            },
        )
    }
}

@Composable
fun ImageSection(
    state: PlaceDetailsState,
    isInternetAvailable: Boolean,
    onEvent: (PlaceDetailsEvent) -> Unit,
    userEmail: String,
    onShowFullScreenImageViewer: (Int) -> Unit,
    onShowUploadImagesProgress: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val gridHeight by remember { mutableStateOf(260.dp) }
    val imageWidth by remember { mutableStateOf(185.dp) }
    var showToast by remember { mutableStateOf(false) }
    var showDeleteImageConfirmationDialog by remember { mutableStateOf(false) }
    var selectedImage by remember { mutableStateOf<PlaceImage?>(null) }
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
                onEvent(
                    PlaceDetailsEvent.OnUploadPlaceImages(
                        it,
                        context,
                        state.currentPlace.id ?: return@rememberLauncherForActivityResult,
                    ),
                )
                onShowUploadImagesProgress()
            }
        }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "Imagens de ${state.currentPlace.title}",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier,
        )
        LazyHorizontalGrid(
            rows = GridCells.Fixed(1),
            modifier = Modifier
                .fillMaxWidth()
                .height(gridHeight),
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            state.currentPlace.images.forEachIndexed { index, image ->
                image?.let {
                    item {
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
                                        onShowFullScreenImageViewer(index)
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
                                        selectedImage = image
                                        showDeleteImageConfirmationDialog = true
                                    },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(35.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(
                                            MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                                        ),
                                    enabled = isInternetAvailable,
                                ) {
                                    Icon(
                                        imageVector = Icons.TwoTone.Delete,
                                        contentDescription = null,
                                        tint = if (isInternetAvailable) MaterialTheme.colorScheme.primary else Color.Gray,
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
                                    if (!isInternetAvailable) {
                                        Toast
                                            .makeText(
                                                context,
                                                "Sem conexão com a internet",
                                                Toast.LENGTH_SHORT,
                                            )
                                            .show()
                                        return@clickable
                                    }
                                    showToast = true
                                    launcher.launch(
                                        PickVisualMediaRequest(
                                            ActivityResultContracts.PickVisualMedia.ImageOnly,
                                        ),
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
                                    text = "Adicionar imagens",
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

    AnimatedVisibility(showDeleteImageConfirmationDialog) {
        DeleteImageConfirmationDialog(
            onDismiss = {
                showDeleteImageConfirmationDialog = false
                selectedImage = null
            },
            onDelete = {
                selectedImage?.let { onEvent(PlaceDetailsEvent.OnDeletePlaceImage(it)) }
            },
            isDeletingImage = state.isDeletingImage,
            isInternetAvailable = isInternetAvailable,
            isDeleted = state.isImageDeleted,
        )
    }
}

@Composable
fun AccessibilityResourcesSection(
    isInternetAvailable: Boolean,
    state: PlaceDetailsState,
    onAddAccessibilityResource: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(
            text = "Recursos de acessibilidade",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(vertical = 4.dp),
        )
        LazyHorizontalStaggeredGrid(
            rows = StaggeredGridCells.Fixed(if (state.currentPlace.resources.size in 0..2) 1 else 2),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(if (state.currentPlace.resources.size in 0..2) 55.dp else 110.dp),
            horizontalItemSpacing = 6.dp,
        ) {
            if (state.currentPlace.resources.isNotEmpty()) {
                state.currentPlace.resources.forEach { resource ->
                    item {
                        AssistChip(
                            label = {
                                Text(text = resource.resource.displayName)
                            },
                            onClick = { },
                            modifier = Modifier.height(45.dp),
                            enabled = true,
                            leadingIcon = {
                                Icon(
                                    imageVector = resource.resource.displayName.toResourceIcon(),
                                    contentDescription = null,
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(5.dp),
                            ),
                        )
                    }
                }
            }
            item {
                AssistChip(
                    label = {
                        Text(text = if (state.currentPlace.resources.isEmpty()) "Adicionar" else "Editar")
                    },
                    onClick = {
                        onAddAccessibilityResource()
                    },
                    modifier = Modifier.height(45.dp),
                    leadingIcon = {
                        Icon(
                            imageVector = if (state.currentPlace.resources.isEmpty()) Icons.Outlined.Add else Icons.Filled.Edit,
                            contentDescription = null,
                        )
                    },
                    border = BorderStroke(
                        1.dp,
                        if (isInternetAvailable) MaterialTheme.colorScheme.primary else Color.Gray,
                    ),
                    enabled = isInternetAvailable,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentSection(
    state: PlaceDetailsState,
    isInternetAvailable: Boolean,
    onEvent: (PlaceDetailsEvent) -> Unit,
    userPicture: ImageBitmap?,
    userName: String,
    userEmail: String,
    bottomSheetState: SheetState,
    onShouldShowUnsavedCommentDialog: (Boolean) -> Unit,
    allowedShowUserProfilePicture: suspend (String) -> Boolean,
    downloadUserProfilePicture: suspend (String) -> ImageBitmap?,
    modifier: Modifier = Modifier,
) {
    val maxCommentLength by remember { mutableIntStateOf(300) }
    val latestEvent by rememberUpdatedState(onEvent)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val latestAllowedShowUserProfilePicture by rememberUpdatedState(allowedShowUserProfilePicture)
    val latestDownloadUserProfilePicture by rememberUpdatedState(downloadUserProfilePicture)
    var showUserCommentOptions by remember { mutableStateOf(false) }
    var userComment by remember(state.isUserCommented) { mutableStateOf(state.userComment) }

    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(
            text = "Comentários" + " (${state.currentPlace.comments.size})",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(vertical = 4.dp),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surfaceColorAtElevation(15.dp))
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
                        state.userAccessibilityRate
                            .toFloat()
                            .coerceAtLeast(1f)
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
                                            userAccessibilityColor.copy(
                                                alpha = if (state.isUserCommented && !state.isEditingComment || !isInternetAvailable) 0.4f else 1f,
                                            ),
                                        )
                                    } else {
                                        Modifier
                                    },
                                )
                                .border(
                                    1.25.dp,
                                    MaterialTheme.colorScheme.primary.copy(
                                        alpha = if (state.isUserCommented) 0.4f else 0.8f,
                                    ),
                                    CircleShape,
                                )
                                .clickable {
                                    latestEvent(PlaceDetailsEvent.SetUserAccessibilityRate(it))
                                },
                        )
                    }
                }
                if (!state.isUserCommented || state.isEditingComment) {
                    TextField(
                        value = userComment,
                        onValueChange = {
                            if (it.length <= maxCommentLength) {
                                userComment = it
                                latestEvent(PlaceDetailsEvent.SetIsTrySendComment(false))
                            }
                            if (it.isNotEmpty()) {
                                onShouldShowUnsavedCommentDialog(true)
                            } else {
                                onShouldShowUnsavedCommentDialog(false)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth(),
                        placeholder = {
                            Text(text = "Adicione um comentário sobre a acessibilidade desse local")
                        },
                        maxLines = 3,
                        shape = RoundedCornerShape(16.dp, 16.dp, 0.dp, 0.dp),
                        trailingIcon = {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(2.dp),
                                horizontalAlignment = Alignment.End,
                                modifier = Modifier.padding(end = 6.dp, bottom = 6.dp),
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.End,
                                    modifier = Modifier.padding(end = 2.dp),
                                ) {
                                    Text(
                                        text = userComment.length.toString(),
                                        fontSize = 10.sp,
                                        color = if (userComment.length < 3 && userComment.isNotEmpty()) {
                                            MaterialTheme.colorScheme.error
                                        } else {
                                            MaterialTheme.colorScheme.onSurface
                                        },
                                    )
                                    Text(
                                        text = "/$maxCommentLength",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                }
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    if (state.isEditingComment) {
                                        IconButton(
                                            modifier = Modifier
                                                .size(30.dp),
                                            onClick = {
                                                latestEvent(
                                                    PlaceDetailsEvent.SetIsEditingComment(
                                                        false,
                                                    ),
                                                )
                                                userComment = state.userComment
                                                onShouldShowUnsavedCommentDialog(false)
                                            },
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Close,
                                                contentDescription = null,
                                            )
                                        }
                                    }
                                    IconButton(
                                        modifier = Modifier
                                            .size(30.dp),
                                        onClick = {
                                            onShouldShowUnsavedCommentDialog(false)
                                            if (userComment.isEmpty()) {
                                                Toast.makeText(
                                                    context,
                                                    "O comentário está vazio!",
                                                    Toast.LENGTH_SHORT,
                                                ).show()
                                                return@IconButton
                                            }
                                            if (userComment.length < 3) {
                                                Toast.makeText(
                                                    context,
                                                    "O comentário é muito curto!",
                                                    Toast.LENGTH_SHORT,
                                                ).show()
                                                return@IconButton
                                            }
                                            if (state.userAccessibilityRate == 0) {
                                                Toast.makeText(
                                                    context,
                                                    "Selecione uma avaliação!",
                                                    Toast.LENGTH_SHORT,
                                                ).show()
                                                return@IconButton
                                            }
                                            latestEvent(PlaceDetailsEvent.OnSendComment(userComment))
                                            Toast.makeText(
                                                context,
                                                "Comentário adicionado!",
                                                Toast.LENGTH_SHORT,
                                            ).show()
                                        },
                                        enabled = isInternetAvailable,
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Send,
                                            contentDescription = null,
                                        )
                                    }
                                }
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
                                latestEvent(PlaceDetailsEvent.OnSendComment(userComment))
                            },
                        ),
                        enabled = isInternetAvailable,
                        isError = (state.userAccessibilityRate == 0 || userComment.isEmpty()) &&
                            state.trySendComment,
                    )
                }
                if (state.isUserCommented && !state.isEditingComment) {
                    Row {
                        if (userPicture != null) {
                            Image(
                                bitmap = userPicture,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop,
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.Person,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(30.dp),
                            )
                        }
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = userName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = state.userCommentDate.removeTime()?.formatDate() ?: "",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        Text(
                            text = userComment,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            modifier = Modifier.weight(1f),
                        )
                        IconButton(
                            onClick = {
                                showUserCommentOptions = true
                            },
                            modifier = Modifier
                                .size(35.dp)
                                .clip(CircleShape),
                            enabled = isInternetAvailable,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = "More",
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.End),
                    ) {
                        DropdownMenu(
                            expanded = showUserCommentOptions,
                            onDismissRequest = {
                                showUserCommentOptions = false
                            },
                            offset = DpOffset(0.dp, (-40).dp),
                            shape = RoundedCornerShape(16.dp),
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text(text = "Editar")
                                },
                                onClick = {
                                    onShouldShowUnsavedCommentDialog(true)
                                    latestEvent(PlaceDetailsEvent.SetIsEditingComment(true))
                                    scope.launch {
                                        async { bottomSheetState.expand() }.await()
                                        showUserCommentOptions = false
                                    }
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.Edit,
                                        contentDescription = null,
                                    )
                                },
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(text = "Remover")
                                },
                                onClick = {
                                    latestEvent(PlaceDetailsEvent.OnDeleteComment)
                                    userComment = ""
                                    Toast.makeText(
                                        context,
                                        "Comentário removido!",
                                        Toast.LENGTH_SHORT,
                                    ).show()
                                    showUserCommentOptions = false
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.TwoTone.Delete,
                                        contentDescription = null,
                                    )
                                },
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
                .background(MaterialTheme.colorScheme.surfaceColorAtElevation(15.dp)),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
            ) {
                state.currentPlace.comments.filter { comment -> comment.email != userEmail }
                    .forEachIndexed { index, comment ->
                        var userProfilePicture by remember {
                            mutableStateOf<ImageBitmap?>(null)
                        }
                        var allowedShowUserPicture by remember {
                            mutableStateOf<Boolean?>(null)
                        }
                        LaunchedEffect(Unit) {
                            allowedShowUserPicture =
                                latestAllowedShowUserProfilePicture(comment.email)
                        }
                        LaunchedEffect(allowedShowUserPicture == true) {
                            userProfilePicture = latestDownloadUserProfilePicture(comment.email)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top,
                        ) {
                            if (allowedShowUserPicture == true && userProfilePicture != null) {
                                Image(
                                    bitmap = userProfilePicture!!,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop,
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Outlined.Person,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(30.dp),
                                )
                            }
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = comment.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = comment.postDate.removeTime()?.formatDate() ?: "",
                                fontSize = 12.sp,
                                maxLines = 1,
                                fontWeight = FontWeight.Normal,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                                modifier = Modifier.weight(1f),
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.CenterEnd,
                            ) {
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
                                                1.dp,
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                                CircleShape,
                                            ),
                                    )
                                }
                            }
                        }
                        Text(
                            text = comment.body,
                            fontSize = 14.sp,
                            lineHeight = 18.sp,
                            fontWeight = FontWeight.Normal,
                        )
                        if (index != (
                                state.currentPlace.comments.filter {
                                    it.email != userEmail
                                }.size - 1
                                )
                        ) {
                            HorizontalDivider(thickness = 2.dp)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberModalBottomSheetProperties(shouldDismissOnBackPress: Boolean) =
    remember(shouldDismissOnBackPress) {
        ModalBottomSheetProperties(
            shouldDismissOnBackPress = shouldDismissOnBackPress,
        )
    }
