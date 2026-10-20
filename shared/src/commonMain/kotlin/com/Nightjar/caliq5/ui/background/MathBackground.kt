package com.Nightjar.caliq5.ui.background

import androidx.compose.material.icons.filled.*
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// ===== Data class for each floating symbol =====
data class FloatingSymbol(
    val text: String,
    val startX: Float,      // 0f..1f (fraction of screen width)
    val durationMs: Int,    // animation duration (15-35 seconds)
    val delayFraction: Float, // -1f..0f (negative delay to start mid-animation)
    val fontSize: Float,    // sp
)

// Generate a fixed set of symbols (stable composition)
private val mathSymbols = listOf("*", "π", "√", "%", "×", "∆", "=", "+", "-")

@Composable
fun rememberFloatingSymbols(count: Int = 25): List<FloatingSymbol> {
    return remember {
        (0 until count).map {
            FloatingSymbol(
                text = mathSymbols[it % mathSymbols.size],
                startX = Random.nextFloat(),
                durationMs = (15_000 + Random.nextFloat() * 20_000).toInt(),
                delayFraction = -Random.nextFloat(),
                fontSize = 14f + Random.nextFloat() * 20f,
            )
        }
    }
}

/**
 * Animated mathematical background composable.
 * Recreates the CSS @keyframes floatSymbol effect using rememberInfiniteTransition.
 * Symbols float from bottom to top with rotation, low opacity.
 */
@Composable
fun MathBackground(
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val primaryContainerColor = MaterialTheme.colorScheme.primaryContainer
    val backgroundColor = MaterialTheme.colorScheme.background

    val symbols = rememberFloatingSymbols(count = 16)
    val textMeasurer = rememberTextMeasurer()
    val isHighPerf = com.Nightjar.caliq5.utils.rememberIsHighPerformanceDevice()
    var masterTime by remember { mutableStateOf(0f) }
    
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()
    val isAppVisible = lifecycleState.isAtLeast(androidx.lifecycle.Lifecycle.State.STARTED)

    androidx.compose.runtime.LaunchedEffect(isHighPerf, isAppVisible) {
        if (!isAppVisible) return@LaunchedEffect
        kotlinx.coroutines.delay(350) // Smooth initial startup frame
        var previousFrame = androidx.compose.runtime.withFrameMillis { it }
        while (true) {
            val currentFrame = androidx.compose.runtime.withFrameMillis { it }
            val delta = (currentFrame - previousFrame).toFloat()
            previousFrame = currentFrame
            
            // On weak phones, move 4x slower to create a calm, barely-moving effect
            val speedMultiplier = if (isHighPerf) 1f else 0.25f
            masterTime += delta * speedMultiplier
            
            // Limit frame rate to save battery globally:
            // Strong phones: ~30 fps (delay 33ms)
            // Weak phones: ~10 fps (delay 100ms)
            if (isHighPerf) {
                kotlinx.coroutines.delay(33)
            } else {
                kotlinx.coroutines.delay(100)
            }
        }
    }

    // Pre-measure texts once
    val symbolColor = remember(primaryColor) { primaryColor.copy(alpha = 0.35f) }
    val measuredSymbols = remember(symbols, symbolColor) {
        symbols.map { symbol ->
            symbol to textMeasurer.measure(
                text = symbol.text,
                style = TextStyle(
                    color = symbolColor,
                    fontSize = symbol.fontSize.sp,
                    fontWeight = FontWeight.Bold,
                )
            )
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Gradient and Floating symbols drawn in a single draw pass with 0 composition overhead
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(color = backgroundColor)
            // Removed top edge glow to prevent flashing on result screen and landscape/tablet modes

            val currentTime = masterTime
            measuredSymbols.forEach { (symbol, measuredText) ->
                val progress = ((currentTime % symbol.durationMs) / symbol.durationMs.toFloat() - symbol.delayFraction) % 1f
                val clampedProgress = if (progress < 0f) progress + 1f else progress
                
                drawFloatingSymbol(
                    measuredText = measuredText,
                    symbol = symbol,
                    progress = clampedProgress,
                    canvasWidth = size.width,
                    canvasHeight = size.height,
                )
            }
        }
    }
}

/**
 * Draws a single symbol on the canvas at the given animation progress.
 * Mimics CSS: translateY(110vh) rotate(0deg) → translateY(-10vh) rotate(360deg)
 */
private fun DrawScope.drawFloatingSymbol(
    measuredText: androidx.compose.ui.text.TextLayoutResult,
    symbol: FloatingSymbol,
    progress: Float,
    canvasWidth: Float,
    canvasHeight: Float,
) {
    // Y position: from 110% height to -10% height
    val startY = canvasHeight * 1.10f
    val endY = -canvasHeight * 0.10f
    val currentY = startY + (endY - startY) * progress

    // Rotation: 0 to 360 degrees
    val rotation = 360f * progress

    // X position fixed per symbol
    val currentX = canvasWidth * symbol.startX

    translate(left = currentX, top = currentY) {
        rotate(degrees = rotation, pivot = Offset(
            measuredText.size.width / 2f,
            measuredText.size.height / 2f
        )) {
            drawText(
                textLayoutResult = measuredText,
            )
        }
    }
}
