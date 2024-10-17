package com.rafael.inclusimap.feature.map.presentation

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.ArrowOutward
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.SentimentVeryDissatisfied
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rafael.inclusimap.core.domain.model.toCategoryName
import com.rafael.inclusimap.core.domain.model.util.formatDate
import com.rafael.inclusimap.core.domain.model.util.removeTime
import com.rafael.inclusimap.core.domain.model.util.toColor
import com.rafael.inclusimap.feature.map.domain.InclusiMapEvent
import com.rafael.inclusimap.feature.map.domain.InclusiMapState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContributionsScreen(
    state: InclusiMapState,
    onEvent: (InclusiMapEvent) -> Unit,
    onPopBackStack: () -> Unit,
    userEmail: String,
    userName: String,
    userPicture: ImageBitmap?,
    modifier: Modifier = Modifier,
) {
    val latestOnEvent by rememberUpdatedState(onEvent)

    LaunchedEffect(Unit) {
        if (!state.allContributionsLoaded) {
            latestOnEvent(InclusiMapEvent.LoadUserContributions(userEmail))
        }
    }

    Column(
        modifier = modifier
            .statusBarsPadding()
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Top),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CenterAlignedTopAppBar(
            title = {
                Text(
                    text = "Contribuições",
                    fontSize = 26.sp,
                    textAlign = TextAlign.Center,
                )
            },
            navigationIcon = {
                IconButton(
                    onClick = onPopBackStack,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                    )
                }
            },
        )
        if (!state.allContributionsLoaded) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.Top,
            ) {
                CircularProgressIndicator(
                    strokeCap = StrokeCap.Round,
                )
            }
        }
        if (state.userContributions.places.isEmpty() && state.userContributions.comments.isEmpty() && state.userContributions.images.isEmpty() && state.allContributionsLoaded) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Nenhuma contribuição encontrada",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Icon(
                    imageVector = Icons.Outlined.SentimentVeryDissatisfied,
                    contentDescription = null,
                    modifier = Modifier.size(60.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (state.userContributions.places.isNotEmpty()) {
                item {
                    Text(
                        text = "Locais adicionados",
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
                            .height(((state.userContributions.places.size.coerceAtLeast(2) / 2) * 140).dp),
                        horizontalArrangement = Arrangement.Center,
                        userScrollEnabled = false,
                    ) {
                        state.userContributions.places.forEachIndexed { index, place ->
                            item {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier
                                        .padding(vertical = 6.dp)
                                        .padding(end = if (index == 0 || index % 2 == 0) 8.dp else 0.dp)
                                        .height(130.dp)
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
                                    ) {
                                        Text(
                                            text = place.title,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                        Text(
                                            text = place.category?.toCategoryName()?.uppercase() ?: "",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Normal,
                                            color = MaterialTheme.colorScheme.onSurface,
                                        )
                                        Text(
                                            text = "Adicionado em: ${
                                                place.time.removeTime()?.formatDate() ?: "Unknown"
                                            }",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Normal,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                        )
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.End),
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth(),
                                        ) {
                                            IconButton(
                                                onClick = {
                                                },
                                                modifier = Modifier.size(35.dp),
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
            }
            if (state.userContributions.comments.isNotEmpty()) {
                item {
                    Text(
                        text = "Comentários adicionados",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            state.userContributions.comments.forEach { comment ->
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .animateItem()
                            .clip(MaterialTheme.shapes.medium)
                            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp))
                            .padding(12.dp),

                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                        ) {
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
                                            .size(40.dp)
                                            .clip(CircleShape),
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Outlined.Person,
                                        contentDescription = null,
                                        modifier = Modifier.size(40.dp),
                                    )
                                }
                                Text(
                                    text = userName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    text = comment.postDate.removeTime()?.formatDate()
                                        ?: "",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                                    modifier = Modifier.weight(1f),
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clip(CircleShape)
                                        .background(
                                            color = comment.accessibilityRate
                                                .toFloat()
                                                .toColor(),
                                        ),

                                )
                            }
                            Text(
                                text = comment.body,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
            }
            if (state.userContributions.images.isNotEmpty()) {
                item {
                    Text(
                        text = "Imagens adicionadas",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            state.userContributions.images.forEach { image ->
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateItem()
                            .clip(MaterialTheme.shapes.medium)
                            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp))
                            .padding(vertical = 8.dp, horizontal = 12.dp),
                    ) {
                        Column {
                            Text(
                                text = "Local: " + image.place.title,
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1,
                            )
                            Text(
                                text = "Postada em: " + image.place.time.removeTime()?.formatDate(),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Image(
                            bitmap = image.placeImage.image,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .height(100.dp)
                                .aspectRatio(image.placeImage.image.width / image.placeImage.image.height.toFloat())
                                .clip(RoundedCornerShape(8.dp)),
                        )
                    }
                }
            }
        }
    }

    BackHandler {
        onPopBackStack()
    }
}
