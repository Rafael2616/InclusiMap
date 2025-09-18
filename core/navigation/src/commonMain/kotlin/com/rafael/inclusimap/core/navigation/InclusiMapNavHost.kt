package com.rafael.inclusimap.core.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.rafael.inclusimap.core.ui.theme.InclusiMapTheme
import com.rafael.inclusimap.core.util.network.SingletonCoilImageLoader
import com.rafael.inclusimap.feature.about.presentation.AboutAppScreen
import com.rafael.inclusimap.feature.auth.domain.model.LoginEvent
import com.rafael.inclusimap.feature.auth.presentation.UnifiedLoginScreen
import com.rafael.inclusimap.feature.auth.presentation.dialogs.DeleteAccountConfirmationDialog
import com.rafael.inclusimap.feature.auth.presentation.dialogs.LogoutConfirmationDialog
import com.rafael.inclusimap.feature.auth.presentation.dialogs.ProfileSettingsDialog
import com.rafael.inclusimap.feature.auth.presentation.dialogs.UserBannedDialog
import com.rafael.inclusimap.feature.auth.presentation.viewmodel.LoginViewModel
import com.rafael.inclusimap.feature.intro.presentation.dialogs.AppIntroDialog
import com.rafael.inclusimap.feature.intro.presentation.dialogs.TermsAndConditionsDialog
import com.rafael.inclusimap.feature.intro.presentation.viewmodel.AppIntroViewModel
import com.rafael.inclusimap.feature.libraryinfo.presentation.LibraryScreen
import com.rafael.inclusimap.feature.libraryinfo.presentation.viewmodel.LibraryViewModel
import com.rafael.inclusimap.feature.settings.domain.model.SettingsEvent
import com.rafael.inclusimap.feature.settings.presentation.SettingsScreen
import com.rafael.inclusimap.feature.settings.presentation.viewmodel.SettingsViewModel
import com.svenjacobs.reveal.RevealCanvas
import com.svenjacobs.reveal.rememberRevealCanvasState
import org.koin.compose.viewmodel.koinViewModel
import soup.compose.material.motion.animation.materialSharedAxisXIn
import soup.compose.material.motion.animation.materialSharedAxisXOut
import soup.compose.material.motion.animation.rememberSlideDistance

@Composable
fun InclusiMapNavHost(
    modifier: Modifier = Modifier,
) {
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val slideDistance = rememberSlideDistance()
    val navController = rememberNavController()
    val revealCanvasState = rememberRevealCanvasState()

    val appIntroViewModel = koinViewModel<AppIntroViewModel>()
    val appIntroState by appIntroViewModel.state.collectAsStateWithLifecycle()
    val loginViewModel = koinViewModel<LoginViewModel>()
    val loginState by loginViewModel.state.collectAsStateWithLifecycle()
    val settingsViewModel = koinViewModel<SettingsViewModel>()
    val settingsState by settingsViewModel.state.collectAsStateWithLifecycle()
    val snackBarHostState = remember { SnackbarHostState() }

    InclusiMapTheme(
        isDarkThemeOn = settingsState.isDarkThemeOn,
        isFollowingSystemTheme = settingsState.isFollowingSystemOn,
        isDynamicColorOn = settingsState.isDynamicColorsOn,
    ) {
        Scaffold(
            modifier = modifier
                .fillMaxSize(),
            snackbarHost = {
                SnackbarHost(snackBarHostState) {
                    Snackbar(it)
                }
            },
            contentWindowInsets = ScaffoldDefaults.contentWindowInsets.add(WindowInsets.navigationBars),
        ) { innerPadding ->
            RevealCanvas(
                modifier = Modifier.fillMaxSize(),
                revealCanvasState = revealCanvasState,
            ) {
                NavHost(
                    navController = navController,
                    startDestination = if (!loginState.isLoggedIn) Destination.LoginScreen(false) else Destination.MapHost,
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
                            onCancel = navController::popBackStack,
                            onPopBackStack = {
                                navController.popBackStack()
                                loginViewModel.onEvent(
                                    LoginEvent.SetIsPasswordChanged(false),
                                )
                            },
                            onSendRecoverEmail = { email ->
                                loginViewModel.onEvent(
                                    LoginEvent.SendPasswordResetEmail(email),
                                )
                            },
                            onValidateToken = { token ->
                                loginViewModel.onEvent(
                                    LoginEvent.ValidateToken(token),
                                )
                            },
                            onResetUpdateProcess = {
                                loginViewModel.onEvent(LoginEvent.InvalidateUpdatePasswordProcess)
                            },
                            isEditPasswordModeFromSettings = it.toRoute<Destination.LoginScreen>().isEditPasswordMode,
                            termsAndConditionsDialog = { onDismiss ->
                                TermsAndConditionsDialog(onDismiss)
                            },
                        )

                        AnimatedVisibility(loginState.isUserBanned) {
                            UserBannedDialog(
                                onLogin = {
                                    loginViewModel.onEvent(LoginEvent.SetIsBanned(false))
                                },
                            )
                        }
                    }
                    composable<Destination.MapHost> {
                        MapNavHost(
                            parentNavController = navController,
                            settingsState = settingsState,
                            onSettingsEvent = settingsViewModel::onEvent,
                            appIntroState = appIntroState,
                            loginState = loginState,
                            revealCanvasState = revealCanvasState,
                            onTryReconnect = loginViewModel::checkServerIsAvailable,
                            setShowAppIntro = appIntroViewModel::setShowAppIntro,
                            allowedShowUserProfilePicture = loginViewModel::allowedShowUserProfilePicture,
                            downloadUserProfilePicture = loginViewModel::downloadUserProfilePicture,
                            onSetPresentationMode = loginViewModel::onSetPresentationMode,
                            snackBarHostState = snackBarHostState,
                        )
                    }
                    composable<Destination.SettingsScreen> {
                        SettingsScreen(
                            isLoggedIn = loginState.isLoggedIn,
                            state = settingsState,
                            onEvent = settingsViewModel::onEvent,
                            userProfilePicture = loginState.userProfilePicture,
                            revealCanvasState = revealCanvasState,
                            showFirstTimeAnimation = loginState.user?.showFirstTimeAnimation,
                            onNavigateBack = navController::popBackStack,
                            onGoToLoginScreen = {
                                navController.navigate(Destination.LoginScreen(it))
                            },
                            onGoToAboutAppScreen = {
                                navController.navigate(Destination.AboutScreen)
                            },
                            clearBackStack = { navController.clearBackStack(Destination.MapHost) },
                            appIntroDialog = { onDismiss ->
                                AppIntroDialog(onDismiss)
                            },
                        )
                        AnimatedVisibility(settingsState.showLogoutDialog) {
                            LogoutConfirmationDialog(
                                isLoginOut = loginState.isLoginOut,
                                onDismissRequest = {
                                    settingsViewModel.onEvent(
                                        SettingsEvent.ShowLogoutDialog(false),
                                    )
                                },
                                onLogout = {
                                    loginViewModel.onEvent(LoginEvent.OnLogout)
                                },
                            )
                        }
                        AnimatedVisibility(settingsState.showDeleteAccountDialog) {
                            DeleteAccountConfirmationDialog(
                                loginState = loginState,
                                onDeleteAccount = { keepContributions ->
                                    loginViewModel.onEvent(
                                        LoginEvent.DeleteAccount(keepContributions),
                                    )
                                },
                                onDismissRequest = {
                                    settingsViewModel.onEvent(
                                        SettingsEvent.ShowDeleteAccountDialog(false),
                                    )
                                },
                                snackBarHostState = snackBarHostState,
                            )
                        }
                        AnimatedVisibility(settingsState.showProfilePictureSettings) {
                            ProfileSettingsDialog(
                                onDismiss = {
                                    settingsViewModel.onEvent(
                                        SettingsEvent.ShowProfilePictureSettings(false),
                                    )
                                },
                                loginState = loginState,
                                onEvent = loginViewModel::onEvent,
                                snackBarHostState = snackBarHostState,
                            )
                        }
                    }
                    composable<Destination.LibraryScreen> {
                        val libraryViewModel = koinViewModel<LibraryViewModel>()
                        val ossLibraries by libraryViewModel.ossLibraries.collectAsStateWithLifecycle()
                        LibraryScreen(
                            libraries = ossLibraries,
                            popBackStack = navController::popBackStack,
                        )
                    }
                    composable<Destination.AboutScreen> {
                        AboutAppScreen(
                            showTermsAndConditions = settingsState.showTermsAndConditions,
                            onNavigateBack = navController::navigateUp,
                            onShowTermsAndConditions = {
                                settingsViewModel.onEvent(
                                    SettingsEvent.OpenTermsAndConditions(true),
                                )
                            },
                            onGoToLicenses = {
                                navController.navigate(Destination.LibraryScreen)
                            },
                            termsAndConditionsDialog = {
                                TermsAndConditionsDialog(onDismissRequest = {
                                    settingsViewModel.onEvent(SettingsEvent.OpenTermsAndConditions(false))
                                })
                            },
                        )
                    }
                }
            }
        }
    }

    SingletonCoilImageLoader()

    LaunchedEffect(loginState.isLoggedIn, appIntroState.showAppIntro) {
        if (loginState.isLoggedIn && appIntroState.showAppIntro) {
            navController.navigate(Destination.MapHost)
        }
    }
}
