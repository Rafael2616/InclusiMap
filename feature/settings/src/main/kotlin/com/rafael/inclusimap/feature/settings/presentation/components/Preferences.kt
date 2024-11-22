package com.rafael.inclusimap.feature.settings.presentation.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.rafael.inclusimap.core.settings.domain.model.SettingsEvent
import com.rafael.inclusimap.core.settings.domain.model.SettingsState
import com.rafael.inclusimap.feature.settings.presentation.components.groups.AccountPreferenceGroup
import com.rafael.inclusimap.feature.settings.presentation.components.groups.MapPreferenceGroup
import com.rafael.inclusimap.feature.settings.presentation.components.groups.MapSearchPreferenceGroup
import com.rafael.inclusimap.feature.settings.presentation.components.groups.OthersPreferenceGroup
import com.rafael.inclusimap.feature.settings.presentation.components.groups.ThemePreferenceGroup

@Composable
internal fun Preferences(
    innerPadding: PaddingValues,
    state: SettingsState,
    onEvent: (SettingsEvent) -> Unit,
    onAppIntroEvent: (Boolean) -> Unit,
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    val lazyState = rememberLazyListState()

    LazyColumn(
        state = lazyState,
        modifier = modifier
            .fillMaxSize()
            .consumeWindowInsets(innerPadding)
            .animateContentSize(),
        contentPadding = innerPadding,
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item {
            AccountPreferenceGroup(onEvent, navController)
        }
        item {
            ThemePreferenceGroup(onEvent, state)
        }
        item {
            MapPreferenceGroup(onEvent, state)
        }
        item {
            MapSearchPreferenceGroup(onEvent, state)
        }
        item {
            OthersPreferenceGroup(onAppIntroEvent, navController)
        }
    }
}
