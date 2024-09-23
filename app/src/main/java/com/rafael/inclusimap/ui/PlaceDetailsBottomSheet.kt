package com.rafael.inclusimap.ui

import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
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
import com.google.api.services.drive.model.File
import com.rafael.inclusimap.data.GoogleDriveService
import com.rafael.inclusimap.data.extractUserName
import com.rafael.inclusimap.data.toColor
import com.rafael.inclusimap.data.toMessage
import com.rafael.inclusimap.domain.AccessibleLocalMarker
import com.rafael.inclusimap.domain.Comment
import com.rafael.inclusimap.domain.PlaceImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class,
    ExperimentalLayoutApi::class, ExperimentalComposeUiApi::class
)
@Composable
fun PlaceDetailsBottomSheet(
    driveService: GoogleDriveService,
    localMarker: AccessibleLocalMarker,
    bottomSheetScaffoldState: SheetState,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    var sharedFolders: List<File> by remember { mutableStateOf(emptyList()) }
    var localMarkerFolder by remember { mutableStateOf<String?>(null) }
    val localMarkerImages = remember { mutableStateListOf<PlaceImage?>() }
    var allImagesLoaded by remember { mutableStateOf(false) }
    var userComment by remember { mutableStateOf("") }
    val updatedLocalMarker by remember { mutableStateOf(localMarker) }
    var userAcessibilityRate by remember { mutableIntStateOf(0) }
    var trySendComment by remember { mutableStateOf(false) }
    val userName by remember { mutableStateOf("<Sem Nome>") }
    val context = LocalContext.current
    var isUserCommented by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            uri?.let {
                localMarkerImages.add(
                    PlaceImage(
                        userName = userName,
                        BitmapFactory.decodeStream(
                            context.contentResolver.openInputStream(it)
                        ).asImageBitmap()
                    )
                )
                scope.launch(Dispatchers.IO) {
                    async {
                        if (localMarkerFolder.isNullOrEmpty()) {
                            localMarkerFolder = driveService.createFolder(
                                localMarker.title,
                                "18C_8JhqLKaLUVif_Vh1_nl0LzfF5zVYM"
                            )
                        }
                    }.await()
                    driveService.uploadFile(
                        context.contentResolver.openInputStream(it),
                        "${
                            localMarker.title.replace(
                                " ",
                                ""
                            )
                        }-${userName}-${System.currentTimeMillis()}.jpg",
                        localMarkerFolder
                            ?: throw IllegalStateException("Folder not found: Maybe an issue has occurred while creating the folder")
                    )
                }
            }
        }
    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.IO) {
            sharedFolders = driveService.listFiles("18C_8JhqLKaLUVif_Vh1_nl0LzfF5zVYM")
            localMarkerFolder = sharedFolders.find { it.name == localMarker.title }?.id

            if (localMarkerFolder.isNullOrEmpty() || driveService.listFiles(localMarkerFolder!!)
                    .isEmpty()
            ) {
                allImagesLoaded = true
                return@launch
            }
            val localMarkerFiles = driveService.listFiles(localMarkerFolder!!)
            localMarkerFiles.map { file ->
                async {
                    try {
                        val fileContent = driveService.driveService.files().get(file.id)
                            .executeMediaAsInputStream()
                        val options = BitmapFactory.Options()
                        options.inSampleSize = 3
                        BitmapFactory.decodeStream(fileContent, null, options)
                            ?.asImageBitmap()?.also { image ->
                                localMarkerImages += PlaceImage(
                                    userName = file.name.extractUserName(),
                                    image = image
                                )
                                if (localMarkerImages.size == localMarkerFiles.size) {
                                    allImagesLoaded = true
                                }
                            }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }.awaitAll()
        }
    }

    ModalBottomSheet(
        sheetState = bottomSheetScaffoldState,
        onDismissRequest = {
            updatedLocalMarker.comments = updatedLocalMarker.comments?.filter { it.name != userName }
            onDismiss()
        },
        modifier = Modifier.imeNestedScroll(),
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
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
                val accessibilityAverage by remember(
                    trySendComment,
                    updatedLocalMarker.comments,
                    isUserCommented
                ) {
                    mutableStateOf(
                        updatedLocalMarker.comments?.map { it.accessibilityRate }?.average()
                            ?.toFloat()
                    )
                }
                val acessibilityColor by animateColorAsState(
                    accessibilityAverage?.toColor() ?: Color.Gray
                )
                Box(
                    modifier = Modifier
                        .height(45.dp)
                        .widthIn(120.dp, 150.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(acessibilityColor)
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
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text(
                        text = "Imagens de ${localMarker.title}",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                    )
                    val gridHeight = 260.dp
                    val imageWidth = 185.dp
                    val deleteImageScope = rememberCoroutineScope()
                    LazyHorizontalStaggeredGrid(
                        rows = StaggeredGridCells.Fixed(1),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(gridHeight),
                        verticalArrangement = Arrangement.spacedBy(
                            8.dp,
                            Alignment.CenterVertically
                        ),
                        horizontalItemSpacing = 8.dp
                    ) {
                        localMarkerImages.forEach { image ->
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
                                            .combinedClickable(
                                                onClick = {
                                                    // Do nothing
                                                },
                                                onLongClick = {
                                                    showRemoveImageBtn = true
                                                }
                                            )
                                    )
                                    if (image.userName == userName) {
                                        Box(
                                            modifier = Modifier
                                                .width(185.dp)
                                                .height(250.dp)
                                                .padding(12.dp)
                                        ) {
                                            IconButton(
                                                onClick = {
                                                    localMarkerImages.remove(image)
                                                    deleteImageScope.launch(Dispatchers.Default) {
                                                        val localMarkerFiles =
                                                            driveService.listFiles(localMarkerFolder.orEmpty())
                                                        val imageId =
                                                            localMarkerFiles.find { it.name.extractUserName() == image.userName }?.id.orEmpty()
                                                        driveService.deleteFile(imageId)
                                                    }.invokeOnCompletion {
                                                        showRemoveImageBtn = false
                                                    }
                                                },
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .size(35.dp)
                                                    .clip(RoundedCornerShape(16.dp))
                                                    .background(
                                                        MaterialTheme.colorScheme.surface.copy(
                                                            alpha = 0.5f
                                                        )
                                                    )
                                            ) {
                                                Icon(
                                                    imageVector = Icons.TwoTone.Delete,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(30.dp)
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
                                if (localMarkerImages.isEmpty() && allImagesLoaded) {
                                    Text(
                                        text = "Nenhuma imagem disponível desse local",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        textAlign = TextAlign.Center,
                                        style = TextStyle(
                                            lineHeight = 16.sp
                                        ),
                                        modifier = Modifier
                                            .width(imageWidth)
                                    )
                                }
                                if (!allImagesLoaded) {
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
                                            strokeWidth = 5.dp
                                        )
                                    }
                                }
                                Card(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .width(imageWidth)
                                        .clip(RoundedCornerShape(24.dp))
                                        .clickable {
                                            launcher.launch(
                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                            )
                                        }
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
                                                .size(40.dp)
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
                    Spacer(Modifier.height(6.dp))
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
                            .border(
                                1.25.dp,
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                RoundedCornerShape(24.dp)
                            )
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (isUserCommented) {
                                    Text(
                                        text = "Sua avaliação:",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        fontFamily = FontFamily.SansSerif,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.weight(1f)
                                    )
                                } else {
                                    Text(
                                        text = "Qual o nível de acessibilidade do local?",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                val userAccessibilityColor by animateColorAsState(
                                    userAcessibilityRate.toFloat().coerceAtLeast(1f).toColor()
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
                                                        userAccessibilityColor
                                                            .copy(
                                                                alpha = if (isUserCommented) 0.4f else 1f
                                                            )
                                                    )
                                                } else Modifier
                                            )
                                            .border(
                                                1.25.dp,
                                                MaterialTheme.colorScheme.onSurface.copy(alpha = if (isUserCommented) 0.4f else 0.8f),
                                                CircleShape
                                            )
                                            .clickable {
                                                if (isUserCommented) return@clickable
                                                userAcessibilityRate = it
                                            }
                                    )
                                }
                            }
                            val onSend = {
                                trySendComment = true
                                updatedLocalMarker.comments =
                                    updatedLocalMarker.comments?.filter { it.name != userName }
                                if (userComment.isNotEmpty() && userAcessibilityRate != 0) {
                                    updatedLocalMarker.comments =
                                        updatedLocalMarker.comments?.plus(
                                            Comment(
                                                postDate = System.currentTimeMillis()
                                                    .toString(),
                                                id = updatedLocalMarker.comments?.size?.plus(
                                                    1
                                                ) ?: 1,
                                                name = userName,
                                                body = userComment,
                                                email = "",
                                                accessibilityRate = userAcessibilityRate,
                                            )
                                        )
                                    trySendComment = false
                                    isUserCommented = true
                                }
                            }
                            if (!isUserCommented) {
                                TextField(
                                    value = userComment,
                                    onValueChange = {
                                        userComment = it
                                        trySendComment = false
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
                                                onSend()
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.Send,
                                                contentDescription = null
                                            )
                                        }
                                    },
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Text,
                                        capitalization = KeyboardCapitalization.Sentences,
                                        autoCorrectEnabled = true,
                                        imeAction = ImeAction.Send
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onSend = {
                                            onSend()
                                        }
                                    ),
                                    isError =
                                    (userAcessibilityRate == 0 || userComment.isEmpty()) && trySendComment,
                                )
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = userComment,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Normal,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = {
                                            isUserCommented = false
                                            scope.launch {
                                                async { bottomSheetScaffoldState.expand() }.await()
                                                focusRequester.requestFocus()
                                            }
                                        },
                                        modifier = Modifier
                                            .size(35.dp)
                                            .clip(CircleShape)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Edit,
                                            contentDescription = "Edit"
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            userComment = ""
                                            trySendComment = false
                                            userAcessibilityRate = 0
                                            isUserCommented = false
                                            updatedLocalMarker.comments =
                                                updatedLocalMarker.comments?.filter { it.name != userName }
                                        },
                                        modifier = Modifier
                                            .size(35.dp)
                                            .clip(CircleShape)
                                    ) {
                                        Icon(
                                            imageVector = Icons.TwoTone.Delete,
                                            contentDescription = null
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
                            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(30.dp))
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth()
                        ) {
                            updatedLocalMarker.comments?.forEachIndexed { index, comment ->
                                if (comment.name == userName) return@forEachIndexed
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
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
                                                    MaterialTheme.colorScheme.onSurface.copy(
                                                        alpha = 0.8f
                                                    ),
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
                                if (index != (updatedLocalMarker.comments!!.size - 1).plus(if (isUserCommented) -1 else 0)) {
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
