package com.rafael.inclusimap.core.ui.theme

import androidx.compose.runtime.Composable

@Composable
actual fun InclusiMapTheme(
    isDarkThemeOn: Boolean,
    isFollowingSystemTheme: Boolean,
    isDynamicColorOn: Boolean,
    content: @Composable (() -> Unit),
) {
    DefaultTheme(
        isDarkThemeOn = isDarkThemeOn,
        isFollowingSystemTheme = isFollowingSystemTheme,
        content = content,
    )
}
