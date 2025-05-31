package com.joincoded.bankapi.SVG

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp

@Composable
fun ShopFilledIcon(
    modifier: Modifier = Modifier,
    tint: Color = Color.Black
) {
    val path = PathParser().parsePathString(
        "M7.5 7v-.5a4.5 4.5 0 0 1 9 0V7H19c.552 0 1 .449 1 1.007v12.001c0 1.1-.895 1.992-1.994 1.992H5.994A1.994 1.994 0 0 1 4 20.008v-12C4 7.45 4.445 7 5 7zM9 7h6v-.5a3 3 0 0 0-6 0zM7.5 7v4H9V7zM15 7v4h1.5V7z"
    ).toNodes()

    val vector = ImageVector.Builder(
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        addPath(
            pathData = path,
            fill = SolidColor(Color(0xFF292F33)),
            fillAlpha = 1.0f
        )
    }.build()
    androidx.compose.foundation.Image(
        painter = rememberVectorPainter(vector),
        contentDescription = "Shop",
        modifier = modifier,
        colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(tint)
    )
} 