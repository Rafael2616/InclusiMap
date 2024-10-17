package com.rafael.inclusimap.core.navigation.impl

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.google.android.gms.location.FusedLocationProviderClient
import com.rafael.inclusimap.core.navigation.Destination
import com.rafael.inclusimap.core.settings.domain.model.SettingsEvent
import com.rafael.inclusimap.core.ui.theme.InclusiMapTheme
import com.rafael.inclusimap.feature.about.presentation.AboutAppScreen
import com.rafael.inclusimap.feature.about.util.SingletonCoilImageLoader
import com.rafael.inclusimap.feature.auth.domain.model.LoginEvent
import com.rafael.inclusimap.feature.auth.presentation.UnifiedLoginScreen
import com.rafael.inclusimap.feature.auth.presentation.viewmodel.LoginViewModel
import com.rafael.inclusimap.feature.intro.presentation.viewmodel.AppIntroViewModel
import com.rafael.inclusimap.feature.libraryinfo.presentation.LibraryScreen
import com.rafael.inclusimap.feature.libraryinfo.presentation.viewmodel.LibraryViewModel
import com.rafael.inclusimap.feature.map.domain.InclusiMapEvent
import com.rafael.inclusimap.feature.map.presentation.ContributionsScreen
import com.rafael.inclusimap.feature.map.presentation.InclusiMapGoogleMapScreen
import com.rafael.inclusimap.feature.map.presentation.viewmodel.InclusiMapGoogleMapViewModel
import com.rafael.inclusimap.feature.map.presentation.viewmodel.PlaceDetailsViewModel
import com.rafael.inclusimap.feature.map.presentation.viewmodel.ReportViewModel
import com.rafael.inclusimap.feature.map.search.presentation.viewmodel.SearchViewModel
import com.rafael.inclusimap.feature.settings.presentation.SettingsScreen
import com.rafael.inclusimap.feature.settings.presentation.viewmodel.SettingsViewModel
import org.koin.compose.KoinContext
import org.koin.compose.viewmodel.koinViewModel
import soup.compose.material.motion.animation.materialSharedAxisXIn
import soup.compose.material.motion.animation.materialSharedAxisXOut
import soup.compose.material.motion.animation.rememberSlideDistance

@Composable
fun InclusiMapNavHost(
    fusedLocationProviderClient: FusedLocationProviderClient,
    modifier: Modifier = Modifier,
) {
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val slideDistance = rememberSlideDistance()
    val navController = rememberNavController()

    KoinContext {
        val appIntroViewModel = koinViewModel<AppIntroViewModel>()
        val appIntroState by appIntroViewModel.state.collectAsStateWithLifecycle()
        val mapViewModel = koinViewModel<InclusiMapGoogleMapViewModel>()
        val mapState by mapViewModel.state.collectAsStateWithLifecycle()
        val placeDetailsViewModel = koinViewModel<PlaceDetailsViewModel>()
        val placeDetailsState by placeDetailsViewModel.state.collectAsStateWithLifecycle()
        val loginViewModel = koinViewModel<LoginViewModel>()
        val loginState by loginViewModel.state.collectAsStateWithLifecycle()
        val searchViewModel = koinViewModel<SearchViewModel>()
        val searchState by searchViewModel.state.collectAsStateWithLifecycle()
        val settingsViewModel = koinViewModel<SettingsViewModel>()
        val settingsState by settingsViewModel.state.collectAsStateWithLifecycle()
        val libraryViewModel = koinViewModel<LibraryViewModel>()
        val ossLibraries by libraryViewModel.ossLibraries.collectAsStateWithLifecycle()
        val reportViewModel = koinViewModel<ReportViewModel>()
        val reportState by reportViewModel.state.collectAsStateWithLifecycle()

        InclusiMapTheme(state = settingsState) {
            Scaffold(
                modifier = modifier
                    .fillMaxSize(),
            ) { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = if (!loginState.isLoggedIn) Destination.LoginScreen(false) else Destination.MapScreen,
                    enterTransition = { materialSharedAxisXIn(!isRtl, slideDistance) },
                    exitTransition = { materialSharedAxisXOut(!isRtl, slideDistance) },
                    popEnterTransition = { materialSharedAxisXIn(isRtl, slideDistance) },
                    popExitTransition = { materialSharedAxisXOut(isRtl, slideDistance) },
                ) {
                    composable<Destination.LoginScreen> {
                        UnifiedLoginScreen(
                            loginState = loginState,
                            onLogin = { registeredUser ->
                                loginViewModel.onEvent(
                                    LoginEvent.OnLogin(registeredUser),
                                )
                                appIntroViewModel.setShowAppIntro(true)
                            },
                            onRegister = {
                                loginViewModel.onEvent(
                                    LoginEvent.OnRegisterNewUser(it),
                                )
                                appIntroViewModel.setShowAppIntro(true)
                            },
                            modifier = Modifier.consumeWindowInsets(innerPadding),
                            onUpdatePassword = { newPassword ->
                                loginViewModel.onEvent(
                                    LoginEvent.UpdatePassword(newPassword),
                                )
                            },
                            onCancel = {
                                navController.popBackStack()
                            },
                            onPopBackStack = {
                                navController.popBackStack()
                                loginViewModel.onEvent(
                                    LoginEvent.SetIsPasswordChanged(false),
                                )
                            },
                            isEditPasswordMode = it.toRoute<Destination.LoginScreen>().isEditPasswordMode,
                        )
                    }
                    composable<Destination.MapScreen> {
                        InclusiMapGoogleMapScreen(
                            mapState,
                            mapViewModel::onEvent,
                            placeDetailsState,
                            placeDetailsViewModel::onEvent,
                            appIntroState,
                            onDismissAppIntro = {
                                appIntroViewModel.setShowAppIntro(it)
                                mapViewModel.onEvent(InclusiMapEvent.ShouldAnimateMap(it))
                            },
                            searchState,
                            searchViewModel::onEvent,
                            settingsState,
                            fusedLocationProviderClient,
                            onMapTypeChange = {
                                settingsViewModel.onEvent(
                                    SettingsEvent.SetMapType(it),
                                )
                            },
                            onNavigateToSettings = { navController.navigate(Destination.SettingsScreen) },
                            onNavigateToContributions = { navController.navigate(Destination.ContributionsScreen) },
                            userName = loginState.user?.name ?: "",
                            userEmail = loginState.user?.email ?: "",
                            onReport = {
                                reportViewModel.onReport(it)
                            },
                            reportState = reportState,
                            allowedShowUserProfilePicture = loginViewModel::allowedShowUserProfilePicture,
                            downloadUserProfilePicture = loginViewModel::downloadUserProfilePicture,
                        )
                    }
                    composable<Destination.SettingsScreen> {
                        val isErrorUpdatingUserInfos by remember {
                            derivedStateOf {
                                loginState.isErrorUpdatingUserName && loginState.isErrorUpdatingProfilePicture && loginState.isErrorRemovingProfilePicture && loginState.isErrorAllowingPictureOptedIn
                            }
                        }
                        val isSuccessfullUpdatingUserInfo by remember {
                            derivedStateOf {
                                loginState.isPictureOptedInSuccessfullyChanged && loginState.isUserNameUpdated && loginState.isProfilePictureUpdated && loginState.isProfilePictureRemoved
                            }
                        }
                        SettingsScreen(
                            loginState.isLoginOut,
                            navController,
                            settingsState,
                            settingsViewModel::onEvent,
                            onLogout = {
                                loginViewModel.onEvent(
                                    LoginEvent.OnLogout,
                                )
                            },
                            onDeleteAccount = { keepContributions ->
                                loginViewModel.onEvent(
                                    LoginEvent.DeleteAccount(keepContributions),
                                )
                            },
                            isDeleting = loginState.isDeletingAccount,
                            deleteStep = loginState.deleteStep,
                            networkError = loginState.networkError,
                            userName = loginState.user?.name ?: "",
                            onEditUserName = {
                                loginViewModel.onEvent(
                                    LoginEvent.UpdateUserName(it),
                                )
                            },
                            onAddEditProfilePicture = {
                                settingsViewModel.onEvent(
                                    SettingsEvent.OnAddEditProfilePicture(it),
                                )
                                loginViewModel.onEvent(
                                    LoginEvent.OnAddEditUserProfilePicture(it),
                                )
                            },
                            onRemoveProfilePicture = {
                                settingsViewModel.onEvent(
                                    SettingsEvent.OnRemoveProfilePicture,
                                )
                                loginViewModel.onEvent(
                                    LoginEvent.OnRemoveUserProfilePicture,
                                )
                            },
                            onAllowPictureOptedIn = {
                                loginViewModel.onEvent(
                                    LoginEvent.OnAllowPictureOptedIn(it),
                                )
                            },
                            allowOtherUsersToSeeProfilePicture = loginState.user?.showProfilePictureOptedIn ?: false,
                            isErrorUpdatingUserInfos = isErrorUpdatingUserInfos,
                            isSuccessfulUpdatingUserInfos = isSuccessfullUpdatingUserInfo,
                        )
                        LaunchedEffect(loginState.isLoggedIn) {
                            if (!loginState.isLoggedIn) {
                                settingsViewModel.onEvent(SettingsEvent.ShowLogoutDialog(false))
                                settingsViewModel.onEvent(SettingsEvent.ShowDeleteAccountDialog(false))
                                navController.clearBackStack(Destination.MapScreen)
                            }
                        }
                    }
                    composable<Destination.LibraryScreen> {
                        LibraryScreen(
                            libraries = ossLibraries,
                            popBackStack = {
                                navController.popBackStack()
                            },
                        )
                    }
                    composable<Destination.AboutScreen> {
                        AboutAppScreen(
                            onPopBackStack = {
                                navController.popBackStack()
                            },
                        )
                    }
                    composable<Destination.ContributionsScreen> {
                        ContributionsScreen(
                            state = mapState,
                            onEvent = mapViewModel::onEvent,
                            userEmail = loginState.user?.email ?: "",
                            userName = loginState.user?.name ?: "",
                            userPicture = settingsState.profilePicture,
                            onPopBackStack = {
                                navController.popBackStack()
                                mapViewModel.onEvent(InclusiMapEvent.SetIsContributionsScreen(false))
                            },
                        )
                    }
                }
            }
        }

        SingletonCoilImageLoader()

        LaunchedEffect(loginState.isLoggedIn, appIntroState.showAppIntro) {
            if (loginState.isLoggedIn && appIntroState.showAppIntro) {
                navController.navigate(Destination.MapScreen)
            }
        }

        DisposableEffect(loginState.userProfilePicture) {
            settingsViewModel.onEvent(
                SettingsEvent.OnAddEditProfilePicture(loginState.userProfilePicture),
            )
            onDispose { }
        }
    }
}
