package com.Nightjar.caliq5.ui.components

import androidx.compose.ui.composed

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import com.Nightjar.caliq5.theme.isAppInDarkTheme
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * Elastic bounce/overscroll effect on vertical scrolling.
 */
fun Modifier.elasticOverscroll(maxStretchRatio: Float = 0.10f): Modifier = composed {
    var componentHeight by remember { mutableStateOf(0f) }
    val overscrollOffset = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val current = overscrollOffset.value
                if (current != 0f && source == NestedScrollSource.UserInput) {
                    val delta = available.y
                    if ((current > 0 && delta < 0) || (current < 0 && delta > 0)) {
                        val newOffset = current + delta
                        return if ((current > 0 && newOffset <= 0) || (current < 0 && newOffset >= 0)) {
                            scope.launch { overscrollOffset.snapTo(0f) }
                            Offset(0f, current)
                        } else {
                            scope.launch { overscrollOffset.snapTo(newOffset) }
                            Offset(0f, delta)
                        }
                    }
                }
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (source == NestedScrollSource.UserInput && available.y != 0f) {
                    val maxOffset = if (componentHeight > 0) componentHeight * maxStretchRatio else 140f
                    val current = overscrollOffset.value
                    val progress = (kotlin.math.abs(current) / maxOffset).coerceIn(0f, 1f)
                    val resistance = 0.35f * (1f - progress * progress)
                    val targetOffset = (current + available.y * resistance).coerceIn(-maxOffset, maxOffset)
                    scope.launch { overscrollOffset.snapTo(targetOffset) }
                    return Offset(0f, available.y)
                }
                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                if (overscrollOffset.value != 0f) {
                    overscrollOffset.animateTo(
                        0f,
                        spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    )
                    return available
                }
                return Velocity.Zero
            }
        }
    }

    this
        .onSizeChanged { componentHeight = it.height.toFloat() }
        .nestedScroll(nestedScrollConnection)
        .graphicsLayer {
            translationY = overscrollOffset.value
        }
}

/**
 * Fluid staggered sliding animation on screen load and quick navigations.
 * Replays smoothly on every navigation with an elegant, moist (Fluid) spring motion.
 */
fun Modifier.popInOnInitialLoad(index: Int = 0): Modifier = composed {
    val isHighPerf = com.Nightjar.caliq5.utils.rememberIsHighPerformanceDevice()
    if (!isHighPerf) return@composed this

    var isLoaded by remember { mutableStateOf(index == 0) }
    LaunchedEffect(Unit) {
        if (index > 0) {
            kotlinx.coroutines.delay((index.coerceAtMost(6) * 20L))
            isLoaded = true
        }
    }
    val animProgress by animateFloatAsState(
        targetValue = if (isLoaded) 1f else 0f,
        animationSpec = spring(
            dampingRatio = 0.82f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "fluidSlide_$index"
    )
    this.graphicsLayer {
        this.alpha = animProgress.coerceIn(0f, 1f)
        this.translationY = (1f - animProgress) * 18.dp.toPx()
        this.scaleX = 0.96f + 0.04f * animProgress
        this.scaleY = 0.96f + 0.04f * animProgress
        this.clip = false
    }
}


/**
 * Authentic 2.5D & 3D Specular Curved & Cloudy Glassmorphism & Neumorphism Card modifier.
 * Uses drawWithContent to create smooth, cloudy specular highlights and soft ambient bevel shadows
 * strictly adhering to the card's shape and inverting cleanly with the theme:
 * - Dark Mode: Luminous cloudy white specular sheen with soft frosted ambient glow.
 * - Light Mode: Crisp dark slate claymorphic/neumorphic bevel with soft ambient depth.
 */
@Composable
fun Modifier.glassFrame3D(
    cornerRadius: Dp = 20.dp,
    elevation: Dp = 4.dp,
    borderWidth: Dp = 1.3.dp,
    hasInnerGlow: Boolean = false
): Modifier = composed {
    val isHighPerf = com.Nightjar.caliq5.utils.rememberIsHighPerformanceDevice()
    val isDark = isAppInDarkTheme()
    val shape = RoundedCornerShape(cornerRadius)
    
    if (!isHighPerf) {
        // Adaptive Graceful Degradation for low-RAM / low-end devices:
        // Render a clean, crisp, lightweight border with zero dropped frames
        return@composed this
            .clip(shape)
            .border(
                androidx.compose.foundation.BorderStroke(borderWidth, MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDark) 0.35f else 0.50f)),
                shape = shape
            )
    }

    // Base 3D colors strictly inverting with theme
    val baseColor = if (isDark) Color.White else Color.Black

    val topHighlight = baseColor.copy(alpha = if (isDark) 0.55f else 0.22f)
    val midBevel = baseColor.copy(alpha = if (isDark) 0.28f else 0.32f)
    val bottomShadow = androidx.compose.ui.graphics.Color.Black.copy(alpha = if (isDark) 0.8f else 0.48f) // TRUE DEPTH SHADOW
    
    val tintBase = MaterialTheme.colorScheme.primary.copy(
        alpha = if (isDark) (if (hasInnerGlow) 0.14f else 0.04f) else (if (hasInnerGlow) 0.06f else 0.02f)
    )

    this
        .clip(shape)
        .drawWithCache {
            val radiusPx = cornerRadius.toPx()
            val w = size.width
            val h = size.height

            // 1. Soft cloudy 3D ambient specular sheen from top-left
            val cloudySheen = Brush.radialGradient(
                colors = listOf(
                    baseColor.copy(alpha = if (isDark) 0.14f else 0.06f),
                    Color.Transparent
                ),
                center = Offset(0f, 0f),
                radius = kotlin.math.max(w, h) * 0.75f
            )

            // 2. Authentic 3D Slanted/Bent Specular Bevel Border (Smooth, Anti-Aliased 360°)
            val outer3DBrush = Brush.linearGradient(
                colors = listOf(topHighlight, midBevel, bottomShadow),
                start = Offset(0f, 0f),
                end = Offset(w, h)
            )
            val strokeStyle = Stroke(width = borderWidth.toPx())

            onDrawWithContent {
                // Draw card background & content first
                drawContent()

                // Soft cloudy 3D ambient specular sheen
                drawRoundRect(
                    brush = cloudySheen,
                    size = size,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(radiusPx, radiusPx)
                )

                // Ambient inner tint if enabled
                if (hasInnerGlow) {
                    drawRoundRect(
                        color = tintBase,
                        size = size,
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(radiusPx, radiusPx)
                    )
                }

                // 3D Bevel border
                drawRoundRect(
                    brush = outer3DBrush,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(radiusPx, radiusPx),
                    style = strokeStyle
                )
            }
        }
}



/**
 * Wrapper for backward compatibility if needed, falling back to glassFrame3D
 */
@Composable
fun Modifier.neumorphicGlassCard(
    cornerRadius: Dp = 20.dp,
    elevation: Dp = 4.dp,
    borderWidth: Dp = 1.4.dp
): Modifier = this.glassFrame3D(cornerRadius, elevation, borderWidth)

@Composable
fun Modifier.neumorphic3D(interactionSource: MutableInteractionSource): Modifier {
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f)
    )
    val shadowElevation by animateFloatAsState(
        targetValue = if (isPressed) 2f else 8f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f)
    )
    return this
        .scale(scale)
        .shadow(shadowElevation.dp, RoundedCornerShape(16.dp))
}

/**
 * Fluid multi-phase animated title:
 * 1. Stays at rest for 2 seconds in its original theme color and font.
 * 2. Sweeps a bright specular gleam across the letters.
 * 3. Fluidly transitions into an iridescent chromatic color wave with ultra-soft easing.
 * 4. Dissolves seamlessly back into the resting state in an infinite smooth loop.
 */
@Composable
fun ShimmeringAppTitle(
    text: String = "حاسبة الإعفاء والسعي",
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.headlineSmall.copy(
        fontWeight = FontWeight.Bold,
        fontFamily = com.Nightjar.caliq5.theme.ReemKufiFontFamily
    ),
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Center
) {
    val transition = rememberInfiniteTransition(label = "fluid_title_cycle_trans")

    val cycleTime by transition.animateFloat(
        initialValue = 0f,
        targetValue = 7500f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 7500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "cycle_time_ms"
    )

    val defaultColor = MaterialTheme.colorScheme.primary
    val baseColor = if (style.color != Color.Unspecified) style.color else defaultColor

    val baseColors = listOf(
        Color(0xFF9C27B0), // Vivid Purple
        Color(0xFF3F51B5), // Indigo
        Color(0xFF00BCD4), // Cyan
        Color(0xFFFFB300), // Amber Gold
        Color(0xFFFF4081), // Pink
        Color(0xFF00E676), // Emerald
        Color(0xFF9C27B0)  // Loop
    )

    val currentBrush = when {
        // Phase 1: 2 Seconds Solid Rest in original color
        cycleTime < 2000f -> {
            Brush.linearGradient(listOf(baseColor, baseColor))
        }

        // Phase 2: Bright Specular Gleam Sweep across base color (~1.6s)
        cycleTime < 3600f -> {
            val p = ((cycleTime - 2000f) / 1600f).coerceIn(0f, 1f)
            val gleamOffset = -300f + p * 1200f
            Brush.linearGradient(
                colors = listOf(
                    baseColor,
                    baseColor,
                    Color.White.copy(alpha = 0.96f),
                    Color(0xFFFFF9C4),
                    baseColor,
                    baseColor
                ),
                start = Offset(gleamOffset, 0f),
                end = Offset(gleamOffset + 350f, 80f)
            )
        }

        // Phase 3: Fluid Chromatic Wave with Soft Blend (~2.6s)
        cycleTime < 6200f -> {
            val p = ((cycleTime - 3600f) / 2600f).coerceIn(0f, 1f)
            val waveOffset = -250f + p * 1100f
            val shift = (p * 5).toInt()
            val blendFactor = (kotlin.math.sin(p * kotlin.math.PI)).toFloat().coerceIn(0f, 1f)
            
            val c1 = lerp(baseColor, baseColors[shift % baseColors.size], blendFactor)
            val c2 = lerp(baseColor, baseColors[(shift + 1) % baseColors.size], blendFactor)
            val c3 = lerp(baseColor, Color.White.copy(alpha = 0.95f), blendFactor)
            val c4 = lerp(baseColor, baseColors[(shift + 3) % baseColors.size], blendFactor)
            val c5 = lerp(baseColor, baseColors[(shift + 4) % baseColors.size], blendFactor)

            Brush.linearGradient(
                colors = listOf(c1, c2, c3, c4, c5),
                start = Offset(waveOffset, 0f),
                end = Offset(waveOffset + 420f, 120f)
            )
        }

        // Phase 4: Seamless Dissolve Back to Base Color (~1.3s)
        else -> {
            val p = ((cycleTime - 6200f) / 1300f).coerceIn(0f, 1f)
            val fadeBlend = (1f - p).coerceIn(0f, 1f)
            val c1 = lerp(baseColor, baseColors[0], fadeBlend)
            val c2 = lerp(baseColor, baseColors[2], fadeBlend)
            Brush.linearGradient(
                colors = listOf(c1, c2, baseColor)
            )
        }
    }

    Text(
        text = text,
        style = style.copy(brush = currentBrush),
        modifier = modifier,
        textAlign = textAlign
    )
}

@Composable
fun ShimmeringText(
    text: String,
    style: androidx.compose.ui.text.TextStyle,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "shimmer_text_trans")
    val translateAnim by transition.animateFloat(
        initialValue = -500f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_text_offset"
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val shimmerColors = listOf(
        primaryColor,
        Color.White.copy(alpha = 0.95f),
        primaryColor
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim, translateAnim),
        end = Offset(translateAnim + 300f, translateAnim + 300f)
    )

    Text(
        text = text,
        style = style.copy(brush = brush),
        modifier = modifier
    )
}

@Composable
fun MorphingStarLoadingIndicator(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    size: Dp = 48.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "m3_expressive_loading")

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val morphProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "morph"
    )

    // Squishy elastic breathing (stretch & squash)
    val scaleX by infiniteTransition.animateFloat(
        initialValue = 0.84f,
        targetValue = 1.16f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scaleX"
    )
    val scaleY by infiniteTransition.animateFloat(
        initialValue = 1.16f,
        targetValue = 0.84f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scaleY"
    )

    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                rotationZ = rotation
                this.scaleX = scaleX
                this.scaleY = scaleY
            },
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val radius = minOf(this.size.width, this.size.height) / 2.25f
            
            // Draw multi-lobed expressive star / morphing blossom
            val path = androidx.compose.ui.graphics.Path()
            val points = 8
            val innerRadius = radius * (0.52f + 0.36f * morphProgress)
            val outerRadius = radius

            for (i in 0 until points * 2) {
                val r = if (i % 2 == 0) outerRadius else innerRadius
                val angle = (i * kotlin.math.PI / points)
                val x = center.x + (r * kotlin.math.cos(angle)).toFloat()
                val y = center.y + (r * kotlin.math.sin(angle)).toFloat()
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            path.close()

            // Soft radiant ambient glow
            drawPath(
                path = path,
                color = color.copy(alpha = 0.22f),
                style = Stroke(
                    width = 8.dp.toPx(),
                    cap = androidx.compose.ui.graphics.StrokeCap.Round,
                    join = androidx.compose.ui.graphics.StrokeJoin.Round
                )
            )
            // Vibrant primary stroke
            drawPath(
                path = path,
                color = color,
                style = Stroke(
                    width = 3.6.dp.toPx(),
                    cap = androidx.compose.ui.graphics.StrokeCap.Round,
                    join = androidx.compose.ui.graphics.StrokeJoin.Round
                )
            )
            // Inner pulsating core
            drawCircle(
                color = color.copy(alpha = 0.85f),
                radius = radius * (0.22f + 0.08f * (1f - morphProgress)),
                center = center
            )
        }
    }
}


// Instant Neural Expressive 3D Shadow (Cloudy/Ambient Theme Glow)
fun Modifier.neuralExpressiveShadow(
    cornerRadius: androidx.compose.ui.unit.Dp = 28.dp,
    elevation: androidx.compose.ui.unit.Dp = 14.dp,
    tintColor: androidx.compose.ui.graphics.Color? = null
): Modifier = composed {
    val isDark = isAppInDarkTheme()
    val themeColor = tintColor ?: MaterialTheme.colorScheme.primary
    
    // Neural Expressive M3E: Theme-tinted cloudy ambient + directional elevation
    val ambientColor = if (isDark) themeColor.copy(alpha = 0.35f) else themeColor.copy(alpha = 0.16f)
    val spotColor = if (isDark) themeColor.copy(alpha = 0.48f) else themeColor.copy(alpha = 0.24f)
    
    this.shadow(
        elevation = elevation,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(cornerRadius),
        ambientColor = ambientColor,
        spotColor = spotColor,
        clip = false
    )
}

/**
 * 100% Instant 4-sided Neural Expressive ambient depth.
 * Renders directly on Canvas via drawBehind with continuous 5-step quadratic diffusion falloff:
 * - Zero RenderNode frame lag during scrolling or screen transitions.
 * - Zero rectangular / sharp bounding box artifacts.
 * - Soft ambient cloudy theme glow strictly mirroring cornerRadius from all 4 sides.
 * - No hard/solid borders.
 */
/**
 * 4-sided Neural Expressive ambient depth with authentic floating salt/dust spray (رذاذ ملحي وغباري بلون الثيم).
 * Renders an ultra-soft Skia diffuse shadow combined with floating organic salt motes outside the element bounds.
 * When [hasSaltMotes] is false, only renders the soft diffused ambient dust glow (for bottom/side navigation bars).
 */
fun Modifier.neuralAmbientGlow(
    cornerRadius: androidx.compose.ui.unit.Dp = 28.dp,
    elevation: androidx.compose.ui.unit.Dp = 0.dp,
    tintColor: androidx.compose.ui.graphics.Color? = null,
    hasSaltMotes: Boolean = false
): Modifier = this

/**
 * Longitudinal Sheen (اللمحة الطولية الخفيفة 10%):
 * 10% white in Dark mode, 9-10% subtle black in Light mode fading smoothly down from the top.
 * Applied to the bottom navigation bar, side rail, and cards.
 */
fun Modifier.longitudinalSheen(
    cornerRadius: androidx.compose.ui.unit.Dp = 24.dp,
    shape: androidx.compose.ui.graphics.Shape? = null
): Modifier = composed {
    val isDark = isAppInDarkTheme()
    val effectiveShape = shape ?: RoundedCornerShape(cornerRadius)

    this
        .clip(effectiveShape)
        .drawWithCache {
            val radiusPx = cornerRadius.toPx()
            val sheenColor = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.04f)
            val sheenBrush = Brush.verticalGradient(
                0.0f to sheenColor,
                (if (isDark) 0.18f else 0.22f) to Color.Transparent,
                1.0f to Color.Transparent
            )

            onDrawWithContent {
                drawContent()
                drawRoundRect(
                    brush = sheenBrush,
                    size = size,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(radiusPx, radiusPx)
                )
            }
        }
}

/**
 * 3D Convex Curvature (التحدب والاعوجاج ثلاثي الأبعاد المحدب العضوي الناعم كالصورة المرجعية):
 * - Creates an authentic convex volume with organic curvature using the element's OWN theme color (Tone-on-tone).
 * - ZERO harsh white specular rays, ZERO snowy frost lines washing out the theme color in light mode.
 * - ZERO bottom drop shadows. Pure convex surface volume.
 */
fun Modifier.expressive3DCurvature(
    cornerRadius: androidx.compose.ui.unit.Dp = 24.dp,
    shape: androidx.compose.ui.graphics.Shape? = null,
    tintColor: androidx.compose.ui.graphics.Color? = null
): Modifier = composed {
    val isDark = isAppInDarkTheme()
    val effectiveShape = shape ?: RoundedCornerShape(cornerRadius)
    val themeColor = tintColor ?: MaterialTheme.colorScheme.primary

    this
        .clip(effectiveShape)
        .drawWithCache {
            val radiusPx = cornerRadius.toPx()
            val w = size.width
            val h = size.height

            // 1. Organic Tone-on-tone Volume: Soft central bulge with richer tone at bottom & perimeter
            val volumeBrush = if (isDark) {
                Brush.verticalGradient(
                    0.0f to Color.White.copy(alpha = 0.08f),
                    0.45f to Color.Transparent,
                    1.0f to Color.Black.copy(alpha = 0.20f)
                )
            } else {
                // In Light mode: NEVER wash out with flat white. Use gentle depth gradient from same theme tone!
                Brush.verticalGradient(
                    0.0f to Color.White.copy(alpha = 0.06f),
                    0.40f to Color.Transparent,
                    1.0f to themeColor.copy(alpha = 0.10f)
                )
            }

            // 2. Soft perimeter bezel that contours the pill corners seamlessly
            val perimeterCurvature = Brush.radialGradient(
                colors = listOf(
                    Color.Transparent,
                    (if (isDark) Color.Black.copy(alpha = 0.16f) else themeColor.copy(alpha = 0.07f))
                ),
                center = Offset(w * 0.5f, h * 0.5f),
                radius = kotlin.math.max(w, h) * 0.70f
            )

            onDrawWithContent {
                // 1. رسم محتوى الكارت والنصوص أولاً
                drawContent()

                // 2. رسم التحدب الحجمي العضوي الناعم فوق السطح ليعطي إحساس المجسم المحدب دون طمس النص وبدون أي ظلال خارجية
                drawRoundRect(
                    brush = volumeBrush,
                    size = size,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(radiusPx, radiusPx)
                )
                drawRoundRect(
                    brush = perimeterCurvature,
                    size = size,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(radiusPx, radiusPx)
                )
            }
        }
}

/**
 * Frosted Organic Capsule (تصميم الكبسولة الزجاجية العضوية شبه الشفافة Sea Glass):
 * - Translucent frosted sea glass base tinted with theme color.
 * - Isolated and simplified gracefully on low-end devices.
 * - Zero heavy bottom drop shadows.
 */
fun Modifier.frostedOrganicCapsule(
    shape: Shape = CircleShape,
    cornerRadius: Dp = 36.dp,
    tintColor: Color? = null,
    alpha: Float = 0.25f
): Modifier = composed {
    val isHighPerf = com.Nightjar.caliq5.utils.rememberIsHighPerformanceDevice()
    val isDark = isAppInDarkTheme()
    val themeColor = tintColor ?: MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface

    if (!isHighPerf) {
        return@composed this
            .clip(shape)
            .background(if (isDark) surfaceColor.copy(alpha = 0.90f) else surfaceColor.copy(alpha = 0.95f))
            .border(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDark) 0.35f else 0.45f),
                shape
            )
    }

    // Semi-transparent frosted Sea Glass base tinted naturally with theme and surface
    val capsuleBg = if (isDark) {
        Color(
            red = surfaceColor.red * 0.60f + themeColor.red * 0.40f,
            green = surfaceColor.green * 0.60f + themeColor.green * 0.40f,
            blue = surfaceColor.blue * 0.60f + themeColor.blue * 0.40f,
            alpha = alpha.coerceIn(0.20f, 0.45f)
        )
    } else {
        Color(
            red = surfaceColor.red * 0.45f + themeColor.red * 0.55f,
            green = surfaceColor.green * 0.45f + themeColor.green * 0.55f,
            blue = surfaceColor.blue * 0.45f + themeColor.blue * 0.55f,
            alpha = alpha.coerceIn(0.18f, 0.35f)
        )
    }

    this
        .clip(shape)
        .background(capsuleBg)
        .drawWithCache {
            val w = size.width
            val h = size.height
            val radiusPx = cornerRadius.toPx().coerceAtMost(minOf(w, h) / 2f)

            // Smooth Sea Glass 3D volume gradient without harsh highlights
            val convexVolumeBrush = Brush.verticalGradient(
                0.0f to (if (isDark) Color.White.copy(alpha = 0.10f) else Color.White.copy(alpha = 0.12f)),
                0.35f to Color.Transparent,
                1.0f to (if (isDark) Color.Black.copy(alpha = 0.15f) else themeColor.copy(alpha = 0.08f))
            )

            // Micro Bevel Rim for delicate Sea Glass edge definition
            val borderSheen = Brush.verticalGradient(
                0.0f to (if (isDark) Color.White.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.22f)),
                0.60f to themeColor.copy(alpha = if (isDark) 0.06f else 0.08f),
                1.0f to themeColor.copy(alpha = if (isDark) 0.14f else 0.12f)
            )

            onDrawWithContent {
                // 1. Draw convex volume
                drawRoundRect(
                    brush = convexVolumeBrush,
                    size = size,
                    cornerRadius = CornerRadius(radiusPx, radiusPx)
                )
                // 2. Draw content (icons, text)
                drawContent()
                // 3. Draw fine Sea Glass perimeter
                drawRoundRect(
                    brush = borderSheen,
                    size = size,
                    cornerRadius = CornerRadius(radiusPx, radiusPx),
                    style = Stroke(width = 1.dp.toPx())
                )
            }
        }
}

