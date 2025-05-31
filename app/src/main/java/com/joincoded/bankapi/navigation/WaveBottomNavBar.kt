package com.joincoded.bankapi.navigation

import com.joincoded.bankapi.SVG.CurrencyExchangeIcon
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.joincoded.bankapi.SVG.BankCardsIcon
import com.joincoded.bankapi.SVG.ShopFilledIcon

@Composable
fun WaveBottomNavBar(
    items: List<NavBarItem>,
    currentIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    fabIcon: ImageVector? = null,
    onFabClick: (() -> Unit)? = null
) {
    val iconPositions = remember { mutableStateListOf<Float>() }
    val rowWidth = remember { mutableStateOf(0f) }
    val fabIndex = if (fabIcon != null) items.size / 2 else -1
    val actualItems = remember(items, fabIcon) {
        if (fabIcon != null) {
            val mutable = items.toMutableList()
            mutable.add(fabIndex, NavBarItem("FAB", fabIcon, Color(0xFFFB7185)))
            mutable
        } else items
    }

    // Prepare animators for each bubble (must be before handleItemSelected)
    val bubbleAnimators = remember { mutableMapOf<Int, Animatable<Float, *>>() }

    // Animation: Track previous index for dip/pop effect
    var prevIndex by remember { mutableStateOf(currentIndex) }
    val prevIndexRef = remember { mutableStateOf(currentIndex) }
    val pendingIndex = remember { mutableStateOf<Int?>(null) }
    val scope = rememberCoroutineScope()

    // Intercept selection to sequence the animation
    fun handleItemSelected(newIndex: Int) {
        if (newIndex == currentIndex) return
        pendingIndex.value = newIndex
        scope.launch {
            // Start the bubble drop in parallel
            launch {
                bubbleAnimators[prevIndex]?.animateTo(
                    targetValue = 24f,
                    animationSpec = tween(durationMillis = 100)
                )
                bubbleAnimators[prevIndex]?.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 100)
                )
            }
            kotlinx.coroutines.delay(80)
            // Immediately update selection and move the dip
            prevIndexRef.value = prevIndex
            prevIndex = newIndex
            pendingIndex.value = null
            onItemSelected(newIndex)
        }
    }

    // Animate dip position
    val targetDipX = when {
        fabIcon != null && currentIndex == fabIndex -> rowWidth.value / 2f
        iconPositions.size == actualItems.size -> iconPositions.getOrNull(currentIndex) ?: 0f
        else -> 0f
    }
    val animatedDipX by animateFloatAsState(
        targetValue = targetDipX,
        animationSpec = tween(durationMillis = 120),
        label = "dipX"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
            .shadow(8.dp, shape = MaterialTheme.shapes.medium),
        contentAlignment = Alignment.TopCenter
    ) {
        // Draw background with dip
        Canvas(modifier = Modifier
            .fillMaxSize()
            .padding(top = 32.dp)
        ) {
            drawWaveBackground(animatedDipX, size.width, size.height)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp)
                .onGloballyPositioned { coordinates ->
                    rowWidth.value = coordinates.size.width.toFloat()
                },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            actualItems.forEachIndexed { index, item ->
                val isSelected = index == currentIndex
                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.1f else 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "iconScale"
                )
                // Bubble (background) dip and pop/sunset animation
                val bubbleOffset = bubbleAnimators.getOrPut(index) { Animatable(0f) }
                LaunchedEffect(currentIndex, isSelected) {
                    if (isSelected) {
                        bubbleOffset.animateTo(
                            targetValue = -20f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        )
                    } else {
                        bubbleOffset.animateTo(0f)
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp)
                        .onGloballyPositioned { coordinates ->
                            val centerX = coordinates.positionInParent().x + coordinates.size.width / 2f
                            if (iconPositions.size > index) {
                                iconPositions[index] = centerX
                            } else if (iconPositions.size == index) {
                                iconPositions.add(centerX)
                            }
                        }
                        .clickable {
                            if (fabIcon != null && index == fabIndex) {
                                onFabClick?.invoke()
                            } else {
                                handleItemSelected(index)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (fabIcon != null && index == fabIndex) {
                        // FAB as tab
                        FloatingActionButton(
                            onClick = { onFabClick?.invoke() },
                            containerColor = Color(0xFFFB7185),
                            contentColor = Color(0xFFA58CD3),
                            modifier = Modifier
                                .size(44.dp)
                                .offset(y = bubbleOffset.value.dp)
                                .scale(scale)
                        ) {
                            Icon(
                                imageVector = fabIcon,
                                contentDescription = "FAB",
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .offset(y = bubbleOffset.value.dp)
                                    .scale(scale)
                                    .background(
                                        if (isSelected || index == prevIndexRef.value) Color.White else Color.Transparent,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                when (item.label) {
                                    "CurrencyExchange" -> CurrencyExchangeIcon(
                                        modifier = Modifier.size(30.dp),
                                        color = if (isSelected) Color(0xFFA086CE) else Color.Gray)
                                    "BankCards" -> BankCardsIcon(modifier = Modifier.size(30.dp), tint = if (isSelected) Color(0xFFA086CE) else Color.Gray)
                                    "ShopFilled" -> ShopFilledIcon(modifier = Modifier.size(30.dp), tint = if (isSelected) Color(0xFFA086CE) else Color.Gray)
                                    else -> Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.label,
                                        tint = if (isSelected) Color(0xFFA086CE) else Color.Gray,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun DrawScope.drawWaveBackground(selectedX: Float, width: Float, height: Float) {
    val dipDepth = 130f
    val dipWidth = 180f
    val dipYOffset = 13f
    val path = Path().apply {
        moveTo(0f, 0f)
        lineTo(selectedX - dipWidth, 0f)
        cubicTo(
            selectedX - dipWidth * 0.7f, dipYOffset,
            selectedX - dipWidth * 0.6f, dipDepth + dipYOffset,
            selectedX, dipDepth + dipYOffset
        )
        cubicTo(
            selectedX + dipWidth * 0.6f, dipDepth + dipYOffset,
            selectedX + dipWidth * 0.7f, dipYOffset,
            selectedX + dipWidth, 0f
        )
        lineTo(width, 0f)
        lineTo(width, height)
        lineTo(0f, height)
        close()
    }
    drawPath(path, color = Color.White)
}

// Model
data class NavBarItem(
    val label: String,
    val icon: ImageVector,
    val color: Color
) 