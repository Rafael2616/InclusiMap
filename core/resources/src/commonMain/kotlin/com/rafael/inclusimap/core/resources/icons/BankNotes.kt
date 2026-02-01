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

val Icons.Outlined.BankNotes: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "Bank",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(
                fill = null,
                fillAlpha = 1.0f,
                stroke = SolidColor(Color(0xFF0F172A)),
                strokeAlpha = 1.0f,
                strokeLineWidth = 1.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                strokeLineMiter = 1.0f,
                pathFillType = PathFillType.NonZero,
            ) {
                moveTo(2.25f, 18.75f)
                curveTo(7.7172f, 18.75f, 13.0136f, 19.4812f, 18.0468f, 20.8512f)
                curveTo(18.7738f, 21.0491f, 19.5f, 20.5086f, 19.5f, 19.7551f)
                verticalLineTo(18.75f)
                moveTo(3.75f, 4.5f)
                verticalLineTo(5.25f)
                curveTo(3.75f, 5.6642f, 3.4142f, 6f, 3f, 6f)
                horizontalLineTo(2.25f)
                moveTo(2.25f, 6f)
                verticalLineTo(5.625f)
                curveTo(2.25f, 5.0037f, 2.7537f, 4.5f, 3.375f, 4.5f)
                horizontalLineTo(20.25f)
                moveTo(2.25f, 6f)
                verticalLineTo(15f)
                moveTo(20.25f, 4.5f)
                verticalLineTo(5.25f)
                curveTo(20.25f, 5.6642f, 20.5858f, 6f, 21f, 6f)
                horizontalLineTo(21.75f)
                moveTo(20.25f, 4.5f)
                horizontalLineTo(20.625f)
                curveTo(21.2463f, 4.5f, 21.75f, 5.0037f, 21.75f, 5.625f)
                verticalLineTo(15.375f)
                curveTo(21.75f, 15.9963f, 21.2463f, 16.5f, 20.625f, 16.5f)
                horizontalLineTo(20.25f)
                moveTo(21.75f, 15f)
                horizontalLineTo(21f)
                curveTo(20.5858f, 15f, 20.25f, 15.3358f, 20.25f, 15.75f)
                verticalLineTo(16.5f)
                moveTo(20.25f, 16.5f)
                horizontalLineTo(3.75f)
                moveTo(3.75f, 16.5f)
                horizontalLineTo(3.375f)
                curveTo(2.7537f, 16.5f, 2.25f, 15.9963f, 2.25f, 15.375f)
                verticalLineTo(15f)
                moveTo(3.75f, 16.5f)
                verticalLineTo(15.75f)
                curveTo(3.75f, 15.3358f, 3.4142f, 15f, 3f, 15f)
                horizontalLineTo(2.25f)
                moveTo(15f, 10.5f)
                curveTo(15f, 12.1569f, 13.6569f, 13.5f, 12f, 13.5f)
                curveTo(10.3431f, 13.5f, 9f, 12.1569f, 9f, 10.5f)
                curveTo(9f, 8.8431f, 10.3431f, 7.5f, 12f, 7.5f)
                curveTo(13.6569f, 7.5f, 15f, 8.8431f, 15f, 10.5f)
                close()
                moveTo(18f, 10.5f)
                horizontalLineTo(18.0075f)
                verticalLineTo(10.5075f)
                horizontalLineTo(18f)
                verticalLineTo(10.5f)
                close()
                moveTo(6f, 10.5f)
                horizontalLineTo(6.0075f)
                verticalLineTo(10.5075f)
                horizontalLineTo(6f)
                verticalLineTo(10.5f)
                close()
            }
        }.build()
}
