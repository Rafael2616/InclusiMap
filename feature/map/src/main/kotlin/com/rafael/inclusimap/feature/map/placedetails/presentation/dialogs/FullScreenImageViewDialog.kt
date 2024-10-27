package com.rafael.inclusimap.feature.map.placedetails.presentation.dialogs

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.CarouselDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
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
    var currentImageIndex by remember { mutableIntStateOf(index) }
    val scope = rememberCoroutineScope()
    var isImmersiveMode by remember { mutableStateOf(false) }

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
                    .navigationBarsPadding()
                    .background(MaterialTheme.colorScheme.surface),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onTap = { isImmersiveMode = !isImmersiveMode },
                            )
                        },
                    contentAlignment = Alignment.TopCenter,
                ) {
                    val zoomState = rememberZoomState()
                    HorizontalUncontainedCarousel(
                        state = state,
                        itemWidth = (0.85 * width).dp,
                        itemSpacing = 10.dp,
                        modifier = Modifier
                            .fillMaxSize(),
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
                                        .zoomable(
                                            zoomState = zoomState,
                                            onTap = {},
                                        )
                                )
                            }
                        }
                    }
                    if (!isImmersiveMode) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .padding(top = 4.dp)
                                .align(Alignment.TopCenter)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Black.copy(alpha = 0.35f))
                                .padding(horizontal = 6.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            IconButton(
                                onClick = { onDismiss() },
                                modifier = Modifier.size(45.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = null,
                                )
                            }
                            Text(
                                text = placeName,
                                fontSize = 20.sp,
                                color = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.fillMaxWidth(0.85f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Spacer(Modifier.weight(1f))
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 10.dp, start = 6.dp),
                            contentAlignment = Alignment.BottomStart,
                        ) {
                            Row(
                                modifier = Modifier
                                    .wrapContentSize()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color.Black.copy(alpha = 0.4f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                                horizontalArrangement = Arrangement.Center,
                            ) {
                                Text(
                                    text = "Publicada em: ${images[currentImageIndex]?.name?.extractImageDate() ?: "Sem dados"}",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.7f),
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
