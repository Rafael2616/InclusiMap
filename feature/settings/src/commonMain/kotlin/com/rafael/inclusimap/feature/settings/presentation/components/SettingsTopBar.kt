package com.rafael.inclusimap.feature.settings.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.rafael.inclusimap.core.ui.components.OverlayText
import com.rafael.inclusimap.feature.settings.domain.model.SettingsEvent
import com.rafael.inclusimap.feature.settings.domain.model.SettingsState
import com.svenjacobs.reveal.Reveal
import com.svenjacobs.reveal.RevealCanvasState
import com.svenjacobs.reveal.RevealOverlayArrangement
import com.svenjacobs.reveal.RevealShape
import com.svenjacobs.reveal.rememberRevealState
import com.svenjacobs.reveal.shapes.balloon.Arrow
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun SettingsTopBar(
    onNavigateBack: () -> Unit,
    userProfilePicture: ByteArray?,
    state: SettingsState,
    onEvent: (SettingsEvent) -> Unit,
    revealCanvasState: RevealCanvasState,
    showFirstTimeAnimation: Boolean?,
    modifier: Modifier = Modifier,
    content: @Composable (PaddingValues) -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val revealState = rememberRevealState()
    val scope = rememberCoroutineScope()

    Reveal(
        revealCanvasState = revealCanvasState,
        revealState = revealState,
        onRevealableClick = {
            onEvent(SettingsEvent.ShowProfilePictureSettings(true))
            onEvent(SettingsEvent.SetIsProfileSettingsTipShown(true))
            scope.launch {
                revealState.hide()
            }
        },
        onOverlayClick = {
            onEvent(SettingsEvent.SetIsProfileSettingsTipShown(true))
            scope.launch {
                revealState.hide()
            }
        },
        overlayContent = { key ->
            when (key) {
                RevealKeys.PROFILE_SETTINGS -> OverlayText(
                    text = "Clique para editar a foto de perfil e mais...",
                    arrow = Arrow.end(),
                    modifier = Modifier.align(
                        horizontalArrangement = RevealOverlayArrangement.Start,
                    ),
                )
            }
        },
    ) {
        Scaffold(
            modifier = modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                LargeTopAppBar(
                    title = {
                        Text("Configurações")
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onNavigateBack,
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Go Back",
                            )
                        }
                    },
                    scrollBehavior = scrollBehavior,
                    actions = {
                        Box {
                            IconButton(
                                modifier = Modifier
                                    .size(40.dp)
                                    .revealable(RevealKeys.PROFILE_SETTINGS, RevealShape.Circle, PaddingValues(6.dp)),
                                onClick = {
                                    onEvent(SettingsEvent.ShowProfilePictureSettings(true))
                                },
                            ) {
                                userProfilePicture?.let { image ->
                                    AsyncImage(
                                        model = image,
                                        contentDescription = "Profile picture",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(45.dp)
                                            .clip(CircleShape),
                                    )
                                }
                                if (userProfilePicture == null) {
                                    Icon(
                                        imageVector = Icons.Outlined.Person,
                                        contentDescription = "No profile picture",
                                        modifier = Modifier.size(30.dp),
                                    )
                                }
                            }
                        }
                    },
                )
            },
        ) { innerPadding ->
            content(innerPadding)
        }
    }

    DisposableEffect(userProfilePicture) {
        onDispose {
            scope.launch {
                revealState.hide()
            }
        }
    }

    LaunchedEffect(state.isProfileSettingsTipShown) {
        if (showFirstTimeAnimation == true) return@LaunchedEffect
        if (!state.isProfileSettingsTipShown) {
            delay(1.5.seconds)
            revealState.reveal(RevealKeys.PROFILE_SETTINGS)
        }
    }
}

enum class RevealKeys {
    PROFILE_SETTINGS,
}
