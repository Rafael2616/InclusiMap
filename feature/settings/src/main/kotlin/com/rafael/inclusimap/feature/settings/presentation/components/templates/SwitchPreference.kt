package com.rafael.inclusimap.feature.settings.presentation.components.templates

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun SwitchPreference(
    title: String,
    leadingIcon: ImageVector,
    isChecked: Boolean,
    onCheckedChange: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .height(58.dp)
            .clickable { onCheckedChange() }
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(10.dp))
            .padding(horizontal = 10.dp),
    ) {
        Icon(
            imageVector = leadingIcon,
            contentDescription = "Icon",
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .size(32.dp),
        )
        Text(
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp),
            color = MaterialTheme.colorScheme.onSurface,
            text = title,
            modifier = Modifier
                .width(250.dp)
                .padding(start = 16.dp, end = 10.dp),
            textAlign = TextAlign.Start,
        )
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.CenterEnd,
        ) {
            Switch(
                checked = isChecked,
                onCheckedChange = {
                    onCheckedChange()
                },
                thumbContent = remember(isChecked) {
                    if (isChecked) {
                        {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = "Setting Switcher",
                                modifier = Modifier.size(SwitchDefaults.IconSize),
                            )
                        }
                    } else {
                        null
                    }
                },
            )
        }
    }
}
