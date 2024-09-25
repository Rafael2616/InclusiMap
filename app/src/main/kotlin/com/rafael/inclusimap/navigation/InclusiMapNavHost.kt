package com.rafael.inclusimap.navigation

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.rafael.inclusimap.ui.InclusiMapGoogleMapScreen
import com.rafael.inclusimap.ui.LoginScreen
import com.rafael.inclusimap.ui.UnifiedLoginScreen
import com.rafael.inclusimap.ui.theme.InclusiMapTheme
import com.rafael.inclusimap.ui.viewmodel.AppIntroViewModel
import com.rafael.inclusimap.ui.viewmodel.InclusiMapGoogleMapScreenViewModel
import com.rafael.inclusimap.ui.viewmodel.PlaceDetailsViewModel
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

    InclusiMapTheme {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = if (appIntroState.isFirstTime) Destination.AppIntroScreen else Destination.MapScreen,
                enterTransition = { materialSharedAxisXIn(!isRtl, slideDistance) },
                exitTransition = { materialSharedAxisXOut(!isRtl, slideDistance) },
                popEnterTransition = { materialSharedAxisXIn(isRtl, slideDistance) },
                popExitTransition = { materialSharedAxisXOut(isRtl, slideDistance) },
                modifier = modifier
            ) {
                composable<Destination.AppIntroScreen> {
                    UnifiedLoginScreen(
                        onLogin = { navController.navigate(Destination.MapScreen) },
                        onRegister = { },
                        onBack = {
                            appIntroViewModel.setIsFirstTime(false)
                            appIntroViewModel.setShowAppIntro(true) },
                        modifier = Modifier.consumeWindowInsets(innerPadding)
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
                        fusedLocationProviderClient,
                        modifier = Modifier.consumeWindowInsets(innerPadding)
                    )
                }
            }
        }
    }
}