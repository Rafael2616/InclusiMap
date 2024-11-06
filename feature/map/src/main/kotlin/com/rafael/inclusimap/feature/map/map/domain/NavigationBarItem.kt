package com.rafael.inclusimap.feature.map.map.domain

import androidx.compose.ui.graphics.vector.ImageVector

data class NavigationBarItem(
    val selected: Boolean,
    val onClick: () -> Unit,
    val icon: ImageVector,
    val name: String,
)
