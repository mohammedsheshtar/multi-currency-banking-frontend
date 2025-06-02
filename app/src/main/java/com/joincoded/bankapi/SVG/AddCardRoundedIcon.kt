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
fun AddCardRoundedIcon(modifier: Modifier = Modifier, color: Color = Color.Unspecified) {
    val path = PathParser().parsePathString(
        "M19.5 18.5H17q-.213 0-.356-.144t-.144-.357t.144-.356T17 17.5h2.5V15q0-.213.144-.356t.357-.144t.356.144t.143.356v2.5H23q.213 0 .356.144t.144.357t-.144.356T23 18.5h-2.5V21q0 .213-.144.356t-.357.144t-.356-.144T19.5 21zM4 11.192h16V8.808H4zM4.615 19q-.69 0-1.153-.462T3 17.384V6.616q0-.691.463-1.153T4.615 5h14.77q.69 0 1.152.463T21 6.616v5.076q0 .344-.232.576t-.576.232H19.5q-2.075 0-3.537 1.463T14.5 17.5v.692q0 .344-.232.576t-.576.232z"
    ).toNodes()

    val vector = ImageVector.Builder(
        defaultWidth = 26.dp,
        defaultHeight = 26.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        addPath(
            pathData = path,
            fill = SolidColor(color)
        )
    }.build()

    Icon(
        painter = rememberVectorPainter(vector),
        contentDescription = "Add Card Rounded Icon",
        modifier = modifier.size(48.dp),
        tint = Color.Unspecified
    )
}
