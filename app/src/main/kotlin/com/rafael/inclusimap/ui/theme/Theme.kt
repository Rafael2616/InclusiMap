package com.rafael.inclusimap.ui.theme

import android.graphics.Color
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import com.rafael.inclusimap.settings.domain.model.SettingsState

@Composable
fun InclusiMapTheme(
    isSystemInDarkTheme: Boolean = isSystemInDarkTheme(),
    state: SettingsState,
    content: @Composable () -> Unit,
) {
    val isDarkThemeOn = state.isDarkThemeOn
    val isFollowingSystemOn = state.isFollowingSystemOn
    val isDynamicColorsOn = state.isDynamicColorsOn
    val context = LocalContext.current
    val isAndroidS = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    // Handle which color scheme will be applied
    val colors = if (isFollowingSystemOn) {
        if (isAndroidS && isDynamicColorsOn) {
            if (isSystemInDarkTheme) {
                dynamicDarkColorScheme(context)
            } else {
                dynamicLightColorScheme(context)
            }
        } else if (isSystemInDarkTheme) {
            DarkColors
        } else {
            LightColors
        }
    } else {
        if (isAndroidS && isDynamicColorsOn) {
            if (isDarkThemeOn) {
                dynamicDarkColorScheme(context)
            } else {
                dynamicLightColorScheme(context)
            }
        } else {
            if (isDarkThemeOn) {
                DarkColors
            } else {
                LightColors
            }
        }
    }

    MaterialTheme(
        colorScheme = colors,
        shapes = Shapes,
        typography = Typography,
        content = content,
    )

    // Set System Bars Colors
    DisposableEffect(isDarkThemeOn, isSystemInDarkTheme, isFollowingSystemOn) {
        val activity = context as ComponentActivity
        activity.enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                Color.TRANSPARENT,
                Color.TRANSPARENT,
            ) { isFollowingSystemOn && isSystemInDarkTheme || !isFollowingSystemOn && isDarkThemeOn },
            navigationBarStyle = SystemBarStyle.auto(
                Color.TRANSPARENT,
                Color.TRANSPARENT,
            ) { isDarkThemeOn },
        )
        onDispose {}
    }
}
