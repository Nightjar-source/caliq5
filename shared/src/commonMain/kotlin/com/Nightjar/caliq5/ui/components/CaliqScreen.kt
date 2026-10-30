package com.Nightjar.caliq5.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Unified Edge-to-Edge Screen Container with Elastic Overscroll and Safe Insets Handling.
 */
@Composable
fun CaliqScreen(
    title: String,
    onBack: (() -> Unit)? = null,
    showBottomNav: Boolean = true,
    scrollable: Boolean = true,
    scrollState: androidx.compose.foundation.ScrollState = androidx.compose.foundation.rememberScrollState(),
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) {
        val outerModifier = Modifier.fillMaxSize().elasticOverscroll()
        BoxWithConstraints(modifier = outerModifier) {
            val isWide = maxWidth >= 600.dp
            val isWideOrLandscape = maxWidth >= 600.dp
            val topBarHeight = if (isWideOrLandscape) 42.dp else 48.dp
            val density = androidx.compose.ui.platform.LocalDensity.current
            val isImeVisible = WindowInsets.ime.getBottom(density) > 0
            val dynamicBottomPadding = if (isImeVisible) 20.dp else if (!showBottomNav) 85.dp else if (isWide) 85.dp else 150.dp

            if (scrollable) {
                Column(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .widthIn(min = 320.dp, max = 840.dp)
                        .fillMaxWidth()
                        .imePadding()
                        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
                        .verticalScroll(scrollState)
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val topSafeInsets = WindowInsets.statusBars
                        .union(WindowInsets.captionBar)
                        .union(WindowInsets.displayCutout)
                    Spacer(modifier = Modifier.windowInsetsTopHeight(topSafeInsets))
                    Spacer(modifier = Modifier.height(topBarHeight + 14.dp))
                    content()
                    Spacer(modifier = Modifier.height(dynamicBottomPadding))
                    Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
                }
            } else {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxSize(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    content()
                }
            }
        }
    }
}
