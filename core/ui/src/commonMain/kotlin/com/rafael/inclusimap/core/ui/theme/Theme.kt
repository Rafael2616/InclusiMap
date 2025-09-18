package com.rafael.inclusimap.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
expect fun InclusiMapTheme(
    isDarkThemeOn: Boolean = true,
    isFollowingSystemTheme: Boolean = true,
    isDynamicColorOn: Boolean = false,
    content: @Composable () -> Unit,
)

@Composable
fun DefaultTheme(
    isDarkThemeOn: Boolean = true,
    isFollowingSystemTheme: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colors = when {
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
