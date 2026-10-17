package com.Nightjar.caliq5.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import com.Nightjar.caliq5.theme.contrastingTextColor
import com.Nightjar.caliq5.theme.contrastingShadow
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Interactive 3D Perspective Tilt on touch / drag.
 * Tilts the card gently towards the user's touch location and springs back naturally when released.
 */
fun Modifier.interactive3DTilt(
    maxTiltDegrees: Float = 6.5f,
    enabled: Boolean = true
): Modifier = composed {
    if (!enabled) return@composed this

    var targetRotX by remember { mutableStateOf(0f) }
    var targetRotY by remember { mutableStateOf(0f) }

    val rotX by animateFloatAsState(
        targetValue = targetRotX,
        animationSpec = androidx.compose.animation.core.spring(dampingRatio = 0.60f, stiffness = 320f),
        label = "tilt_rot_x"
    )
    val rotY by animateFloatAsState(
        targetValue = targetRotY,
        animationSpec = androidx.compose.animation.core.spring(dampingRatio = 0.60f, stiffness = 320f),
        label = "tilt_rot_y"
    )

    this
        .pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) {
                    val event = awaitPointerEvent()
                    val change = event.changes.firstOrNull()
                    if (change != null) {
                        if (change.pressed) {
                            val w = size.width.toFloat()
                            val h = size.height.toFloat()
                            if (w > 0 && h > 0) {
                                val normX = ((change.position.x - w / 2f) / (w / 2f)).coerceIn(-1f, 1f)
                                val normY = ((change.position.y - h / 2f) / (h / 2f)).coerceIn(-1f, 1f)
                                targetRotX = -normY * maxTiltDegrees
                                targetRotY = normX * maxTiltDegrees
                            }
                        } else {
                            targetRotX = 0f
                            targetRotY = 0f
                        }
                    }
                }
            }
        }
        .graphicsLayer {
            rotationX = rotX
            rotationY = rotY
            cameraDistance = 16f * density
        }
}

@Composable
fun ResultCard(
    title: String = "",
    result: String = "",
    subtitle: String? = null,
    modifier: Modifier = Modifier.fillMaxWidth(),
    content: (@Composable ColumnScope.() -> Unit)? = null
) {
    val titleColor = MaterialTheme.colorScheme.primary
    val textColor = MaterialTheme.colorScheme.onSurface

    var isHovered by remember { mutableStateOf(false) }
    val elevation by animateDpAsState(
        targetValue = if (isHovered) 8.dp else 5.dp,
        animationSpec = androidx.compose.animation.core.spring(dampingRatio = 0.6f, stiffness = androidx.compose.animation.core.Spring.StiffnessLow)
    )
    
    val isDark = com.Nightjar.caliq5.theme.isAppInDarkTheme()
    val surfaceColor = MaterialTheme.colorScheme.surface
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer

    val cardShape = RoundedCornerShape(28.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 680.dp)
                .fillMaxWidth()
                .clip(cardShape)
                .background(surfaceColor)
                .background(
                    brush = Brush.linearGradient(
                        colors = if (isDark) {
                            listOf(
                                primaryContainer.copy(alpha = 0.28f),
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.60f),
                                surfaceColor
                            )
                        } else {
                            listOf(
                                primaryContainer.copy(alpha = 0.20f),
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.30f),
                                surfaceColor
                            )
                        }
                    )
                )
        ) {
        CompositionLocalProvider(LocalContentColor provides textColor) {
            Column(
                modifier = Modifier.padding(18.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (title.isNotEmpty()) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = titleColor,
                            fontSize = 16.sp
                        ),
                        modifier = Modifier.padding(bottom = 10.dp).fillMaxWidth(),
                        textAlign = TextAlign.Start
                    )
                }
                if (content != null) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        content()
                    }
                } else {
                    val shadowColor = if (isDark) Color.Black.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.7f)
                    Text(
                        text = result,
                        style = MaterialTheme.typography.displayMedium.copy(
                            shadow = androidx.compose.ui.graphics.Shadow(
                                color = shadowColor,
                                offset = Offset(1f, 1f),
                                blurRadius = 3f
                            )
                        ),
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    if (subtitle != null) {
                        Spacer(Modifier.height(4.dp))
                        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
    }
}

@Composable
fun FeatureExplanationCard(
    modifier: Modifier = Modifier,
    title: String = "",
    description: String = "",
    text: String = "",
    icon: String = "info"
) {
    val displayText = if (text.isNotEmpty()) text else description
    val iconVector = when (icon) {
        "workspace_premium" -> Icons.Default.Star
        "person_add" -> Icons.Default.PersonAdd
        "calculate" -> Icons.Default.Calculate
        "calendar_today" -> Icons.Default.CalendarToday
        "target" -> Icons.Default.TrackChanges
        "task_alt" -> Icons.Default.TaskAlt
        "school" -> Icons.Default.School
        else -> Icons.Default.Info
    }
    val isDark = com.Nightjar.caliq5.theme.isAppInDarkTheme()
    val primaryColor = MaterialTheme.colorScheme.primary
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val surfaceColor = MaterialTheme.colorScheme.surface
    val shape = RoundedCornerShape(28.dp)
    val isRtl = androidx.compose.ui.platform.LocalLayoutDirection.current == androidx.compose.ui.unit.LayoutDirection.Rtl

    // Smooth theme color gradient from 95% down to 10%
    val gradientColors = if (isRtl) {
        listOf(
            primaryColor.copy(alpha = if (isDark) 0.15f else 0.10f),
            primaryColor.copy(alpha = if (isDark) 0.38f else 0.45f),
            primaryColor.copy(alpha = if (isDark) 0.68f else 0.75f),
            primaryColor.copy(alpha = 0.95f)
        )
    } else {
        listOf(
            primaryColor.copy(alpha = 0.95f),
            primaryColor.copy(alpha = if (isDark) 0.68f else 0.75f),
            primaryColor.copy(alpha = if (isDark) 0.38f else 0.45f),
            primaryColor.copy(alpha = if (isDark) 0.15f else 0.10f)
        )
    }

    // Dynamic contrast determination based on actual composite background
    val effectiveBgColor = if (isDark) {
        primaryColor.copy(alpha = 0.65f)
    } else {
        Color(
            red = primaryColor.red * 0.35f + surfaceColor.red * 0.65f,
            green = primaryColor.green * 0.35f + surfaceColor.green * 0.65f,
            blue = primaryColor.blue * 0.35f + surfaceColor.blue * 0.65f
        )
    }
    val cardTextColor = effectiveBgColor.contrastingTextColor(
        darkText = Color(0xFF0F172A),
        lightText = Color.White
    )
    val cardTitleColor = if (cardTextColor == Color.White) Color.White else Color(0xFF0F172A)

    // Deep diffused Gaussian blur aura shadow without character repetition (ظل ضبابي كالهالة يبرز النص بنقاء بدون تكرار الحروف)
    val cardTextShadow = if (cardTextColor == Color.White) {
        androidx.compose.ui.graphics.Shadow(
            color = Color.Black.copy(alpha = if (isDark) 0.90f else 0.95f),
            offset = androidx.compose.ui.geometry.Offset.Zero,
            blurRadius = 16f
        )
    } else {
        androidx.compose.ui.graphics.Shadow(
            color = Color.White.copy(alpha = if (isDark) 0.90f else 0.98f),
            offset = androidx.compose.ui.geometry.Offset.Zero,
            blurRadius = 15f
        )
    }

    val cardTitleShadow = if (cardTitleColor == Color.White) {
        androidx.compose.ui.graphics.Shadow(
            color = Color.Black.copy(alpha = if (isDark) 0.90f else 0.95f),
            offset = androidx.compose.ui.geometry.Offset.Zero,
            blurRadius = 16f
        )
    } else {
        androidx.compose.ui.graphics.Shadow(
            color = Color.White.copy(alpha = if (isDark) 0.90f else 0.98f),
            offset = androidx.compose.ui.geometry.Offset.Zero,
            blurRadius = 15f
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 680.dp)
                .fillMaxWidth()
                .interactive3DTilt(maxTiltDegrees = 6.5f)
                .neuralAmbientGlow(
                    cornerRadius = 28.dp,
                    elevation = 6.dp,
                    tintColor = primaryColor,
                    hasSaltMotes = true
                )
                .clip(shape)
                .background(surfaceColor.copy(alpha = 0.20f))
                .background(
                    brush = Brush.horizontalGradient(
                        colors = gradientColors
                    )
                )
                .expressive3DCurvature(cornerRadius = 28.dp, shape = shape, tintColor = primaryColor)
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isDark) MaterialTheme.colorScheme.surface.copy(alpha = 0.90f) else MaterialTheme.colorScheme.surface,
                    shadowElevation = 0.dp,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = iconVector,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 44.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    if (title.isNotEmpty()) {
                        Text(
                            title,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium.copy(
                                shadow = cardTitleShadow
                            ),
                            color = cardTitleColor,
                            textAlign = TextAlign.Justify
                        )
                        Spacer(Modifier.height(4.dp))
                    }
                    AnimatedContent(
                        targetState = displayText,
                        transitionSpec = {
                            (fadeIn(animationSpec = tween(300, easing = androidx.compose.animation.core.LinearOutSlowInEasing)) +
                             slideInVertically(animationSpec = tween(300, easing = androidx.compose.animation.core.LinearOutSlowInEasing)) { it / 4 })
                                .togetherWith(
                                    fadeOut(animationSpec = tween(200, easing = androidx.compose.animation.core.FastOutLinearInEasing)) +
                                    slideOutVertically(animationSpec = tween(200, easing = androidx.compose.animation.core.FastOutLinearInEasing)) { -it / 4 }
                                )
                                .using(SizeTransform(clip = false))
                        },
                        label = "explanation_text_anim"
                    ) { targetText ->
                        Text(
                            text = targetText,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                lineHeight = 22.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = cardTextColor,
                                shadow = cardTextShadow
                            ),
                            textAlign = TextAlign.Justify,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MainGradeBox(
    title: String,
    grade: String,
    subtitle: String? = null,
    isSecondary: Boolean = false,
    gradientColors: List<Color>? = null
) {
    val isDark = com.Nightjar.caliq5.theme.isAppInDarkTheme()
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val baseColor = gradientColors?.firstOrNull() ?: primaryColor
    val dynamicTextColor = baseColor.contrastingTextColor(
        darkText = Color(0xFF0F172A),
        lightText = Color.White
    )
    val secondaryGradientColor = if (isSecondary) secondaryColor else primaryColor.copy(alpha = 0.75f)
    val boxShape = RoundedCornerShape(28.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 680.dp)
                .fillMaxWidth()
                .interactive3DTilt(maxTiltDegrees = 6.5f)
                .neuralAmbientGlow(
                    cornerRadius = 28.dp,
                    elevation = 7.dp,
                    tintColor = gradientColors?.firstOrNull() ?: primaryColor,
                    hasSaltMotes = true
                )
                .clip(boxShape)
                .background(
                    brush = Brush.linearGradient(
                        colors = gradientColors ?: listOf(primaryColor, secondaryGradientColor)
                    )
                )
                .expressive3DCurvature(cornerRadius = 28.dp, shape = boxShape, tintColor = gradientColors?.firstOrNull() ?: primaryColor)
        ) {
                val cardTextShadow = if (dynamicTextColor == Color.White) {
                    androidx.compose.ui.graphics.Shadow(
                        color = Color.Black.copy(alpha = if (isDark) 0.50f else 0.40f),
                        offset = androidx.compose.ui.geometry.Offset(0f, 1.5f),
                        blurRadius = 4.0f
                    )
                } else {
                    androidx.compose.ui.graphics.Shadow(
                        color = Color.White.copy(alpha = 0.60f),
                        offset = androidx.compose.ui.geometry.Offset(0f, 1.2f),
                        blurRadius = 3.5f
                    )
                }
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 26.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = dynamicTextColor.copy(alpha = 0.95f),
                            fontSize = 17.sp,
                            shadow = cardTextShadow
                        ),
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = grade,
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontWeight = FontWeight.Black,
                            color = dynamicTextColor,
                            fontSize = 46.sp,
                            letterSpacing = (-0.5).sp,
                            shadow = cardTextShadow
                        ),
                        textAlign = TextAlign.Center
                    )
                    if (!subtitle.isNullOrEmpty()) {
                        Spacer(Modifier.height(10.dp))
                        Surface(
                            color = Color.Black.copy(alpha = 0.22f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    color = dynamicTextColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp,
                                    shadow = cardTextShadow
                                ),
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }
        }
    }

@Composable
fun Term2GradeBox(
    requiredGrade: Double,
    isPossible: Boolean,
    gradientColors: List<Color>? = null
) {
    val isDark = com.Nightjar.caliq5.theme.isAppInDarkTheme()
    val primaryColor = MaterialTheme.colorScheme.primary
    val reqValue = if (requiredGrade < 0) 0.0 else requiredGrade
    val boxShape = RoundedCornerShape(28.dp)
    
    val actualGradient = if (gradientColors != null) {
        gradientColors
    } else if (isPossible) {
        listOf(primaryColor, primaryColor.copy(alpha = 0.75f))
    } else {
        listOf(Color(0xFFDC2626), Color(0xFFB91C1C))
    }

    val baseColor = actualGradient.firstOrNull() ?: primaryColor
    val dynamicTextColor = baseColor.contrastingTextColor(
        darkText = Color(0xFF0F172A),
        lightText = Color.White
    )
    val cardTextShadow = if (dynamicTextColor == Color.White) {
        androidx.compose.ui.graphics.Shadow(
            color = Color.Black.copy(alpha = if (isDark) 0.50f else 0.40f),
            offset = androidx.compose.ui.geometry.Offset(0f, 1.5f),
            blurRadius = 4.0f
        )
    } else {
        androidx.compose.ui.graphics.Shadow(
            color = Color.White.copy(alpha = 0.60f),
            offset = androidx.compose.ui.geometry.Offset(0f, 1.2f),
            blurRadius = 3.5f
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 680.dp)
                .fillMaxWidth()
                .interactive3DTilt(maxTiltDegrees = 6.5f)
                .neuralAmbientGlow(
                    cornerRadius = 28.dp,
                    elevation = 7.dp,
                    tintColor = actualGradient.firstOrNull() ?: primaryColor,
                    hasSaltMotes = true
                )
                .clip(boxShape)
                .background(
                    brush = Brush.linearGradient(actualGradient)
                )
                .expressive3DCurvature(cornerRadius = 28.dp, shape = boxShape, tintColor = actualGradient.firstOrNull() ?: primaryColor)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 26.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
            Text(
                text = "تحتاج إلى معدل",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = dynamicTextColor.copy(alpha = 0.95f),
                    fontSize = 17.sp,
                    shadow = cardTextShadow
                ),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = formatDecimalGrade(reqValue),
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Black,
                        color = dynamicTextColor,
                        fontSize = 46.sp,
                        letterSpacing = (-0.5).sp,
                        shadow = cardTextShadow
                    ),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "%",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = dynamicTextColor,
                        shadow = cardTextShadow
                    ),
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = "درجة في الفصل الثاني",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = dynamicTextColor.copy(alpha = 0.95f),
                    fontSize = 13.5.sp,
                    shadow = cardTextShadow
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}
}

@Composable
fun GradeDetailRow(
    label: String = "",
    value: String = "",
    isBold: Boolean = false,
    subjectName: String = "",
    grade: Double? = null,
    originalGrade: Double? = null,
    isRounded: Boolean = false,
    isSkipped: Boolean = false,
    showExemptionLabels: Boolean = false
) {
    if (label.isNotEmpty() || value.isNotEmpty()) {
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal)
            Text(value, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal)
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(subjectName, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal)
            
            if (isSkipped) {
                Text("لا تشملني", color = MaterialTheme.colorScheme.outline, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal)
            } else if (grade != null) {
                val isIndivExempt = grade >= 90.0
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (showExemptionLabels && isIndivExempt) {
                        Surface(
                            color = com.Nightjar.caliq5.theme.successColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "إعفاء فردي",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = com.Nightjar.caliq5.theme.successColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    if (isRounded && originalGrade != null) {
                        Text(
                            text = "(${formatDecimalGrade(originalGrade, maxDecimals = 4)}) جبر",
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.primary),
                        )
                    }
                    Text(
                        text = "${formatDecimalGrade(grade, maxDecimals = 6)}%",
                        fontWeight = FontWeight.Bold,
                        color = if (isIndivExempt) com.Nightjar.caliq5.theme.successColor else if (grade < 50.0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                }
            } else {
                Text("N/A", fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal)
            }
        }
    }
}

/**
 * Compact golden spiritual card with clean Glassmorphism & Neumorphism translucent background
 * matching other app cards, and sweeping golden glittering calligraphy.
 */
@Composable
fun GoldenSpiritualCard(modifier: Modifier = Modifier) {
    val isDark = com.Nightjar.caliq5.theme.isAppInDarkTheme()
    val primaryColor = MaterialTheme.colorScheme.primary
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val shape = RoundedCornerShape(20.dp)

    val transition = rememberInfiniteTransition(label = "spiritual_gold_shimmer")
    
    val shimmerOffset by transition.animateFloat(
        initialValue = -350f,
        targetValue = 900f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "gold_shimmer_offset"
    )

    val goldenShimmerBrush = Brush.linearGradient(
        colors = listOf(
            if (isDark) Color(0xFFFFD54F) else Color(0xFFC58E00),
            Color(0xFFFFF9C4),
            Color.White.copy(alpha = 0.98f),
            if (isDark) Color(0xFFFFB300) else Color(0xFFD48800),
            if (isDark) Color(0xFFFFD54F) else Color(0xFFC58E00)
        ),
        start = Offset(shimmerOffset, 0f),
        end = Offset(shimmerOffset + 380f, 60f)
    )

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = 680.dp)
                .fillMaxWidth()
                .glassFrame3D(cornerRadius = 20.dp, elevation = 4.dp),
            shape = shape,
            color = Color.Transparent
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                primaryContainer.copy(alpha = if (isDark) 0.50f else 0.40f),
                                primaryColor.copy(alpha = if (isDark) 0.20f else 0.12f),
                                MaterialTheme.colorScheme.surface.copy(alpha = if (isDark) 0.80f else 0.88f)
                            )
                        ),
                        shape = shape
                    )
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "بِاسْمِ الْعَلِيِّ الْعَظِيمِ، وَقُلْ هُوَ اللَّهُ أَحَدٌ، وَلَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ الْعَلِيِّ الْعَظِيمِ",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = com.Nightjar.caliq5.theme.ReemKufiFontFamily,
                        fontSize = 14.5.sp,
                        lineHeight = 22.sp,
                        brush = goldenShimmerBrush
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
