package com.rafael.inclusimap.feature.settings.presentation.components.templates

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyHorizontalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.maps.android.compose.MapType
import com.rafael.inclusimap.feature.map.domain.GoogleMapType

@Composable
internal fun MultiSelectionPreference(
    selections: List<GoogleMapType>,
    selected: MapType,
    onSelectionChange: (MapType) -> Unit,
    modifier: Modifier = Modifier,
    rowHeight: Dp = 58.dp,
) {
    Row(
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .height(rowHeight)
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(10.dp))
            .padding(horizontal = 10.dp),
    ) {
        LazyHorizontalStaggeredGrid(
            rows = StaggeredGridCells.Adaptive(108.dp),
            modifier = Modifier
                .fillMaxWidth(),
        ) {
            selections.forEach {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(horizontal = 8.dp, vertical = 8.dp)
                            .animateContentSize()
                            .fillMaxWidth()
                            .height(30.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(width = 1.dp, color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(12.dp))
                            .then(
                                if (it.type == selected) {
                                    Modifier.background(MaterialTheme.colorScheme.surfaceColorAtElevation(100.dp))
                                } else {
                                    Modifier
                                },
                            )
                            .clickable { onSelectionChange(it.type) },
                    ) {
                        if (it.type == selected) {
                            Icon(
                                imageVector = Icons.TwoTone.Check,
                                contentDescription = "Icon",
                                tint = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                        Text(
                            text = it.name,
                            modifier = Modifier.padding(start = 8.dp),
                        )
                        Icon(
                            imageVector = it.icon,
                            contentDescription = "Icon",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 8.dp),
                        )
                    }
                }
            }
        }
    }
}
