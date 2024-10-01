package com.rafael.inclusimap.feature.about.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImagePainter
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.SubcomposeAsyncImageContent
import com.rafael.inclusimap.core.resources.R
import com.rafael.inclusimap.core.resources.icons.Github
import com.rafael.inclusimap.feature.about.BuildConfig
import com.rafael.inclusimap.feature.about.domain.Author

@Composable
fun AboutAppScreen(
    onPopBackStack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .displayCutoutPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        IconButton(
            onClick = onPopBackStack,
            modifier = Modifier
                .align(Alignment.Start)
                .size(35.dp),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
            )
        }
        LazyColumn(
            modifier = Modifier
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            item {
                Spacer(modifier = Modifier.height(30.dp))
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = null,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(36.dp)),
                )
            }
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "InclusiMap©",
                    fontSize = 30.sp,
                    fontStyle = FontStyle.Italic,
                )
            }
            item {
                Spacer(modifier = Modifier.height(50.dp))
                Text(
                    "Autores:",
                    fontSize = 16.sp,
                    textAlign = TextAlign.Start,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                )
            }
            Author.authors.forEach { author ->
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(10.dp)),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(12.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.Start,
                        ) {
                            Text(
                                text = author.name,
                                fontSize = 18.sp,
                                modifier = Modifier,
                            )
                            Text(
                                text = author.position,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier,
                            )
                        }
                        IconButton(
                            onClick = {
                                uriHandler.openUri(author.site)
                            },
                            modifier = Modifier
                                .size(35.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Github,
                                contentDescription = null,
                            )
                        }
                        SubcomposeAsyncImage(
                            model = author.image,
                            contentScale = ContentScale.Crop,
                            contentDescription = null,
                            modifier = modifier
                                .size(45.dp)
                                .clip(CircleShape),
                        ) {
                            val painterState by painter.state.collectAsStateWithLifecycle()
                            if (painterState is AsyncImagePainter.State.Success) {
                                SubcomposeAsyncImageContent()
                            } else {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .size(30.dp),
                                    strokeCap = StrokeCap.Round,
                                    strokeWidth = 5.dp,
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                    }
                }
            }
        }
        Text(
            text = "v${BuildConfig.VERSION_NAME}",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
