package com.rafael.inclusimap.feature.auth.presentation

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imeNestedScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rafael.inclusimap.core.resources.R
import com.rafael.inclusimap.feature.auth.domain.model.LoginState
import com.rafael.inclusimap.feature.auth.domain.model.RegisteredUser
import com.rafael.inclusimap.feature.auth.domain.model.User
import com.rafael.inclusimap.feature.auth.presentation.components.LoginScreen
import com.rafael.inclusimap.feature.auth.presentation.components.RegistrationScreen
import com.rafael.inclusimap.feature.auth.presentation.components.UpdatePasswordScreen

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UnifiedLoginScreen(
    loginState: LoginState,
    onLogin: (RegisteredUser) -> Unit,
    onRegister: (User) -> Unit,
    onUpdatePassword: (String) -> Unit,
    onCancel: () -> Unit,
    onPopBackStack: () -> Unit,
    modifier: Modifier = Modifier,
    isEditPasswordMode: Boolean = false,
) {
    var cadastreNewUser by remember { mutableStateOf(false) }
    val orientation = LocalConfiguration.current.orientation
    val isLandscape = orientation == Configuration.ORIENTATION_LANDSCAPE

    Box(
        modifier = modifier
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
                .imeNestedScroll()
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
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
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
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            ) {
                item {
                    Text(
                        text = "InclusiMap",
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .fillMaxWidth(),
                    )
                }
                if (isEditPasswordMode) {
                    item {
                        UpdatePasswordScreen(
                            state = loginState,
                            onCancel = {
                                onCancel()
                            },
                            onUpdatePassword = {
                                onUpdatePassword(it)
                            },
                            popBackStack = {
                                onPopBackStack()
                            },
                        )
                    }
                } else {
                    item {
                        AnimatedContent(
                            targetState = cadastreNewUser,
                            modifier = Modifier.fillMaxWidth(),
                            label = "",
                        ) {
                            if (it) {
                                RegistrationScreen(
                                    state = loginState,
                                    onRegister = { registredUser -> onRegister(registredUser) },
                                    onGoToLogin = { cadastreNewUser = false },
                                )
                            } else {
                                LoginScreen(
                                    state = loginState,
                                    onLogin = { user -> onLogin(user) },
                                    onGoToRegister = { cadastreNewUser = true },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
