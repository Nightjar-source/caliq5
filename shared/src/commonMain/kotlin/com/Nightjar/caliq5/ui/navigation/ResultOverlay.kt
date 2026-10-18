package com.Nightjar.caliq5.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
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

                // Floating Oval Capsule Top Bar (Unified with all screens, < 60dp height, translucent glass)
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .statusBarsPadding()
                        .padding(top = 8.dp)
                        .padding(horizontal = 16.dp)
                        .widthIn(max = 560.dp)
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = if (isDark) 0.82f else 0.88f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDark) 0.28f else 0.40f)
                    ),
                    shadowElevation = 0.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // 1. Close button on the far right (Start in RTL)
                        IconButton(
                            onClick = onClose,
                            modifier = Modifier.size(36.dp).clip(CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "إغلاق",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // 2. Title in the middle with transition animation
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 4.dp),
                            contentAlignment = Alignment.CenterStart
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
                                    textAlign = TextAlign.Start,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 15.sp
                                    ),
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        // 3. PDF button on the far left (End in RTL)
                        IconButton(
                            onClick = onExportPdf,
                            modifier = Modifier.size(36.dp).clip(CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = "تصدير PDF",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
