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

                // Docked top header matching web app .result-header exactly with clear bottom border
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
                    shadowElevation = 0.dp,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                            )
                        )
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // 1. Close button on the far right (Start in RTL)
                        CaliqIconButton(
                            onClick = onClose,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "إغلاق",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        val titleFontSize = when {
                            title.length > 25 -> 14.sp
                            title.length > 20 -> 17.sp
                            else -> 22.sp
                        }

                        // 2. Title in the middle with transition animation
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            AnimatedContent(
                                targetState = title,
                                transitionSpec = {
                                    (fadeIn(tween(300)) + slideInHorizontally { it }).togetherWith(
                                        fadeOut(tween(200)) + slideOutHorizontally { -it }
                                    )
                                },
                                label = "result_title_motion"
                            ) { targetTitle ->
                                Text(
                                    text = targetTitle,
                                    textAlign = TextAlign.Start,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = titleFontSize
                                    ),
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // 3. PDF button on the far left (End in RTL)
                        CaliqIconButton(
                            onClick = onExportPdf,
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = "تصدير PDF",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
