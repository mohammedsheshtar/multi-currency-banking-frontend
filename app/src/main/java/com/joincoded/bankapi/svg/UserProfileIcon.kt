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
fun UserProfileIcon(modifier: Modifier = Modifier, color: Color = Color.Gray) {
    val path = PathParser().parsePathString(
        "M18 2a16 16 0 1 0 0 32a16 16 0 0 0 0-32zm0 4a6 6 0 1 1 0 12a6 6 0 0 1 0-12zm0 28c-4 0-7.5-2-9.5-5c.5-3 6-4.5 9.5-4.5s9 1.5 9.5 4.5c-2 3-5.5 5-9.5 5z"
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
        contentDescription = "User Profile Icon",
        modifier = modifier.size(48.dp),
        tint = Color.Unspecified // preserve original fill colors
    )
} 