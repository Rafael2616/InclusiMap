package com.rafael.inclusimap.core.ui.theme

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

@Composable
actual fun InclusiMapTheme(
    isDarkThemeOn: Boolean,
    isFollowingSystemTheme: Boolean,
    isDynamicColorOn: Boolean,
    content: @Composable (() -> Unit),
) {
    AndroidTheme(
        isDarkThemeOn = isDarkThemeOn,
        isFollowingSystemTheme = isFollowingSystemTheme,
        isDynamicColorOn = isDynamicColorOn,
        content = content,
    )

    val activity = LocalContext.current as ComponentActivity
    val isSystemInDarkTheme = isSystemInDarkTheme()

    DisposableEffect(isDarkThemeOn, isSystemInDarkTheme, isFollowingSystemTheme) {
        activity.enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                Color.TRANSPARENT,
                Color.TRANSPARENT,
            ) { isFollowingSystemTheme && isSystemInDarkTheme || !isFollowingSystemTheme && isDarkThemeOn },
            navigationBarStyle = SystemBarStyle.auto(
                Color.TRANSPARENT,
                Color.TRANSPARENT,
            ) { isFollowingSystemTheme && isSystemInDarkTheme || !isFollowingSystemTheme && isDarkThemeOn },
        )
        onDispose {}
    }
}

@Composable
fun AndroidTheme(
    isDarkThemeOn: Boolean,
    isFollowingSystemTheme: Boolean,
    isDynamicColorOn: Boolean,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colors = when {
        isDynamicColorOn && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (isSystemInDarkTheme()) {
                dynamicDarkColorScheme(
                    context,
                )
            } else {
                dynamicLightColorScheme(context)
            }
        }
        isFollowingSystemTheme -> if (isSystemInDarkTheme()) darkColorScheme else lightColorScheme
        isDarkThemeOn -> darkColorScheme
        else -> lightColorScheme
    }

    MaterialTheme(
        colorScheme = colors,
        typography = typography,
        shapes = shapes,
        content = content,
    )
}
