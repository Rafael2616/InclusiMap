package com.rafael.inclusimap.feature.contributions.domain.model

import androidx.compose.ui.graphics.vector.ImageVector

data class ContributionItem(
    val icon: ImageVector,
    val name: String,
    val type: ContributionType,
    val quantity: Int,
)
