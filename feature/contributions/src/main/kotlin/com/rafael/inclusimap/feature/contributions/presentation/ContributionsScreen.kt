package com.rafael.inclusimap.feature.contributions.presentation

import android.content.res.Configuration
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Comment
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Accessibility
import androidx.compose.material.icons.outlined.ArrowOutward
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieClipSpec
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.rafael.inclusimap.core.domain.model.icon
import com.rafael.inclusimap.core.domain.model.toCategoryName
import com.rafael.inclusimap.core.domain.util.formatDate
import com.rafael.inclusimap.core.domain.util.removeTime
import com.rafael.inclusimap.core.domain.util.toColor
import com.rafael.inclusimap.core.navigation.Destination
import com.rafael.inclusimap.core.navigation.types.Location
import com.rafael.inclusimap.core.resources.R
import com.rafael.inclusimap.feature.contributions.domain.ContributionsState
import com.rafael.inclusimap.feature.contributions.domain.model.ContributionItem
import com.rafael.inclusimap.feature.contributions.domain.model.ContributionType
import com.rafael.inclusimap.feature.contributions.domain.model.ContributionsEvent
import com.rafael.inclusimap.feature.contributions.presentation.dialogs.ContributionsHelpDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContributionsScreen(
    state: ContributionsState,
    onEvent: (ContributionsEvent) -> Unit,
    userName: String,
    userPicture: ImageBitmap?,
    navController: NavController,
    onPopBackStack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val latestOnEvent by rememberUpdatedState(onEvent)
    val refreshState = rememberPullToRefreshState()
    val isRefreshing by remember(state.isLoadingContributions) { mutableStateOf(state.isLoadingContributions) }
    var shouldRefresh by remember(state.shouldRefresh) { mutableStateOf(state.shouldRefresh) }
    val context = LocalContext.current
    var showHelpDialog by remember { mutableStateOf(false) }
    val orientation = LocalConfiguration.current.orientation
    val isLandscape = orientation == Configuration.ORIENTATION_LANDSCAPE

    LaunchedEffect(Unit) {
        latestOnEvent(ContributionsEvent.LoadUserContributions)
    }

    var selectedButton by remember { mutableStateOf(ContributionType.PLACE) }
    val buttons = listOf(
        ContributionItem(
            icon = Icons.Outlined.Place,
            name = "Perfil",
            type = ContributionType.PLACE,
            quantity = state.contributionsSize.places,
        ),
        ContributionItem(
            icon = Icons.AutoMirrored.Outlined.Comment,
            name = "Comentários",
            type = ContributionType.COMMENT,
            quantity = state.contributionsSize.comments,
        ),
        ContributionItem(
            icon = Icons.Outlined.Accessibility,
            name = "Recursos de acessibilidade",
            type = ContributionType.ACCESSIBLE_RESOURCES,
            quantity = state.contributionsSize.resources,
        ),
        ContributionItem(
            icon = Icons.Outlined.Image,
            name = "Locais",
            type = ContributionType.IMAGE,
            quantity = state.contributionsSize.images,
        ),
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(top = 8.dp, bottom = 8.dp)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Top),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CenterAlignedTopAppBar(
            title = {
                Text(
                    text = "Contribuições",
                    fontSize = 28.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.height(if (isLandscape) 30.dp else 40.dp),
                )
            },
            actions = {
                IconButton(
                    onClick = { showHelpDialog = true },
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                        contentDescription = null,
                        modifier = Modifier.size(30.dp),
                    )
                }
            },
            expandedHeight = 60.dp,
        )
        PullToRefreshBox(
            state = refreshState,
            isRefreshing = isRefreshing && shouldRefresh,
            onRefresh = {
                shouldRefresh = true
                latestOnEvent(ContributionsEvent.LoadUserContributions)
            },
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Top),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier
                        .width(320.dp)
                        .height(if (isLandscape) 35.dp else 45.dp),
                ) {
                    buttons.forEachIndexed { index, button ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = buttons.size,
                            ),
                            onClick = {
                                selectedButton = button.type
                            },
                            selected = button.type == selectedButton,
                            icon = { },
                            label = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Icon(
                                        imageVector = button.icon,
                                        contentDescription = null,
                                        modifier = Modifier.size(25.dp),
                                    )
                                    Text(
                                        text = "(${button.quantity})",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                }
                            },
                        )
                    }
                }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .animateContentSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    when (selectedButton) {
                        ContributionType.PLACE -> {
                            if (state.userContributions.places.isNotEmpty()) {
                                item {
                                    Text(
                                        text = "Seus locais",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                }
                                item {
                                    LazyVerticalGrid(
                                        columns = GridCells.Fixed(if (state.userContributions.places.size in 0..1) 1 else 2),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .animateItem()
                                            .animateContentSize()
                                            .height(
                                                (
                                                    (
                                                        state.userContributions.places.size.coerceAtLeast(
                                                            2,
                                                        ) / 2 + if (state.userContributions.places.size % 2 == 0) 0 else 1
                                                        ) * if (state.userContributions.places.size == 1) 155 else 170
                                                    ).dp,
                                            ),
                                        horizontalArrangement = Arrangement.Center,
                                        userScrollEnabled = false,
                                    ) {
                                        state.userContributions.places.sortedBy {
                                            it.place.time.removeTime()?.formatDate()
                                        }.forEachIndexed { index, place ->
                                            item {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    modifier = Modifier
                                                        .padding(vertical = 6.dp)
                                                        .padding(end = if (index % 2 == 0 && state.userContributions.places.size != 1) 8.dp else 0.dp)
                                                        .height(if (state.userContributions.places.size == 1) 140.dp else 155.dp)
                                                        .fillMaxWidth(if (isLandscape) 0.85f else 1f)
                                                        .clip(MaterialTheme.shapes.medium)
                                                        .background(
                                                            MaterialTheme.colorScheme.surfaceColorAtElevation(
                                                                8.dp,
                                                            ),
                                                        )
                                                        .padding(12.dp)
                                                        .animateContentSize()
                                                        .animateItem(),
                                                ) {
                                                    Column(
                                                        modifier = Modifier.fillMaxSize(),
                                                        verticalArrangement = Arrangement.spacedBy(4.dp),
                                                    ) {
                                                        Text(
                                                            text = place.place.title,
                                                            fontSize = 16.sp,
                                                            fontWeight = FontWeight.SemiBold,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis,
                                                        )
                                                        Row(
                                                            horizontalArrangement = Arrangement.spacedBy(
                                                                4.dp,
                                                                Alignment.Start,
                                                            ),
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            modifier = Modifier.fillMaxWidth(),
                                                        ) {
                                                            Text(
                                                                text = place.place.category?.toCategoryName()
                                                                    ?.uppercase()
                                                                    ?: "",
                                                                fontSize = 12.sp,
                                                                lineHeight = 18.sp,
                                                                fontWeight = FontWeight.Normal,
                                                                color = MaterialTheme.colorScheme.onSurface,
                                                            )
                                                            place.place.category?.let {
                                                                Icon(
                                                                    imageVector = it.icon(),
                                                                    contentDescription = null,
                                                                    modifier = Modifier.size(20.dp),
                                                                    tint = MaterialTheme.colorScheme.primary,
                                                                )
                                                            }
                                                        }
                                                        Text(
                                                            text = place.place.address + " - " + place.place.locatedIn,
                                                            fontSize = 14.sp,
                                                            fontWeight = FontWeight.Normal,
                                                            maxLines = 2,
                                                            lineHeight = 16.sp,
                                                            overflow = TextOverflow.Ellipsis,
                                                        )
                                                        Row(
                                                            horizontalArrangement = Arrangement.spacedBy(
                                                                4.dp,
                                                                Alignment.Start,
                                                            ),
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            modifier = Modifier.fillMaxWidth(),
                                                        ) {
                                                            val date by remember {
                                                                mutableStateOf(
                                                                    place.place.time.removeTime()
                                                                        ?.formatDate() ?: "Unknown",
                                                                )
                                                            }
                                                            Text(
                                                                text = if (state.userContributions.places.size == 1) {
                                                                    "Adicionado em: $date"
                                                                } else {
                                                                    "Adicionado em:\n$date"
                                                                },
                                                                fontSize = 12.sp,
                                                                lineHeight = 14.sp,
                                                                fontWeight = FontWeight.Normal,
                                                                color = MaterialTheme.colorScheme.onSurface.copy(
                                                                    alpha = 0.8f,
                                                                ),
                                                            )
                                                            Spacer(modifier = Modifier.weight(1f))
                                                            IconButton(
                                                                onClick = {
                                                                    navController.popBackStack()
                                                                    if (place.place.id != null) {
                                                                        navController.navigate(
                                                                            Destination.MapScreen(
                                                                                Location(
                                                                                    place.place.position.first,
                                                                                    place.place.position.second,
                                                                                    place.place.id ?: return@IconButton,
                                                                                ),
                                                                            ),
                                                                        )
                                                                    } else {
                                                                        Toast.makeText(
                                                                            context,
                                                                            "Local não encontrado",
                                                                            Toast.LENGTH_SHORT,
                                                                        ).show()
                                                                    }
                                                                },
                                                                modifier = Modifier
                                                                    .size(35.dp),
                                                            ) {
                                                                Icon(
                                                                    imageVector = Icons.Outlined.ArrowOutward,
                                                                    contentDescription = null,
                                                                    modifier = Modifier.size(30.dp),
                                                                    tint = MaterialTheme.colorScheme.primary,
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                noContributionsFoundedScreen(
                                    message = "Você ainda não adicionou nenhum local",
                                    condition = state.allPlacesContributionsLoaded,
                                )
                            }
                            loadingProgressIndicator(condition = !state.allPlacesContributionsLoaded && !state.errorWhileConnectingToServer)
                            connectionErrorScreen(
                                state = state,
                                condition = !state.allPlacesContributionsLoaded,
                            )
                        }

                        ContributionType.COMMENT -> {
                            if (state.userContributions.comments.isNotEmpty()) {
                                item {
                                    Text(
                                        text = "Seus comentários",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                }
                                state.userContributions.comments.forEach { comment ->
                                    item {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            modifier = Modifier
                                                .animateItem()
                                                .fillMaxWidth(if (isLandscape) 0.85f else 1f)
                                                .clip(MaterialTheme.shapes.medium)
                                                .background(
                                                    MaterialTheme.colorScheme.surfaceColorAtElevation(
                                                        8.dp,
                                                    ),
                                                )
                                                .padding(12.dp),
                                        ) {
                                            Column(
                                                modifier = Modifier.fillMaxWidth(),
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(bottom = 4.dp),
                                                ) {
                                                    Text(
                                                        text = "Em: ",
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                    )
                                                    Text(
                                                        text = comment.place.title,
                                                        fontSize = 14.sp,
                                                    )
                                                }
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                ) {
                                                    if (userPicture != null) {
                                                        Image(
                                                            bitmap = userPicture,
                                                            contentDescription = null,
                                                            contentScale = ContentScale.Crop,
                                                            modifier = Modifier
                                                                .size(35.dp)
                                                                .clip(CircleShape),
                                                        )
                                                    } else {
                                                        Icon(
                                                            imageVector = Icons.Outlined.Person,
                                                            contentDescription = null,
                                                            modifier = Modifier.size(35.dp),
                                                        )
                                                    }
                                                    Text(
                                                        text = userName,
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Bold,
                                                    )
                                                    Text(
                                                        text = comment.comment.postDate.removeTime()
                                                            ?.formatDate()
                                                            ?: "",
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Normal,
                                                        color = MaterialTheme.colorScheme.onSurface.copy(
                                                            alpha = 0.55f,
                                                        ),
                                                        modifier = Modifier.weight(1f),
                                                    )
                                                    Spacer(modifier = Modifier.weight(1f))
                                                    Box(
                                                        modifier = Modifier
                                                            .size(14.dp)
                                                            .clip(CircleShape)
                                                            .background(
                                                                color = comment.comment.accessibilityRate
                                                                    .toFloat()
                                                                    .toColor(),
                                                            ),
                                                    )
                                                }
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                ) {
                                                    Text(
                                                        text = comment.comment.body,
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Normal,
                                                        color = MaterialTheme.colorScheme.onSurface,
                                                        modifier = Modifier.fillMaxWidth(0.8f),
                                                    )
                                                    Spacer(Modifier.weight(1f))
                                                    IconButton(
                                                        onClick = {
                                                            navController.popBackStack()
                                                            navController.navigate(
                                                                Destination.MapScreen(
                                                                    Location(
                                                                        comment.place.position.first,
                                                                        comment.place.position.second,
                                                                        comment.place.id
                                                                            ?: return@IconButton,
                                                                    ),
                                                                ),
                                                            )
                                                        },
                                                        modifier = Modifier
                                                            .size(35.dp),
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Outlined.ArrowOutward,
                                                            contentDescription = null,
                                                            modifier = Modifier.size(30.dp),
                                                            tint = MaterialTheme.colorScheme.primary,
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                noContributionsFoundedScreen(
                                    message = "Você ainda não adicionou nenhum comentário",
                                    condition = state.allCommentsContributionsLoaded,
                                )
                            }
                            loadingProgressIndicator(condition = !state.allCommentsContributionsLoaded && !state.errorWhileConnectingToServer)
                            connectionErrorScreen(
                                state = state,
                                condition = !state.allCommentsContributionsLoaded,
                            )
                        }

                        ContributionType.IMAGE -> {
                            if (state.userContributions.images.isNotEmpty()) {
                                item {
                                    Text(
                                        text = "Suas imagens",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                }
                                state.userContributions.images.groupBy { it.place.id }
                                    .forEach { (_, place) ->
                                        item {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                modifier = Modifier
                                                    .fillMaxWidth(if (isLandscape) 0.85f else 1f)
                                                    .animateItem()
                                                    .clip(MaterialTheme.shapes.medium)
                                                    .background(
                                                        MaterialTheme.colorScheme.surfaceColorAtElevation(
                                                            8.dp,
                                                        ),
                                                    )
                                                    .padding(12.dp),
                                            ) {
                                                Column(
                                                    verticalArrangement = Arrangement.spacedBy(2.dp),
                                                ) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                    ) {
                                                        Text(
                                                            text = "Em: ",
                                                            fontSize = 16.sp,
                                                            fontWeight = FontWeight.SemiBold,
                                                        )
                                                        Text(
                                                            text = place.first().place.title,
                                                            fontSize = 16.sp,
                                                            overflow = TextOverflow.MiddleEllipsis,
                                                            maxLines = 1,
                                                            modifier = Modifier.fillMaxWidth(0.8f),
                                                        )
                                                        Spacer(modifier = Modifier.weight(1f))
                                                        IconButton(
                                                            onClick = {
                                                                navController.popBackStack()
                                                                navController.navigate(
                                                                    Destination.MapScreen(
                                                                        Location(
                                                                            place.first().place.position.first,
                                                                            place.first().place.position.second,
                                                                            place.first().place.id
                                                                                ?: return@IconButton,
                                                                        ),
                                                                    ),
                                                                )
                                                            },
                                                            modifier = Modifier
                                                                .size(35.dp),
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Outlined.ArrowOutward,
                                                                contentDescription = null,
                                                                modifier = Modifier.size(30.dp),
                                                                tint = MaterialTheme.colorScheme.primary,
                                                            )
                                                        }
                                                    }
                                                    place.groupBy { it.date }.forEach { (_, date) ->
                                                        Text(
                                                            text = date.size.toString() + if (date.size == 1) " imagem" else " imagens",
                                                            fontSize = 14.sp,
                                                            fontWeight = FontWeight.Normal,
                                                            color = MaterialTheme.colorScheme.primary,
                                                        )
                                                        Text(
                                                            text = "Postada(s) em: " + date.first().date,
                                                            fontSize = 14.sp,
                                                            fontWeight = FontWeight.Normal,
                                                            color = MaterialTheme.colorScheme.onSurface.copy(
                                                                alpha = 0.8f,
                                                            ),
                                                        )
                                                        LazyHorizontalGrid(
                                                            rows = GridCells.Adaptive(120.dp),
                                                            horizontalArrangement = Arrangement.spacedBy(
                                                                4.dp,
                                                            ),
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .height(140.dp)
                                                                .animateItem(),
                                                        ) {
                                                            date.forEach { image ->
                                                                item(key = image.placeImage.name) {
                                                                    Box(
                                                                        modifier = Modifier
                                                                            .padding(3.dp),
                                                                    ) {
                                                                        Image(
                                                                            bitmap = image.placeImage.image,
                                                                            contentDescription = null,
                                                                            contentScale = ContentScale.Crop,
                                                                            modifier = Modifier
                                                                                .height(130.dp)
                                                                                .aspectRatio(image.placeImage.image.width / image.placeImage.image.height.toFloat())
                                                                                .clip(
                                                                                    RoundedCornerShape(
                                                                                        8.dp,
                                                                                    ),
                                                                                ),
                                                                        )
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                            } else {
                                noContributionsFoundedScreen(
                                    message = "Você ainda não adicionou nenhuma imagem",
                                    condition = state.allImagesContributionsLoaded,
                                )
                            }
                            loadingProgressIndicator(condition = !state.allImagesContributionsLoaded && !state.errorWhileConnectingToServer)
                            connectionErrorScreen(
                                state = state,
                                condition = !state.allImagesContributionsLoaded,
                            )
                        }

                        ContributionType.ACCESSIBLE_RESOURCES -> {
                            if (state.userContributions.resources.isNotEmpty()) {
                                item {
                                    Text(
                                        text = "Seus recursos",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                }
                                item {
                                    Text(
                                        text = "Contribuiu alterando recursos de acessibilidade em:",
                                        fontSize = 14.sp,
                                        lineHeight = 16.sp,
                                    )
                                }
                                val resources =
                                    state.userContributions.resources.groupBy { it.place.id }
                                resources.forEach { (_, place) ->
                                    item {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier
                                                .fillMaxWidth(if (isLandscape) 0.85f else 1f)
                                                .animateItem()
                                                .clip(MaterialTheme.shapes.medium)
                                                .background(
                                                    MaterialTheme.colorScheme.surfaceColorAtElevation(
                                                        8.dp,
                                                    ),
                                                )
                                                .padding(12.dp),
                                        ) {
                                            Column(
                                                verticalArrangement = Arrangement.spacedBy(2.dp),
                                                modifier = Modifier.fillMaxWidth(0.85f),
                                            ) {
                                                Text(
                                                    text = place.first().place.title,
                                                    fontSize = 16.sp,
                                                    overflow = TextOverflow.MiddleEllipsis,
                                                    maxLines = 1,
                                                )
                                                Text(
                                                    text = "Endereço: " + place.first().place.address + " - " + place.first().place.locatedIn,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Normal,
                                                    color = MaterialTheme.colorScheme.onSurface.copy(
                                                        alpha = 0.8f,
                                                    ),
                                                )
                                            }
                                            Spacer(modifier = Modifier.weight(1f))
                                            IconButton(
                                                onClick = {
                                                    navController.popBackStack()
                                                    navController.navigate(
                                                        Destination.MapScreen(
                                                            Location(
                                                                place.first().place.position.first,
                                                                place.first().place.position.second,
                                                                place.first().place.id
                                                                    ?: return@IconButton,
                                                            ),
                                                        ),
                                                    )
                                                },
                                                modifier = Modifier
                                                    .size(35.dp),
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Outlined.ArrowOutward,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(30.dp),
                                                    tint = MaterialTheme.colorScheme.primary,
                                                )
                                            }
                                        }
                                    }
                                }
                            } else {
                                noContributionsFoundedScreen(
                                    message = "Você ainda não modificou nenhum recurso",
                                    condition = state.allResourcesContributionsLoaded,
                                )
                            }
                            loadingProgressIndicator(condition = !state.allResourcesContributionsLoaded && !state.errorWhileConnectingToServer)
                            connectionErrorScreen(
                                state = state,
                                condition = !state.allResourcesContributionsLoaded,
                            )
                        }
                    }
                }
            }
        }
    }

    AnimatedVisibility(showHelpDialog) {
        ContributionsHelpDialog(
            onDismiss = { showHelpDialog = false },
        )
    }

    BackHandler {
        onPopBackStack()
    }
}

fun LazyListScope.noContributionsFoundedScreen(
    message: String,
    condition: Boolean,
    modifier: Modifier = Modifier,
) {
    if (condition) {
        item {
            Column(
                modifier = modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = message,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth(),
                )
                val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.no_contributions_anim))
                val progress by animateLottieCompositionAsState(
                    composition,
                    iterations = 1,
                    clipSpec = LottieClipSpec.Progress(0f, 1f),
                )
                LottieAnimation(
                    composition = composition,
                    progress = { progress },
                    modifier = Modifier.size(320.dp),
                )
            }
        }
    }
}

fun LazyListScope.loadingProgressIndicator(
    condition: Boolean,
) {
    if (condition) {
        item {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.Top,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .animateItem(),
            ) {
                CircularProgressIndicator(
                    strokeCap = StrokeCap.Round,
                )
            }
        }
    }
}

fun LazyListScope.connectionErrorScreen(
    state: ContributionsState,
    condition: Boolean,
    modifier: Modifier = Modifier,
) {
    if (condition && state.errorWhileConnectingToServer) {
        item {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(top = 12.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Verifique sua conexão com a internet e tente novamente!",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth(),
                )
                val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.no_internet_anim))
                val progress by animateLottieCompositionAsState(
                    composition,
                    iterations = 1,
                    clipSpec = LottieClipSpec.Progress(0f, 1f),
                )
                LottieAnimation(
                    composition = composition,
                    progress = { progress },
                    modifier = Modifier.size(250.dp),
                )
            }
        }
    }
}
