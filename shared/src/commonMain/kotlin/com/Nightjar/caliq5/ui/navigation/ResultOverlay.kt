package com.Nightjar.caliq5.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.Nightjar.caliq5.data.*
import com.Nightjar.caliq5.ui.components.*
import com.Nightjar.caliq5.viewmodel.ResultDisplayState
import com.Nightjar.caliq5.theme.successColor
import com.Nightjar.caliq5.ui.result.*

/**
 * Result overlay - matching the web app design exactly:
 * Layout direction forced to RTL.
 * Top header docked and connected to status bar (no floating cards).
 * Clipped bottom corners matching other app headers.
 * Title animations and layout match CaliqPageHeader exactly.
 */
@Composable
fun ResultOverlay(
    title: String,
    content: ResultDisplayState,
    onClose: () -> Unit,
    onExportPdf: () -> Unit,
    adBanner: @Composable () -> Unit = {},
) {
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    LaunchedEffect(Unit) {
        focusManager.clearFocus()
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            val isDark = com.Nightjar.caliq5.theme.isAppInDarkTheme()
            val isWideOrLandscape = maxWidth >= 600.dp
            val isWideScreen = maxWidth >= 600.dp
            val isUltraCompact = maxWidth < 280.dp
            val topPad = if (isUltraCompact) 2.dp else if (isWideOrLandscape) 4.dp else 8.dp
            val topCapsuleHeight = if (isUltraCompact) 30.dp else if (isWideOrLandscape) 36.dp else 42.dp
            val iconBtnSize = if (isUltraCompact) 30.dp else if (isWideOrLandscape) 36.dp else 40.dp
            val iconInnerSize = if (isUltraCompact) 15.dp else if (isWideOrLandscape) 18.dp else 22.dp

            val layoutDirection = LocalLayoutDirection.current
            val safeDrawing = WindowInsets.safeDrawing.asPaddingValues()
            val safeStart = safeDrawing.calculateStartPadding(layoutDirection)
            val safeEnd = safeDrawing.calculateEndPadding(layoutDirection)

            val targetStartEdgePad = if (isWideScreen) safeStart + 32.dp else 16.dp
            val targetEndEdgePad = if (isWideScreen) safeEnd + 32.dp else 16.dp
            val startEdgePad by androidx.compose.animation.core.animateDpAsState(
                targetValue = targetStartEdgePad,
                animationSpec = androidx.compose.animation.core.spring(
                    dampingRatio = 0.75f,
                    stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow
                ),
                label = "result_start_pad"
            )
            val endEdgePad by androidx.compose.animation.core.animateDpAsState(
                targetValue = targetEndEdgePad,
                animationSpec = androidx.compose.animation.core.spring(
                    dampingRatio = 0.75f,
                    stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow
                ),
                label = "result_end_pad"
            )
            val titleSideClearance = if (isWideOrLandscape) 68.dp else 72.dp

            val topSafeInset = WindowInsets.statusBars
                .union(WindowInsets.captionBar)
                .union(WindowInsets.displayCutout)
                .asPaddingValues()
                .calculateTopPadding()

            // Background surface with pure Edge-to-Edge bleed across entire window
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background.copy(alpha = 0.98f),
            ) {}

            // Scrollable Content - Pure Edge-to-Edge with safeDrawing insets applied strictly to inner content
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .widthIn(max = 680.dp)
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(top = topSafeInset + 65.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer { clip = false },
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when (content) {
                        is ResultDisplayState.ExemptionResult -> ExemptionResultContent(content)
                        is ResultDisplayState.AnnualResult -> AnnualResultContent(content)
                        is ResultDisplayState.MonthlyResult -> MonthlyResultContent(content)
                        is ResultDisplayState.Term2Result -> Term2ResultContent(content)
                        is ResultDisplayState.StudentProfileResult -> {
                            com.Nightjar.caliq5.ui.result.StudentProfileResultContent(content)
                        }
                    }
                }
                val bottomSafePadding = WindowInsets.safeDrawing.asPaddingValues().calculateBottomPadding()
                Spacer(modifier = Modifier.height(95.dp + bottomSafePadding))
            }

                        // Unified Floating Top Bar (Close Button, Title Capsule, PDF Button) with Zero Overlap Guarantee
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(top = topSafeInset + topPad)
                    .padding(start = startEdgePad, end = endEdgePad),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 1. Separate Floating Circular Close Button at Start Edge with Material Touch Morphing
                CaliqIconButton(
                    onClick = onClose,
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = if (isDark) 0.82f else 0.88f),
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    border = BorderStroke(
                        1.dp,
                        SolidColor(MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDark) 0.28f else 0.40f))
                    ),
                    modifier = Modifier.size(iconBtnSize)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "إغلاق",
                        modifier = Modifier.size(iconInnerSize)
                    )
                }

                // 2. Separate Floating Oval Title Capsule in the Center (Weight 1f, mathematically never collides)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = if (isUltraCompact) 4.dp else 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier
                            .height(topCapsuleHeight)
                            .widthIn(max = 460.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface.copy(alpha = if (isDark) 0.82f else 0.88f),
                        border = BorderStroke(
                            1.dp,
                            SolidColor(MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDark) 0.28f else 0.40f))
                        ),
                        shadowElevation = 0.dp
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(horizontal = if (isUltraCompact) 8.dp else if (isWideOrLandscape) 12.dp else 18.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            AnimatedContent(
                                targetState = title,
                                transitionSpec = {
                                    val easeOut = androidx.compose.animation.core.CubicBezierEasing(0.25f, 1f, 0.5f, 1f)
                                    (fadeIn(animationSpec = tween(300, easing = easeOut)) + 
                                     slideInVertically(animationSpec = androidx.compose.animation.core.spring(dampingRatio = 0.8f, stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow)) { it / 2 } +
                                     scaleIn(initialScale = 0.96f, animationSpec = tween(300, easing = easeOut))
                                    ).togetherWith(
                                        fadeOut(animationSpec = tween(220, easing = easeOut)) + 
                                        slideOutVertically(animationSpec = androidx.compose.animation.core.spring(dampingRatio = 0.8f, stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow)) { -it / 2 } +
                                        scaleOut(targetScale = 1.04f, animationSpec = tween(220, easing = easeOut))
                                    ).using(SizeTransform(clip = false))
                                },
                                label = "result_title_motion"
                            ) { targetTitle ->
                                Text(
                                    text = targetTitle,
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = if (isUltraCompact) 11.5.sp else if (isWideOrLandscape) 13.5.sp else 14.5.sp
                                    ),
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                // 3. Separate Floating Circular PDF Share Button at Screen End Edge with Material Touch Morphing
                CaliqIconButton(
                    onClick = onExportPdf,
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = if (isDark) 0.82f else 0.88f),
                    contentColor = MaterialTheme.colorScheme.primary,
                    border = BorderStroke(
                        1.dp,
                        SolidColor(MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDark) 0.28f else 0.40f))
                    ),
                    modifier = Modifier.size(iconBtnSize)
                ) {
                    Icon(
                        imageVector = Icons.Default.PictureAsPdf,
                        contentDescription = "تصدير PDF",
                        modifier = Modifier.size(iconInnerSize)
                    )
                }
            }

            // 4. Persistent AdMob Adaptive Banner at bottom center of Result Screen
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal)),
                contentAlignment = Alignment.Center
            ) {
                adBanner()
            }
        }
    }
}
