package com.rafael.inclusimap.core.resources.icons

import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Icons.Outlined.Library: ImageVector by lazy {
    ImageVector.Builder(
        name = "LibraryBig",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(
            fill = null,
            fillAlpha = 1.0f,
            stroke = SolidColor(Color(0xFF000000)),
            strokeAlpha = 1.0f,
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
            strokeLineMiter = 1.0f,
            pathFillType = PathFillType.NonZero,
        ) {
            moveTo(4f, 3f)
            horizontalLineTo(10f)
            arcTo(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = true, 11f, 4f)
            verticalLineTo(20f)
            arcTo(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = true, 10f, 21f)
            horizontalLineTo(4f)
            arcTo(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = true, 3f, 20f)
            verticalLineTo(4f)
            arcTo(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = true, 4f, 3f)
            close()
        }
        path(
            fill = null,
            fillAlpha = 1.0f,
            stroke = SolidColor(Color(0xFF000000)),
            strokeAlpha = 1.0f,
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
            strokeLineMiter = 1.0f,
            pathFillType = PathFillType.NonZero,
        ) {
            moveTo(7f, 3f)
            verticalLineToRelative(18f)
        }
        path(
            fill = null,
            fillAlpha = 1.0f,
            stroke = SolidColor(Color(0xFF000000)),
            strokeAlpha = 1.0f,
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
            strokeLineMiter = 1.0f,
            pathFillType = PathFillType.NonZero,
        ) {
            moveTo(20.4f, 18.9f)
            curveToRelative(0.2f, 0.5f, -0.1f, 1.1f, -0.6f, 1.3f)
            lineToRelative(-1.9f, 0.7f)
            curveToRelative(-0.5f, 0.2f, -1.1f, -0.1f, -1.3f, -0.6f)
            lineTo(11.1f, 5.1f)
            curveToRelative(-0.2f, -0.5f, 0.1f, -1.1f, 0.6f, -1.3f)
            lineToRelative(1.9f, -0.7f)
            curveToRelative(0.5f, -0.2f, 1.1f, 0.1f, 1.3f, 0.6f)
            close()
        }
    }.build()
}
