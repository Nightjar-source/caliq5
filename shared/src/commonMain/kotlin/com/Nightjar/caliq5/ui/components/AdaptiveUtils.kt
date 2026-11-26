package com.Nightjar.caliq5.ui.components

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.HingeInfo
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass

/**
 * Official Material 3 Adaptive Window Metrics & Posture helper (November 2026).
 * Replaces SubcomposeLayout-based BoxWithConstraints with zero-overhead WindowSizeClass
 * and LocalWindowInfo measurements across Android, Foldables, DeX, Desktop, and Web PWA.
 */
@Immutable
data class CaliqWindowMetrics(
    val widthDp: Dp,
    val heightDp: Dp,
    val isCompact: Boolean,
    val isMedium: Boolean,
    val isExpanded: Boolean,
    val isLarge: Boolean,
    val isWide: Boolean,
    val isLandscape: Boolean,
    val isPhoneLandscape: Boolean,
    val isUltraCompact: Boolean,
    val isTabletop: Boolean,
    val horizontalHinge: HingeInfo?,
    val verticalHinge: HingeInfo?,
    val windowSizeClass: WindowSizeClass
)

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun rememberCaliqWindowMetrics(): CaliqWindowMetrics {
    val adaptiveInfo = currentWindowAdaptiveInfo(supportLargeAndXLargeWidth = true)
    val density = LocalDensity.current
    val containerSize = LocalWindowInfo.current.containerSize
    val widthDp = with(density) { containerSize.width.toDp() }.coerceAtLeast(1.dp)
    val heightDp = with(density) { containerSize.height.toDp() }.coerceAtLeast(1.dp)

    val wsc = adaptiveInfo.windowSizeClass
    val posture = adaptiveInfo.windowPosture
    val horizontalHinge = posture.hingeList.firstOrNull { !it.isVertical }
    val verticalHinge = posture.hingeList.firstOrNull { it.isVertical && it.isSeparating }

    val isWide = wsc.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND) || verticalHinge != null
    val isExpanded = wsc.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)
    val isLarge = wsc.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_LARGE_LOWER_BOUND)
    val isCompact = !isWide
    val isMedium = isWide && !isExpanded
    val isLandscape = widthDp > heightDp
    val isPhoneLandscape = isLandscape && (heightDp < 500.dp && widthDp >= 520.dp)
    val isUltraCompact = widthDp < 280.dp

    return CaliqWindowMetrics(
        widthDp = widthDp,
        heightDp = heightDp,
        isCompact = isCompact,
        isMedium = isMedium,
        isExpanded = isExpanded,
        isLarge = isLarge,
        isWide = isWide,
        isLandscape = isLandscape,
        isPhoneLandscape = isPhoneLandscape,
        isUltraCompact = isUltraCompact,
        isTabletop = posture.isTabletop,
        horizontalHinge = horizontalHinge,
        verticalHinge = verticalHinge,
        windowSizeClass = wsc
    )
}
