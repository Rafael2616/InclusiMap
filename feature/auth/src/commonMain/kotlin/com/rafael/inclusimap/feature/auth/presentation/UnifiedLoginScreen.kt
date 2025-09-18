package com.rafael.inclusimap.feature.auth.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rafael.inclusimap.core.resources.Res
import com.rafael.inclusimap.core.resources.ic_launcher_foreground
import com.rafael.inclusimap.core.ui.isLandscape
import com.rafael.inclusimap.feature.auth.domain.model.LoginState
import com.rafael.inclusimap.feature.auth.domain.model.RegisteredUser
import com.rafael.inclusimap.feature.auth.domain.model.User
import com.rafael.inclusimap.feature.auth.presentation.components.LoginScreen
import com.rafael.inclusimap.feature.auth.presentation.components.RecoveryPasswordScreen
import com.rafael.inclusimap.feature.auth.presentation.components.RegistrationScreen
import com.rafael.inclusimap.feature.auth.presentation.components.UpdatePasswordScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

@Composable
fun UnifiedLoginScreen(
    loginState: LoginState,
    onLogin: (RegisteredUser) -> Unit,
    onRegister: (User) -> Unit,
    onUpdatePassword: (String) -> Unit,
    onSendRecoverEmail: (String) -> Unit,
    onValidateToken: (String) -> Unit,
    onCancel: () -> Unit,
    onResetUpdateProcess: () -> Unit,
    onPopBackStack: () -> Unit,
    isEditPasswordModeFromSettings: Boolean,
    termsAndConditionsDialog: @Composable (onDismissRequest: () -> Unit) -> Unit,
    modifier: Modifier = Modifier,
) {
    var registerNewUser by remember { mutableStateOf(false) }
    var isRecoveryScreen by remember { mutableStateOf(true) }
    var isUpdatePasswordMode by remember { mutableStateOf(isEditPasswordModeFromSettings) }
    val scope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }

    Scaffold(
        modifier = modifier
            .fillMaxSize(),
        snackbarHost = {
            SnackbarHost(
                hostState = snackBarHostState,
                modifier = Modifier.navigationBarsPadding(),
            ) {
                Snackbar(it)
            }
        },
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.add(WindowInsets.navigationBars),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primaryContainer,
                        ),
                    ),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Card(
                modifier = Modifier
                    .navigationBarsPadding()
                    .statusBarsPadding()
                    .fillMaxWidth(if (isLandscape) 0.5f else 0.85f)
                    .imePadding()
                    .shadow(
                        elevation = 20.dp,
                        shape = RoundedCornerShape(38.dp),
                        spotColor = MaterialTheme.colorScheme.onSurface,
                        clip = true,
                    )
                    .clip(RoundedCornerShape(38.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(20.dp),
                ),
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                ) {
                    Image(
                        painter = painterResource(Res.drawable.ic_launcher_foreground),
                        contentDescription = "InclusiMap Logo",
                        modifier = Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .size(70.dp),
                    )
                }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
                ) {
                    item {
                        Text(
                            text = "InclusiMap",
                            fontSize = 24.sp,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth(),
                        )
                    }
                    item {
                        AnimatedContent(
                            targetState = isUpdatePasswordMode,
                            modifier = Modifier.fillMaxWidth(),
                            label = "",
                        ) { isPasswordUpdateMode ->
                            if (isPasswordUpdateMode) {
                                AnimatedContent(
                                    targetState = isRecoveryScreen,
                                    modifier = Modifier.fillMaxWidth(),
                                    label = "",
                                ) { recoveryScreen ->
                                    if (recoveryScreen) {
                                        RecoveryPasswordScreen(
                                            state = loginState,
                                            onCancel = {
                                                onCancel()
                                                // Await for the navigation to finish
                                                scope.launch {
                                                    delay(350L)
                                                    isUpdatePasswordMode = false
                                                }
                                            },
                                            onSendRecoverEmail = { email ->
                                                onSendRecoverEmail(email)
                                            },
                                            onValidateToken = { token ->
                                                onValidateToken(token)
                                            },
                                            onResetProcess = {
                                                onResetUpdateProcess()
                                            },
                                            snackBarHostState = snackBarHostState,
                                        )
                                    } else {
                                        UpdatePasswordScreen(
                                            state = loginState,
                                            onCancel = {
                                                isRecoveryScreen = true
                                                onResetUpdateProcess()
                                            },
                                            onUpdatePassword = {
                                                onUpdatePassword(it)
                                            },
                                            popBackStack = {
                                                onPopBackStack()
                                                onResetUpdateProcess()
                                            },
                                            snackBarHostState = snackBarHostState,
                                        )
                                    }
                                }
                            } else {
                                AnimatedContent(
                                    targetState = registerNewUser,
                                    modifier = Modifier.fillMaxWidth(),
                                    label = "",
                                ) {
                                    if (it) {
                                        RegistrationScreen(
                                            state = loginState,
                                            onRegister = { registredUser -> onRegister(registredUser) },
                                            onGoToLogin = { registerNewUser = false },
                                            termsAndConditionsDialog = {
                                                termsAndConditionsDialog(
                                                    {
                                                        registerNewUser = false
                                                    },
                                                )
                                            },
                                            snackBarHostState = snackBarHostState,
                                        )
                                    } else {
                                        LoginScreen(
                                            state = loginState,
                                            onLogin = { user -> onLogin(user) },
                                            onGoToRegister = { registerNewUser = true },
                                            onGoToRecover = {
                                                isUpdatePasswordMode = true
                                                isRecoveryScreen = true
                                            },
                                            snackBarHostState = snackBarHostState,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    DisposableEffect(loginState.isTokenValid) {
        if (loginState.isTokenValid) {
            isRecoveryScreen = false
        }
        onDispose { }
    }
}
