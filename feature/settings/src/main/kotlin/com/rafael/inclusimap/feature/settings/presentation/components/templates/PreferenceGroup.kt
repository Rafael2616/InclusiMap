package com.rafael.inclusimap.feature.settings.presentation.components.templates

import android.content.res.Configuration
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PreferenceGroup(
    heading: String,
    modifier: Modifier = Modifier,
    textSize: Int = 16,
    preferences: @Composable () -> Unit,
) {
    val orientation = LocalConfiguration.current.orientation
    val isLandscape = orientation == Configuration.ORIENTATION_LANDSCAPE

    Column(
        modifier = modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = heading,
            style = MaterialTheme.typography.titleLarge.copy(fontSize = textSize.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Start,
            modifier = Modifier
                .padding(bottom = 6.dp)
                .fillMaxWidth(if (isLandscape) 0.9f else 0.95f),
        )
        Card(
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            modifier = Modifier
                .animateContentSize()
                .fillMaxWidth(if (isLandscape) 0.88f else 0.93f)
                .clip(RoundedCornerShape(26.dp)),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                preferences()
            }
        }
    }
}
