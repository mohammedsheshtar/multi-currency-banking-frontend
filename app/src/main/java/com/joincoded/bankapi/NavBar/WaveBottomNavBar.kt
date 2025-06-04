package com.joincoded.bankapi.NavBar

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.example.multicurrency_card.SVG.BankCardsIcon
import com.example.multicurrency_card.SVG.ShopFilledIcon
import com.joincoded.bankapi.SVG.CurrencyExchangeIcon
import com.joincoded.bankapi.SVG.Home3FillIcon
import com.joincoded.bankapi.SVG.UserSolidIcon

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
            mutable.add(fabIndex, NavBarItem("FAB", { isSelected -> Icon(fabIcon, contentDescription = "FAB", tint = Color(0xFFFB7185)) }, Color(0xFFFB7185)))
            mutable
        } else items
    }

    // Prepare animators for each bubble (must be before handleItemSelected)
    val bubbleAnimators = remember { mutableMapOf<Int, Animatable<Float, AnimationVector1D>>() }

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
                    targetValue = 24.dp.value,
                    animationSpec = tween(durationMillis = 100, easing = FastOutSlowInEasing)
                )
                bubbleAnimators[prevIndex]?.animateTo(
                    targetValue = 0.dp.value,
                    animationSpec = tween(durationMillis = 100, easing = FastOutSlowInEasing)
                )
            }
            delay(80)
            // Immediately update selection and move the dip
            prevIndexRef.value = prevIndex
            prevIndex = newIndex
            pendingIndex.value = null
            onItemSelected(newIndex)
        }
    }

    val dipDepth = 100f // Even deeper dip
    val dipWidth = 178f // Wide dip
    val dipYOffset = 24f // Lower the dip

    // Animate dip position
    val targetDipX = when {
        fabIcon != null && currentIndex == fabIndex -> rowWidth.value / 2f
        iconPositions.size == actualItems.size -> iconPositions.getOrNull(currentIndex) ?: 0f
        else -> 0f
    }
    val animatedDipX by animateFloatAsState(
        targetValue = targetDipX,
        animationSpec = tween(durationMillis = 120, easing = FastOutSlowInEasing),
        label = "dipX"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
            .shadow(8.dp, shape = MaterialTheme.shapes.medium, spotColor = Color(0xFF000000)),
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
                val wasSelected = index == prevIndexRef.value && prevIndexRef.value != currentIndex
                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.1f else 1f, // Slightly smaller scale
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow,
                        visibilityThreshold = 0.001f
                    ),
                    label = "iconScale"
                )
                // Bubble (background) dip and pop/sunset animation
                val bubbleOffset = bubbleAnimators.getOrPut(index) { Animatable(0.dp.value) }
                LaunchedEffect(currentIndex, isSelected) {
                    if (isSelected) {
                        bubbleOffset.animateTo(
                            targetValue = (-20).dp.value,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow,
                                visibilityThreshold = 0.001f
                            )
                        )
                    } else {
                        bubbleOffset.animateTo(
                            0.dp.value,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow,
                                visibilityThreshold = 0.001f
                            )
                        )
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
                            elevation = FloatingActionButtonDefaults.elevation(
                                defaultElevation = 0.dp,
                                pressedElevation = 0.dp,
                                focusedElevation = 0.dp,
                                hoveredElevation = 0.dp
                            ),
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
                                    // COLOR FOR CIRCLE SHAPE
                                    .background(
                                        if (isSelected || index == prevIndexRef.value) Color(0xFF2C2C2E) else Color.Transparent,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                when (item.label) {
                                    "CurrencyExchange" -> CurrencyExchangeIcon(
                                        modifier = Modifier.size(30.dp),
                                        color = if (isSelected) Color(0xFFA086CE) else Color(0xFF9E9E9E)
                                    )
                                    "BankCards" -> BankCardsIcon(
                                        modifier = Modifier.size(30.dp),
                                        color = if (isSelected) Color(0xFFA086CE) else Color(0xFF9E9E9E)
                                    )
                                    "ShopFilled" -> ShopFilledIcon(
                                        modifier = Modifier.size(30.dp),
                                        color = if (isSelected) Color(0xFFA086CE) else Color(0xFF9E9E9E)
                                    )
                                    "Home" -> Home3FillIcon(
                                        modifier = Modifier.size(30.dp),
                                        color = if (isSelected) Color(0xFFA086CE) else Color(0xFF9E9E9E)
                                    )
                                    "Profile" -> UserSolidIcon(
                                        modifier = Modifier.size(30.dp),
                                        color = if (isSelected) Color(0xFFA086CE) else Color(0xFF9E9E9E)
                                    )
                                    else -> item.icon(isSelected)
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
    val dipDepth = 130f // Even deeper dip
    val dipWidth = 180f // Wide dip
    val dipYOffset = 13f // Lower the dip
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
    drawPath(path, color = Color(0xFF1A1A1D)) // Changed from Color.White to dark color
}

// Updated Model to work with composable icons while maintaining the same structure
data class NavBarItem(
    val label: String,
    val icon: @Composable (isSelected: Boolean) -> Unit,
    val color: Color = Color.Unspecified
)