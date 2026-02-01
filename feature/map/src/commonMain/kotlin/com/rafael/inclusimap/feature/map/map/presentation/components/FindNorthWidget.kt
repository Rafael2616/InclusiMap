package com.rafael.inclusimap.feature.map.map.presentation.components

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CompassCalibration
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import com.rafael.inclusimap.feature.map.map.domain.model.TILT_RANGE
import com.rafael.inclusimap.feature.map.map.domain.model.greenColor
import com.rafael.inclusimap.feature.map.map.domain.model.inNorthRange
import com.rafael.libs.maps.interop.model.MapsCameraPosition

@Composable
fun BoxScope.FindNorthWidget(
    cameraPosition: MapsCameraPosition,
    isDarkThemeOn: Boolean,
    onFind: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FloatingActionButton(
        modifier = modifier
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(top = 85.dp, start = 12.dp)
            .size(45.dp)
            .align(Alignment.TopStart),
        onClick = {
            onFind()
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp),
    ) {
        Icon(
            imageVector = Icons.Outlined.CompassCalibration,
            contentDescription = "Localizar",
            tint = if (cameraPosition.bearing.inNorthRange() && cameraPosition.tilt in TILT_RANGE) {
                greenColor(isDarkThemeOn)
            } else {
                MaterialTheme.colorScheme.error
            },
            modifier = Modifier
                .rotate(degrees = cameraPosition.bearing),
        )
    }
}
