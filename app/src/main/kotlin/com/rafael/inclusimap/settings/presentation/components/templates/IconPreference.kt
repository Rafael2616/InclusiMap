package com.rafael.inclusimap.settings.presentation.components.templates

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun IconPreference(
    title: String,
    trailingIcon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    description: String? = null,
    rowHeight: Dp = 58.dp,
    leadingCanvas: (DrawScope.() -> Unit)? = null,
    leadingWidget: (@Composable BoxScope.() -> Unit)? = null,
    extraContent: (@Composable () -> Unit)? = null,
    leadingIcon: ImageVector? = null,
) {
    Row(
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .height(rowHeight)
            .clickable { onClick() }
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(10.dp))
            .padding(horizontal = 10.dp),
    ) {
        leadingIcon?.let { leadingIcon ->
            Icon(
                imageVector = leadingIcon,
                contentDescription = "Icon",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .size(32.dp),
            )
        }
        leadingCanvas?.let {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.15f))
                    .size(35.dp)
                    .align(Alignment.CenterVertically),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier.size(28.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize(),
                    ) {
                        leadingCanvas(this)
                    }
                }
            }
        }
        leadingWidget?.let {
            Box(
                modifier = Modifier.size(35.dp),
                contentAlignment = Alignment.Center,
            ) {
                leadingWidget()
            }
        }
        Column(
            modifier = Modifier
                .width(
                    if ("a" == "b") { //iInLandscape
                        350.dp
                    } else if (description != null) {
                        250.dp
                    } else {
                        270.dp
                    },
                )
                .padding(start = 16.dp, end = 10.dp)
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.Start,
        ) {
            Text(
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp),
                color = MaterialTheme.colorScheme.onSurface,
                text = title,
                textAlign = TextAlign.Start,
            )
            description?.let {
                Text(
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    text = description,
                    textAlign = TextAlign.Start,
                )
            }
        }

        // An optional extra content next to preference title
        extraContent?.invoke()

        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.CenterEnd,
        ) {
            Icon(
                imageVector = trailingIcon,
                contentDescription = "Icon",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .size(32.dp),
            )
        }
    }
}
