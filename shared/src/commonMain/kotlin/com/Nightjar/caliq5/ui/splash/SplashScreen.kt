package com.Nightjar.caliq5.ui.splash

import androidx.compose.material.icons.filled.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextStyle
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import caliq5.shared.generated.resources.Res
import caliq5.shared.generated.resources.icon
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * شاشة البداية المتحركة - تطابق splash.html
 * - خلفية متدرجة متغيرة اللون (gradientAnimation)
 * - جسيمات عائمة (particles): رموز رياضية + إيموجي
 * - أيقونة التطبيق مع نبض + اسم التطبيق
 * - تختفي تلقائياً بعد 2.5 ثانية
 */
@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    var contentAlpha by remember { mutableStateOf(1f) }

    val animatedAlpha by animateFloatAsState(
        targetValue = contentAlpha,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 350, easing = androidx.compose.animation.core.CubicBezierEasing(0.25f, 1f, 0.5f, 1f)),
        label = "splash_alpha"
    )
    val animatedScale by animateFloatAsState(
        targetValue = if (contentAlpha > 0f) 1f else 0.96f,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioLowBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow
        ),
        label = "splash_scale"
    )

    LaunchedEffect(Unit) {
        delay(1800) // 1.8 seconds duration
        onSplashFinished()
    }

    // === Animated gradient background ===
    val gradientTransition = rememberInfiniteTransition(label = "gradient")
    val gradientAngle by gradientTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "gradient_angle"
    )

    // Gradient colors from splash.html: #1c5b7d, #2a0845, #6441A5, #2a0845
    val gradientColors = listOf(
        Color(0xFF1C5B7D),
        Color(0xFF2A0845),
        Color(0xFF6441A5),
        Color(0xFF2A0845),
    )

    // === Particle symbols ===
    data class Particle(
        val text: String,
        val x: Float,         // 0..1
        val delay: Float,     // random starting offset
        val size: Float,      // sp
        val isEmoji: Boolean,
        val speedMultiplier: Float,
    )

    val splashSymbols = listOf("+", "-", "÷", "×", "∆", "π", "√", "=", "°", "٪")
    val splashEmojis = listOf("✨", "📐", "☄️", "📃", "+", "-", "÷", "×", "∆", "π", "√")

    val particles = remember {
        (0 until 12).map {
            val sym = splashEmojis[it % splashEmojis.size]
            val isEmoji = !splashSymbols.contains(sym)
            Particle(
                text = sym,
                x = Random.nextFloat(),
                delay = Random.nextFloat(),
                size = 16f + Random.nextFloat() * 20f,
                isEmoji = isEmoji,
                speedMultiplier = 0.6f + Random.nextFloat() * 0.8f,
            )
        }
    }

    val particleTransition = rememberInfiniteTransition(label = "particles_splash")
    val masterProgress by particleTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 10_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "master_particle_progress"
    )

    // === Icon pulse (sub-second smooth rhythmic heartbeat) ===
    val pulseTransition = rememberInfiniteTransition(label = "icon_pulse")
    val iconScale by pulseTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 950, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "icon_scale"
    )

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize().alpha(animatedAlpha),
        contentAlignment = Alignment.Center
    ) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight

        // Layer 1: Exact 135-degree linear gradient matching splash_background.xml (#1C5B7D -> #2A0845)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF1C5B7D),
                            Color(0xFF2A0845)
                        ),
                        start = Offset.Zero,
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    )
                )
        )

        // Layer 2: Floating particles
        particles.forEachIndexed { index, particle ->
            val rawProgress = masterProgress * particle.speedMultiplier + particle.delay
            val progress = rawProgress - kotlin.math.floor(rawProgress)
            val currentY = 1.1f - 1.4f * progress
            val currentX = particle.x
            
            val opacity = when {
                progress < 0.1f -> progress / 0.1f
                progress > 0.9f -> (1f - progress) / 0.1f
                else -> 1f
            }
            
            Box(
                modifier = Modifier
                    .offset(
                        x = screenWidth * (currentX - 0.5f),
                        y = screenHeight * (currentY - 0.5f)
                    )
                    .alpha(opacity * (if (particle.isEmoji) 0.5f else 0.35f))
                    .scale(0.8f + 0.4f * progress)
                    .rotate(360f * progress)
            ) {
                Text(
                    text = particle.text,
                    color = Color.White,
                    fontSize = particle.size.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Layer 3: Center content
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .alpha(animatedAlpha)
                .scale(animatedScale)
        ) {
            // App icon exactly centered
            org.jetbrains.compose.resources.painterResource(Res.drawable.icon)?.let { painter ->
                androidx.compose.foundation.Image(
                    painter = painter,
                    contentDescription = "أيقونة التطبيق",
                    modifier = Modifier
                        .size(160.dp)
                        .scale(iconScale)
                        .clip(RoundedCornerShape(35.dp))
                )
            }
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.offset(y = 145.dp)
            ) {
                // App title - matches splash.html h1.app-title with rainbow glitter shimmer
                com.Nightjar.caliq5.ui.components.ShimmeringAppTitle(
                    text = "حاسبة الإعفاء والسعي",
                    style = TextStyle(
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        fontFamily = com.Nightjar.caliq5.theme.ReemKufiFontFamily
                    ),
                )
                Text(
                    text = "لطلاب العراق 🇮🇶",
                    style = TextStyle(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        fontFamily = FontFamily.Default
                    ),
                )
            }
        }
    }
}
