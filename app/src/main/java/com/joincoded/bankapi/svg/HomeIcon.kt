package com.joincoded.bankapi.SVG

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp

@Composable
fun HomeIcon(modifier: Modifier = Modifier, color: Color = Color.Gray) {
    val path = PathParser().parsePathString(
        "M18 2L2 16h4v16h24V16h4L18 2zm0 4.5L30 16v16H6V16L18 6.5z"
    ).toNodes()

    val vector = ImageVector.Builder(
        defaultWidth = 36.dp,
        defaultHeight = 36.dp,
        viewportWidth = 36f,
        viewportHeight = 36f
    ).apply {
        addPath(pathData = path, fill = SolidColor(color))
    }.build()

    Icon(
        painter = rememberVectorPainter(vector),
        contentDescription = "Home Icon",
        modifier = modifier.size(48.dp),
        tint = Color.Unspecified // preserve original fill colors
    )
} 