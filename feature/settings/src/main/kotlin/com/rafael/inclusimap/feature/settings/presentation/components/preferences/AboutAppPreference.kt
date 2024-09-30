package com.rafael.inclusimap.feature.settings.presentation.components.preferences

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.outlined.Info
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
//import androidx.lifecycle.compose.collectAsStateWithLifecycle
//import coil3.compose.AsyncImagePainter
//import coil3.compose.SubcomposeAsyncImage
//import coil3.compose.SubcomposeAsyncImageContent
//import com.rafael.tictactoe.core.domain.util.BuildInfo.VERSION_CODE
//import com.rafael.tictactoe.core.domain.util.BuildInfo.VERSION_NAME
//import com.rafael.tictactoe.core.resources.Res
//import com.rafael.tictactoe.core.resources.about_app
//import com.rafael.tictactoe.core.resources.app_version
//import com.rafael.tictactoe.core.resources.creator_message
import com.rafael.inclusimap.core.settings.domain.model.SettingsEvent
import com.rafael.inclusimap.core.settings.domain.model.SettingsState
import com.rafael.inclusimap.feature.settings.presentation.components.templates.IconPreference

@Composable
fun AboutAppPreference(
    onEvent: (SettingsEvent) -> Unit,
    state: SettingsState,
    modifier: Modifier = Modifier,
) {
    IconPreference(
        title = "Sobre o InclusiMap©",
        description = null,
        leadingIcon = Icons.Outlined.Info,
//        extraContent = if (state.isAboutShown) {
//            {
//                SubcomposeAsyncImage(
//                    model = "https://avatars.githubusercontent.com/u/93414086?s=96&v=4",
//                    contentScale = ContentScale.Crop,
//                    contentDescription = null,
//                    modifier = modifier
//                        .size(45.dp)
//                        .clip(CircleShape),
//                ) {
//                    val painterState by painter.state.collectAsStateWithLifecycle()
//                    if (painterState is AsyncImagePainter.State.Success) {
//                        SubcomposeAsyncImageContent()
//                    } else {
//                        CircularProgressIndicator(
//                            modifier = Modifier
//                                .size(30.dp),
//                            strokeCap = StrokeCap.Round,
//                            strokeWidth = 5.dp,
//                        )
//                    }
//                }
//            }
//        } else {
//            null
//        },
        trailingIcon = if (state.isAboutShown) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
        onClick = {
            onEvent(SettingsEvent.ShowAboutAppCard(!state.isAboutShown))
        },
    )
}
