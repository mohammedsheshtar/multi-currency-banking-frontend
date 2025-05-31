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
fun ShopsIcon(modifier: Modifier = Modifier, color: Color = Color.Gray) {
    val path = PathParser().parsePathString(
        "M32 4H4a2 2 0 0 0-2 2v24a2 2 0 0 0 2 2h28a2 2 0 0 0 2-2V6a2 2 0 0 0-2-2zm0 26H4V6h28v24zM8 10h4v4H8zm8 0h4v4h-4zm8 0h4v4h-4zM8 18h4v4H8zm8 0h4v4h-4zm8 0h4v4h-4zM8 26h4v4H8zm8 0h4v4h-4zm8 0h4v4h-4z"
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
        contentDescription = "Shops Icon",
        modifier = modifier.size(48.dp),
        tint = Color.Unspecified // preserve original fill colors
    )
} 