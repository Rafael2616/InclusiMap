package com.rafael.inclusimap.feature.about.presentation

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.Ballot
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
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
import com.rafael.inclusimap.feature.intro.presentation.dialogs.TermsAndConditionsDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutAppScreen(
    showTermsAndConditions: Boolean,
    onPopBackStack: () -> Unit,
    onShowTermsAndConditions: () -> Unit,
    onGoToLicenses: () -> Unit,
    onDismissTermsDialog: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uriHandler = LocalUriHandler.current
    val orientation = LocalConfiguration.current.orientation
    val isLandscape = orientation == Configuration.ORIENTATION_LANDSCAPE
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text("Sobre")
                },
                navigationIcon = {
                    IconButton(
                        onClick = onPopBackStack,
                        modifier = Modifier
                            .size(35.dp),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            item {
                Spacer(modifier = Modifier.height(15.dp))
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = null,
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape),
                )
            }
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "InclusiMap©",
                    fontSize = 28.sp,
                    fontStyle = FontStyle.Italic,
                )
            }
            item {
                Text(
                    text = "v${BuildConfig.VERSION_NAME}",
                    fontSize = 12.sp,
                    lineHeight = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            item {
                Spacer(modifier = Modifier.height(40.dp))
                Text(
                    "Autores:",
                    fontSize = 16.sp,
                    textAlign = TextAlign.Start,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxWidth(if (isLandscape) 0.7f else 1f),
                )
            }
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(if (isLandscape) 0.7f else 1f)
                        .clip(RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp),
                    ),
                ) {
                    Author.authors.forEachIndexed { index, author ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1f),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.Start,
                            ) {
                                Text(
                                    text = author.name,
                                    fontSize = 16.sp,
                                )
                                Text(
                                    text = author.position,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                                modifier = Modifier
                                    .size(40.dp)
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
                                        strokeWidth = 4.dp,
                                    )
                                }
                            }
                        }
                        if (index < Author.authors.size - 1) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.surface,
                                thickness = 4.dp,
                            )
                        }
                    }
                }
            }
            item {
                Text(
                    "Produto:",
                    fontSize = 16.sp,
                    textAlign = TextAlign.Start,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxWidth(if (isLandscape) 0.7f else 1f),
                )
            }
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(if (isLandscape) 0.7f else 1f)
                        .clip(RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp),
                    ),
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        ProductItem(
                            title = "Termos e Condições",
                            leadingIcon = Icons.Outlined.Ballot,
                            trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
                            onClick = onShowTermsAndConditions,
                        )
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.surface,
                            thickness = 4.dp,
                        )
                        ProductItem(
                            title = "Licenças de código aberto",
                            leadingIcon = Icons.Outlined.Code,
                            trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
                            onClick = onGoToLicenses,
                        )
                    }
                }
            }
        }
    }

    AnimatedVisibility(showTermsAndConditions) {
        TermsAndConditionsDialog(
            onDismissRequest = onDismissTermsDialog,
        )
    }
}

@Composable
fun ProductItem(
    leadingIcon: ImageVector,
    trailingIcon: ImageVector,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Icon(
            imageVector = leadingIcon,
            contentDescription = null,
            modifier = Modifier
                .size(30.dp),
        )
        Text(
            text = title,
            fontSize = 16.sp,
            textAlign = TextAlign.Start,
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .weight(1f),
        )
        Icon(
            imageVector = trailingIcon,
            contentDescription = null,
            modifier = Modifier
                .size(30.dp),
        )
    }
}
