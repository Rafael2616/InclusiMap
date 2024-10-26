package com.rafael.inclusimap.feature.map.placedetails.presentation.dialogs

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.ViewCarousel
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.CarouselDefaults
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.HorizontalUncontainedCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.rafael.inclusimap.core.domain.model.PlaceImage
import com.rafael.inclusimap.core.domain.model.util.extractImageDate
import kotlinx.coroutines.launch
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullScreenImageViewDialog(
    placeName: String,
    images: List<PlaceImage?>,
    index: Int,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state = rememberCarouselState(
        initialItem = index,
        itemCount = { images.size },
    )
    val width = LocalView.current.width
    var isMultiBrowserView by remember { mutableStateOf(false) }
    var currentImageIndex by remember { mutableIntStateOf(index) }
    val scope = rememberCoroutineScope()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
        ),
    ) {
        Card(
            modifier = modifier
                .fillMaxSize(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(28.dp),
            ) {
                Row(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(45.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                        )
                    }
                    Text(
                        text = "Imagens de $placeName",
                        fontSize = 20.sp,
                        color = LocalContentColor.current.copy(alpha = 0.8f),
                        modifier = Modifier.fillMaxWidth(0.85f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.weight(1f))
                    IconButton(
                        onClick = { isMultiBrowserView = !isMultiBrowserView },
                        modifier = Modifier.size(45.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ViewCarousel,
                            contentDescription = null,
                        )
                    }
                }
                val zoomState = rememberZoomState()
                Column(
                    modifier = Modifier
                        .fillMaxHeight(0.92f)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    if (isMultiBrowserView) {
                        HorizontalMultiBrowseCarousel(
                            state = state,
                            preferredItemWidth = (0.85 * width).dp,
                            itemSpacing = 8.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .navigationBarsPadding(),
                            flingBehavior = CarouselDefaults.singleAdvanceFlingBehavior(state),
                        ) { index ->
                            images[index]?.let { image ->
                                currentImageIndex = index
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Image(
                                        bitmap = image.image,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .aspectRatio(image.image.width / image.image.height.toFloat())
                                            .maskClip(RoundedCornerShape(12.dp)),
                                    )
                                }
                            }
                        }
                    } else {
                        HorizontalUncontainedCarousel(
                            state = state,
                            itemWidth = (0.85 * width).dp,
                            itemSpacing = 10.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .navigationBarsPadding(),
                            flingBehavior = CarouselDefaults.singleAdvanceFlingBehavior(state),
                        ) { index ->
                            images[index]?.let { image ->
                                currentImageIndex = index
                                zoomState.setContentSize(
                                    Size(
                                        width = image.image.width.toFloat(),
                                        height = image.image.height.toFloat(),
                                    ),
                                )
                                if (state.isScrollInProgress) {
                                    scope.launch { zoomState.reset() }
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Image(
                                        bitmap = image.image,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .aspectRatio(image.image.width / image.image.height.toFloat())
                                            .zoomable(zoomState),
                                    )
                                }
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.Top,
                ) {
                    Text(
                        text = "Imagem publicada em: ${images[currentImageIndex]?.name?.extractImageDate() ?: "Sem dados"}",
                        fontSize = 14.sp,
                        color = LocalContentColor.current.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth(),
                    )
                }
            }
        }
    }
}
