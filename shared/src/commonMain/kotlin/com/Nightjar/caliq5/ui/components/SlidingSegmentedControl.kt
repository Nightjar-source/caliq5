package com.Nightjar.caliq5.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@Composable
fun <T> SlidingSegmentedControl(
    options: List<Pair<String, T>>,
    selectedOption: T,
    onOptionSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    height: androidx.compose.ui.unit.Dp = 38.dp
) {
    val selectedIndex = options.indexOfFirst { it.second == selectedOption }.coerceAtLeast(0)
    val isDark = isSystemInDarkTheme()
    val controlShape = RoundedCornerShape(50)
    
    val animatedIndex by animateFloatAsState(
        targetValue = selectedIndex.toFloat(),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "IndicatorIndex"
    )

    Box(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isDark) 0.50f else 0.55f),
                shape = controlShape
            )
            .padding(2.5.dp)
            .height(height)
    ) {
        // Sliding indicator with placeRelative - mathematically mirrors in RTL and LTR
        Box(
            modifier = Modifier
                .fillMaxSize()
                .layout { measurable, constraints ->
                    val segmentCount = options.size.coerceAtLeast(1)
                    val segmentWidthPx = constraints.maxWidth / segmentCount
                    val placeable = measurable.measure(
                        constraints.copy(
                            minWidth = segmentWidthPx,
                            maxWidth = segmentWidthPx,
                            minHeight = constraints.maxHeight,
                            maxHeight = constraints.maxHeight
                        )
                    )
                    layout(constraints.maxWidth, constraints.maxHeight) {
                        val xPx = (animatedIndex * segmentWidthPx).roundToInt()
                        // placeRelative places at x in LTR, and (parentWidth - x - childWidth) in RTL!
                        placeable.placeRelative(x = xPx, y = 0)
                    }
                }
                .shadow(elevation = 1.5.dp, shape = controlShape)
                .background(MaterialTheme.colorScheme.primary, controlShape)
        )

        Row(modifier = Modifier.fillMaxSize()) {
            options.forEach { (label, value) ->
                val isSelected = value == selectedOption
                val targetTextColor = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    if (isDark) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                    else MaterialTheme.colorScheme.onSurfaceVariant
                }
                val textColor by animateColorAsState(
                    targetValue = targetTextColor,
                    animationSpec = tween(200),
                    label = "textColorAnim"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onOptionSelected(value) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontSize = 13.5.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = textColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
