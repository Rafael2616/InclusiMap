package com.rafael.inclusimap.ui.icons
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun GoogleMapsPin(
    pinColor: Color,
    modifier: Modifier = Modifier,
    pinSize: Dp = 64.dp,
) {
    Canvas(modifier = modifier.size(pinSize)) {

        val trianglePath = Path().apply {
            moveTo(size.width / 2, size.height * 0.85f)
            lineTo(size.width * 0.75f, size.height * 0.4f)
            lineTo(size.width * 0.25f, size.height * 0.4f)
            close()
        }
        drawPath(trianglePath, color = pinColor)

        drawCircle(
            color = pinColor,
            radius = size.width * 0.25f,
            center = Offset(size.width / 2, size.height * 0.35f)
        )

        drawCircle(
            color = pinColor.copy(alpha = 0.5f, red = 0.5f, green = 0.5f, blue = 0.5f),
            radius = size.width * 0.1f,
            center = Offset(size.width / 2, size.height * 0.35f)
        )
    }
}
