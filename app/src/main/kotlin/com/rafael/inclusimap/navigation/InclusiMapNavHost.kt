package com.rafael.inclusimap.navigation

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.google.android.gms.location.FusedLocationProviderClient
import com.rafael.inclusimap.domain.LoginEvent
import com.rafael.inclusimap.settings.domain.model.SettingsEvent
import com.rafael.inclusimap.settings.presentation.SettingsScreen
import com.rafael.inclusimap.settings.presentation.viewmodel.SettingsViewModel
import com.rafael.inclusimap.ui.InclusiMapGoogleMapScreen
import com.rafael.inclusimap.ui.UnifiedLoginScreen
import com.rafael.inclusimap.ui.theme.InclusiMapTheme
import com.rafael.inclusimap.ui.viewmodel.AppIntroViewModel
import com.rafael.inclusimap.ui.viewmodel.InclusiMapGoogleMapScreenViewModel
import com.rafael.inclusimap.ui.viewmodel.LoginViewModel
import com.rafael.inclusimap.ui.viewmodel.PlaceDetailsViewModel
import com.rafael.inclusimap.ui.viewmodel.SearchViewModel
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
    val appIntroViewModel = koinViewModel<AppIntroViewModel>()
    val appIntroState by appIntroViewModel.state.collectAsStateWithLifecycle()
    val mapViewModel = koinViewModel<InclusiMapGoogleMapScreenViewModel>()
    val mapState by mapViewModel.state.collectAsStateWithLifecycle()
    val placeDetailsViewModel = koinViewModel<PlaceDetailsViewModel>()
    val placeDetailsState by placeDetailsViewModel.state.collectAsStateWithLifecycle()
    val loginViewModel = koinViewModel<LoginViewModel>()
    val loginState by loginViewModel.state.collectAsStateWithLifecycle()
    val searchViewModel = koinViewModel<SearchViewModel>()
    val searchState by searchViewModel.state.collectAsStateWithLifecycle()
    val settingsViewModel = koinViewModel<SettingsViewModel>()
    val settingsState by settingsViewModel.state.collectAsStateWithLifecycle()

    InclusiMapTheme(state = settingsState) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = if (!loginState.isLoggedIn) Destination.LoginScreen(false) else Destination.MapScreen,
                enterTransition = { materialSharedAxisXIn(!isRtl, slideDistance) },
                exitTransition = { materialSharedAxisXOut(!isRtl, slideDistance) },
                popEnterTransition = { materialSharedAxisXIn(isRtl, slideDistance) },
                popExitTransition = { materialSharedAxisXOut(isRtl, slideDistance) },
                modifier = modifier
            ) {
                composable<Destination.LoginScreen> {
                    UnifiedLoginScreen(
                        loginState = loginState,
                        onLogin = {
                            loginViewModel.onEvent(
                                LoginEvent.OnLogin(it)
                            )
                            appIntroViewModel.setShowAppIntro(true)
                        },
                        onRegister = {
                            loginViewModel.onEvent(
                                LoginEvent.OnRegisterNewUser(it)
                            )
                            appIntroViewModel.setShowAppIntro(true)
                        },
                        modifier = Modifier.consumeWindowInsets(innerPadding),
                        onUpdatePassword = {
                            loginViewModel.onEvent(
                                LoginEvent.UpdatePassword(it)
                            )
                        },
                        onCancel = {
                            navController.popBackStack()
                        },
                        isEditPasswordMode = it.toRoute<Destination.LoginScreen>().isEditPasswordMode
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
                        loginState,
                        searchState,
                        searchViewModel::onEvent,
                        settingsState,
                        onMapTypeChange = {
                            settingsViewModel.onEvent(
                                SettingsEvent.SetMapType(it)
                            )
                        },
                        fusedLocationProviderClient,
                        onNavigateToSettings = { navController.navigate(Destination.SettingsScreen) },
                        modifier = Modifier.consumeWindowInsets(innerPadding)
                    )
                }
                composable<Destination.SettingsScreen> {
                    SettingsScreen(
                        navController,
                        settingsState,
                        settingsViewModel::onEvent,
                        loginState,
                        onLogout = {
                            loginViewModel.onEvent(
                                LoginEvent.OnLogout
                            )
                        }
                    )
                    LaunchedEffect(loginState.isLoggedIn) {
                        if (!loginState.isLoggedIn) {
                            navController.clearBackStack(Destination.MapScreen)
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(loginState.isLoggedIn, appIntroState.showAppIntro) {
        if (loginState.isLoggedIn && appIntroState.showAppIntro) {
            navController.navigate(Destination.MapScreen)
        }
    }
}