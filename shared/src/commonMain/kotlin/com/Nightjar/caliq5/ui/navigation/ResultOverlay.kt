package com.Nightjar.caliq5.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
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
) {
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    LaunchedEffect(Unit) {
        focusManager.clearFocus()
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            // Background surface
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background.copy(alpha = 0.98f),
            ) {}

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .widthIn(max = 680.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.TopCenter
            ) {
                // Scrollable Content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)
                        .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 65.dp) 
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
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
                    Spacer(modifier = Modifier.height(32.dp))
                }

                val isDark = com.Nightjar.caliq5.theme.isAppInDarkTheme()
                val isWideOrLandscape = maxWidth > 600.dp || maxHeight < 500.dp
                val topPad = if (isWideOrLandscape) 4.dp else 8.dp
                val topCapsuleHeight = if (isWideOrLandscape) 36.dp else 42.dp
                val iconBtnSize = if (isWideOrLandscape) 36.dp else 40.dp
                val iconInnerSize = if (isWideOrLandscape) 18.dp else 22.dp
                val titleSideClearance = if (isWideOrLandscape) 56.dp else 64.dp

                // 1. Separate Floating Circular Close Button at Start Edge (الطرف) with Material Touch Morphing
                CaliqIconButton(
                    onClick = onClose,
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = if (isDark) 0.82f else 0.88f),
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDark) 0.28f else 0.40f)
                    ),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .statusBarsPadding()
                        .padding(top = topPad, start = 16.dp)
                        .size(iconBtnSize)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "إغلاق",
                        modifier = Modifier.size(iconInnerSize)
                    )
                }

                // 2. Separate Floating Oval Title Capsule in the Center (الوسط)
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .statusBarsPadding()
                        .padding(top = topPad)
                        .padding(horizontal = titleSideClearance)
                        .height(topCapsuleHeight)
                        .widthIn(min = 130.dp, max = 460.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = if (isDark) 0.82f else 0.88f),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDark) 0.28f else 0.40f)
                    ),
                    shadowElevation = 0.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(horizontal = if (isWideOrLandscape) 14.dp else 20.dp),
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
                                    fontSize = if (isWideOrLandscape) 14.sp else 15.sp
                                ),
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // 3. Separate Floating Circular PDF Share Button at End Edge (الطرف) with Material Touch Morphing
                CaliqIconButton(
                    onClick = onExportPdf,
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = if (isDark) 0.82f else 0.88f),
                    contentColor = MaterialTheme.colorScheme.primary,
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDark) 0.28f else 0.40f)
                    ),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .statusBarsPadding()
                        .padding(top = topPad, end = 16.dp)
                        .size(iconBtnSize)
                ) {
                    Icon(
                        imageVector = Icons.Default.PictureAsPdf,
                        contentDescription = "تصدير PDF",
                        modifier = Modifier.size(iconInnerSize)
                    )
                }
            }
        }
    }
}
