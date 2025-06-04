package com.joincoded.bankapi.composable

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp

@Composable
fun RotationArrowIcon(
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    isAnimating: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Canvas(
        modifier = modifier.size(64.dp)
    ) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val radius = size.width * 0.4f
        val arrowSize = size.width * 0.15f

        // Draw the circle
        drawCircle(
            color = color,
            radius = radius,
            center = Offset(centerX, centerY),
            style = Stroke(width = 4f, cap = StrokeCap.Round)
        )

        // Draw the arrow
        val path = Path().apply {
            moveTo(centerX, centerY - radius + arrowSize)
            lineTo(centerX - arrowSize, centerY - radius)
            lineTo(centerX + arrowSize, centerY - radius)
            close()
        }

        // Apply rotation if animating
        if (isAnimating) {
            rotate(rotation, Offset(centerX, centerY)) {
                drawPath(path, color)
            }
        } else {
            drawPath(path, color)
        }
    }
} 