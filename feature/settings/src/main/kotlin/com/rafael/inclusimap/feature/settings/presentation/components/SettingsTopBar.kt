package com.rafael.inclusimap.feature.settings.presentation.components

import androidx.compose.foundation.Image
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.rafael.inclusimap.core.settings.domain.model.SettingsEvent
import com.rafael.inclusimap.core.settings.domain.model.SettingsState

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun SettingsTopBar(
    navController: NavController,
    state: SettingsState,
    onEvent: (SettingsEvent) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (PaddingValues) -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    var isGoBackClicked by rememberSaveable { mutableStateOf(false) }

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
                        onClick = {
                            if (!isGoBackClicked) {
                                isGoBackClicked = true
                                onEvent(SettingsEvent.ShowAboutAppCard(false))
                                navController.popBackStack()
                            }
                        },
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go Back",
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
                actions = {
                    IconButton(
                        onClick = {
                            onEvent(SettingsEvent.ShowProfilePictureSettings(true))
                        },
                    ) {
                        state.profilePicture?.let { image ->
                            Image(
                                bitmap = image,
                                contentDescription = "Profile picture",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(45.dp)
                                    .clip(CircleShape),
                            )
                        }
                        if (state.profilePicture == null) {
                            Icon(
                                imageVector = Icons.Outlined.Person,
                                contentDescription = "No profile picture",
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }
                }
            )
        },
    ) { innerPadding ->
        content(innerPadding)
    }
}
