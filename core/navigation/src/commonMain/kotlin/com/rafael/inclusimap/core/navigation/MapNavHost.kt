package com.rafael.inclusimap.core.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.rafael.inclusimap.core.navigation.types.locationType
import com.rafael.inclusimap.core.util.map.model.Location
import com.rafael.inclusimap.core.util.permissions.PermissionsViewModel
import com.rafael.inclusimap.feature.auth.domain.model.LoginState
import com.rafael.inclusimap.feature.contributions.presentation.ContributionsScreen
import com.rafael.inclusimap.feature.contributions.presentation.viewmodel.ContributionsViewModel
import com.rafael.inclusimap.feature.intro.domain.model.AppIntroState
import com.rafael.inclusimap.feature.intro.presentation.dialogs.AppIntroDialog
import com.rafael.inclusimap.feature.map.map.domain.model.InclusiMapEvent
import com.rafael.inclusimap.feature.map.map.presentation.InclusiMapGoogleMapScreen
import com.rafael.inclusimap.feature.map.map.presentation.InclusiMapScaffold
import com.rafael.inclusimap.feature.map.map.presentation.viewmodel.InclusiMapGoogleMapViewModel
import com.rafael.inclusimap.feature.map.placedetails.presentation.viewmodel.PlaceDetailsViewModel
import com.rafael.inclusimap.feature.map.search.domain.model.SearchEvent
import com.rafael.inclusimap.feature.map.search.presentation.viewmodel.SearchViewModel
import com.rafael.inclusimap.feature.report.presentation.viewmodel.ReportViewModel
import com.rafael.inclusimap.feature.settings.domain.model.SettingsEvent
import com.rafael.inclusimap.feature.settings.domain.model.SettingsState
import com.svenjacobs.reveal.RevealCanvasState
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory
import kotlin.reflect.typeOf
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
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
    revealCanvasState: RevealCanvasState,
    onTryReconnect: () -> Unit,
    setShowAppIntro: (Boolean) -> Unit,
    allowedShowUserProfilePicture: suspend (String) -> Boolean,
    downloadUserProfilePicture: suspend (String) -> ByteArray?,
    onSetPresentationMode: (Boolean) -> Unit,
    snackBarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    var isFullScreenMode by remember { mutableStateOf(false) }
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
    val factory = rememberPermissionsControllerFactory()
    val permissionsController = factory.createPermissionsController()
    val permissionsViewModel =
        koinViewModel<PermissionsViewModel>(parameters = { parametersOf(permissionsController) })

    BindEffect(permissionsController)

    InclusiMapScaffold(
        searchState = searchState,
        isSearchHistoryEnabled = settingsState.searchHistoryEnabled,
        selectedMapType = settingsState.mapType,
        searchEvent = onSearchEvent,
        state = mapState,
        onMapTypeChange = { onSettingsEvent(SettingsEvent.SetMapType(it)) },
        isFullScreenMode = isFullScreenMode,
        onFullScreenModeChange = { isFullScreenMode = it },
        onNavigateToSettings = { parentNavController.navigate(Destination.SettingsScreen) },
        onNavigateToExplore = { fromContributionScreen ->
            if (fromContributionScreen) {
                onMapEvent(InclusiMapEvent.SetIsContributionsScreen(false))
                navController.navigateUp()
            }
            onSearchEvent(SearchEvent.OnSearch("", emptyList()))
        },
        onTravelToPlace = { onMapEvent(InclusiMapEvent.OnTravelToPlace(it)) },
        onNavigateToContributions = {
            onSearchEvent(SearchEvent.OnSearch("", emptyList()))
            if (!mapState.isContributionsScreen) {
                navController.navigate(Destination.ContributionsScreen)
                onMapEvent(InclusiMapEvent.SetIsContributionsScreen(true))
            }
        },
        userProfilePicture = loginState.userProfilePicture,
        modifier = modifier,
    ) { paddingValues, isFullScreen ->
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
                    onDismissAppIntro = { showAppIntro ->
                        setShowAppIntro(showAppIntro)
                        onMapEvent(InclusiMapEvent.ShouldAnimateMap(showAppIntro))
                    },
                    onUpdateSearchHistory = { placeId ->
                        onSearchEvent(SearchEvent.UpdateHistory(placeId))
                    },
                    onTryReconnect = onTryReconnect,
                    isServerAvailable = loginState.isServerAvailable,
                    isCheckingServerAvailability = loginState.isCheckingServerAvailability,
                    userName = loginState.user?.name ?: "",
                    userEmail = loginState.user?.email ?: "",
                    isPresentationMode = loginState.user?.showFirstTimeAnimation == true,
                    userProfilePicture = loginState.userProfilePicture,
                    isFullScreenMode = isFullScreen,
                    onReport = onReport,
                    reportState = reportState,
                    location = args.location,
                    allowedShowUserProfilePicture = allowedShowUserProfilePicture,
                    downloadUserProfilePicture = downloadUserProfilePicture,
                    onSetPresentationMode = onSetPresentationMode,
                    isFollowingSystemOn = settingsState.isFollowingSystemOn,
                    isDarkThemeOn = settingsState.isDarkThemeOn,
                    selectedMapType = settingsState.mapType,
                    showAppIntro = appIntroState.showAppIntro,
                    revealCanvasState = revealCanvasState,
                    snackbarHostState = snackBarHostState,
                    requestLocationPermission = permissionsViewModel::provideOrRequestLocationPermission,
                    appIntroDialog = @Composable { name, onDismiss ->
                        AppIntroDialog(
                            onDismiss = onDismiss,
                            userName = name,
                        )
                    },
                )
            }
            composable<Destination.ContributionsScreen> {
                ContributionsScreen(
                    state = contributionState,
                    onEvent = onContributionEvent,
                    userName = loginState.user?.name ?: "",
                    userPicture = loginState.userProfilePicture,
                    onGoToMap = {
                        navController.navigate(Destination.MapScreen(it))
                    },
                    onNavigateBack = {
                        onMapEvent(InclusiMapEvent.SetIsContributionsScreen(false))
                        navController.navigateUp()
                    },
                    snackBarHostState = snackBarHostState,
                    modifier = Modifier.padding(PaddingValues(bottom = paddingValues.calculateBottomPadding())),
                )
            }
        }
    }
}
