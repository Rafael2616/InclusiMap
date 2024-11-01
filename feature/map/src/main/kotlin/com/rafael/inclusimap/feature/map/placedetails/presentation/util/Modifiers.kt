package com.rafael.inclusimap.feature.map.placedetails.presentation.util

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.unit.dp

fun Modifier.horizontalPaddingEdgeToEdge(
    index: Int,
    size: Int,
): Modifier = composed {
    when {
        index == 0 -> padding(start = 16.dp)
        index == 1 && size > 2 -> padding(start = 16.dp)
        size > 2 && size - 1 == index -> padding(end = 16.dp)
        else -> this
    }
}
