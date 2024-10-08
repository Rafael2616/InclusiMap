package com.rafael.inclusimap.core.navigation.impl

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
        val context = LocalContext.current

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
                            appIntroViewModel::setShowAppIntro,
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
                            userName = loginState.user?.name ?: "",
                            userEmail = loginState.user?.email ?: "",
                            onReport = {
                                reportViewModel.onReport(it, context)
                            },
                        )
                    }
                    composable<Destination.SettingsScreen> {
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
                }
            }
        }

        SingletonCoilImageLoader()

        LaunchedEffect(loginState.isLoggedIn, appIntroState.showAppIntro) {
            if (loginState.isLoggedIn && appIntroState.showAppIntro) {
                navController.navigate(Destination.MapScreen)
            }
        }
    }
}
