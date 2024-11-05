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

val Icons.Outlined.Cleaning: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "Cleaning",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f,
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                fillAlpha = 1.0f,
                stroke = null,
                strokeAlpha = 1.0f,
                strokeLineWidth = 1.0f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Miter,
                strokeLineMiter = 1.0f,
                pathFillType = PathFillType.NonZero,
            ) {
                moveTo(360f, 280f)
                lineToRelative(40f, -80f)
                verticalLineToRelative(-40f)
                horizontalLineToRelative(-40f)
                verticalLineToRelative(-80f)
                horizontalLineToRelative(240f)
                quadToRelative(17f, 0f, 28.5f, 11.5f)
                reflectiveQuadTo(640f, 120f)
                verticalLineToRelative(40f)
                lineToRelative(-40f, 80f)
                horizontalLineTo(480f)
                verticalLineToRelative(-40f)
                lineToRelative(-80f, 80f)
                close()
                moveTo(320f, 880f)
                verticalLineToRelative(-274f)
                quadToRelative(0f, -11f, 3.5f, -23.5f)
                reflectiveQuadTo(332f, 560f)
                lineToRelative(148f, -280f)
                horizontalLineToRelative(120f)
                quadToRelative(14f, 14f, 27f, 37.5f)
                reflectiveQuadToRelative(13f, 42.5f)
                verticalLineToRelative(520f)
                close()
                moveToRelative(80f, -80f)
                horizontalLineToRelative(160f)
                verticalLineToRelative(-440f)
                horizontalLineToRelative(-32f)
                lineTo(400f, 604f)
                close()
                moveToRelative(0f, 0f)
                horizontalLineToRelative(160f)
                close()
            }
        }.build()
}
