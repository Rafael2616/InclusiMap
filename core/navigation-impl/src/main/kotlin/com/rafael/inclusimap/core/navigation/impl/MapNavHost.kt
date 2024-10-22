package com.rafael.inclusimap.core.navigation.impl

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.google.maps.android.compose.rememberCameraPositionState
import com.rafael.inclusimap.core.navigation.Destination
import com.rafael.inclusimap.core.navigation.Location
import com.rafael.inclusimap.core.navigation.impl.types.locationType
import com.rafael.inclusimap.core.settings.domain.model.SettingsEvent
import com.rafael.inclusimap.core.settings.domain.model.SettingsState
import com.rafael.inclusimap.feature.auth.domain.model.LoginState
import com.rafael.inclusimap.feature.contributions.presentation.ContributionsScreen
import com.rafael.inclusimap.feature.contributions.presentation.viewmodel.ContributionsViewModel
import com.rafael.inclusimap.feature.intro.domain.model.AppIntroState
import com.rafael.inclusimap.feature.map.domain.InclusiMapEvent
import com.rafael.inclusimap.feature.map.presentation.InclusiMapGoogleMapScreen
import com.rafael.inclusimap.feature.map.presentation.InclusiMapScaffold
import com.rafael.inclusimap.feature.map.presentation.viewmodel.InclusiMapGoogleMapViewModel
import com.rafael.inclusimap.feature.map.presentation.viewmodel.PlaceDetailsViewModel
import com.rafael.inclusimap.feature.map.presentation.viewmodel.ReportViewModel
import com.rafael.inclusimap.feature.map.search.domain.model.SearchEvent
import com.rafael.inclusimap.feature.map.search.presentation.viewmodel.SearchViewModel
import kotlin.reflect.typeOf
import org.koin.compose.viewmodel.koinViewModel
import soup.compose.material.motion.animation.materialSharedAxisXIn
import soup.compose.material.motion.animation.materialSharedAxisXOut
import soup.compose.material.motion.animation.rememberSlideDistance

@Composable
fun MapNavHost(
    parentNavController: NavController,
    settingsState: SettingsState,
    onSettingsEvent: (SettingsEvent) -> Unit,
    appIntroState: AppIntroState,
    loginState: LoginState,
    setShowAppIntro: (Boolean) -> Unit,
    allowedShowUserProfilePicture: suspend (String) -> Boolean,
    downloadUserProfilePicture: suspend (String) -> ImageBitmap?,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    var isFullScreenMode by remember { mutableStateOf(false) }
    val cameraPositionState = rememberCameraPositionState()
    val placeDetailsViewModel = koinViewModel<PlaceDetailsViewModel>()
    val placeDetailsState by placeDetailsViewModel.state.collectAsStateWithLifecycle()
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val slideDistance = rememberSlideDistance()
    val mapViewModel = koinViewModel<InclusiMapGoogleMapViewModel>()
    val mapState by mapViewModel.state.collectAsStateWithLifecycle()
    val onMapEvent = mapViewModel::onEvent
    val searchViewModel = koinViewModel<SearchViewModel>()
    val searchState by searchViewModel.state.collectAsStateWithLifecycle()
    val onSearchEvent = searchViewModel::onEvent
    val reportViewModel = koinViewModel<ReportViewModel>()
    val reportState by reportViewModel.state.collectAsStateWithLifecycle()
    val onReport = reportViewModel::onReport
    val contributionViewModel = koinViewModel<ContributionsViewModel>()
    val contributionState by contributionViewModel.state.collectAsStateWithLifecycle()
    val onContributionEvent = contributionViewModel::onEvent

    InclusiMapScaffold(
        searchState = searchState,
        settingsState = settingsState,
        searchEvent = onSearchEvent,
        state = mapState,
        onMapTypeChange = {
            onSettingsEvent(
                SettingsEvent.SetMapType(it),
            )
        },
        isFullScreenMode = isFullScreenMode,
        onFullScreenModeChange = {
            isFullScreenMode = it
        },
        onNavigateToSettings = {
            parentNavController.navigate(Destination.SettingsScreen)
        },
        onNavigateToExplore = { fromContributionScreen ->
            if (fromContributionScreen) {
                onMapEvent(InclusiMapEvent.SetIsContributionsScreen(false))
                navController.popBackStack()
            }
            onSearchEvent(SearchEvent.OnSearch("", emptyList()))
        },
        onTravelToPlace = {
            onMapEvent(InclusiMapEvent.OnTravelToPlace(it))
        },
        onNavigateToContributions = {
            onSearchEvent(SearchEvent.OnSearch("", emptyList()))
            if (!mapState.isContributionsScreen) {
                navController.navigate(Destination.ContributionsScreen)
                onMapEvent(InclusiMapEvent.SetIsContributionsScreen(true))
            }
        },
        modifier = modifier,
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Destination.MapScreen(),
            enterTransition = { materialSharedAxisXIn(!isRtl, slideDistance) },
            exitTransition = { materialSharedAxisXOut(!isRtl, slideDistance) },
            popEnterTransition = { materialSharedAxisXIn(isRtl, slideDistance) },
            popExitTransition = { materialSharedAxisXOut(isRtl, slideDistance) },
        ) {
            composable<Destination.MapScreen>(
                typeMap = mapOf(typeOf<Location?>() to locationType),
            ) {
                val args = it.toRoute<Destination.MapScreen>()
                InclusiMapGoogleMapScreen(
                    mapState,
                    onMapEvent,
                    placeDetailsState,
                    placeDetailsViewModel::onEvent,
                    appIntroState,
                    onDismissAppIntro = {
                        setShowAppIntro(it)
                        onMapEvent(InclusiMapEvent.ShouldAnimateMap(it))
                    },
                    settingsState,
                    userName = loginState.user?.name ?: "",
                    userEmail = loginState.user?.email ?: "",
                    onReport = {
                        onReport(it)
                    },
                    reportState = reportState,
                    allowedShowUserProfilePicture = allowedShowUserProfilePicture,
                    downloadUserProfilePicture = downloadUserProfilePicture,
                    cameraPositionState = cameraPositionState,
                    location = args.location,
                )
            }
            composable<Destination.ContributionsScreen> {
                ContributionsScreen(
                    state = contributionState,
                    onEvent = onContributionEvent,
                    userName = loginState.user?.name ?: "",
                    userPicture = settingsState.profilePicture,
                    navController = navController,
                    modifier = Modifier.padding(PaddingValues(bottom = paddingValues.calculateBottomPadding())),
                    onPopBackStack = {
                        onMapEvent(InclusiMapEvent.SetIsContributionsScreen(false))
                        navController.popBackStack()
                    },
                )
            }
        }
    }
}
