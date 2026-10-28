package com.Nightjar.caliq5.theme

import androidx.compose.material.icons.filled.*
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.Nightjar.caliq5.data.AppSettings
import com.materialkolor.rememberDynamicColorScheme

// ===== Light Color Scheme (matches CSS exactly) =====
val CaliqLightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,
    error = Error,
    onError = OnError,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,
    outline = Outline,
    outlineVariant = OutlineVariant,
)

// ===== Dark Color Scheme =====
val CaliqDarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryContainerDark,
    secondary = SecondaryDark,
    onSecondary = OnSecondaryDark,
    secondaryContainer = SecondaryContainerDark,
    onSecondaryContainer = OnSecondaryContainerDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark,
)

expect @Composable
fun getDynamicColorScheme(darkTheme: Boolean): ColorScheme?

expect @Composable
fun SystemAppearance(isDark: Boolean)

// ===== Custom colors accessible globally =====
val LocalIsDark = androidx.compose.runtime.compositionLocalOf { false }

@Composable
fun isAppInDarkTheme(): Boolean = LocalIsDark.current

/**
 * Calculates the highest-contrast text color (Color.White or deep Slate Black)
 * strictly following WCAG 2.1 relative luminance and contrast ratio standards (ISO 9241-395 / W3C).
 * Guarantees that dark text is never rendered over dark backgrounds, and white text is never rendered over light backgrounds.
 */
fun Color.contrastingTextColor(
    darkText: Color = Color(0xFF0F172A),
    lightText: Color = Color.White
): Color {
    // Standard WCAG 2.1 relative luminance calculation with sRGB gamma correction
    fun channelLuminance(c: Float): Float {
        val v = c.toDouble()
        return if (c <= 0.04045f) (c / 12.92f) else kotlin.math.exp(2.4 * kotlin.math.ln((v + 0.055) / 1.055)).toFloat()
    }
    val l = 0.2126f * channelLuminance(red) + 0.7152f * channelLuminance(green) + 0.0722f * channelLuminance(blue)
    
    // In dark mode or saturated backgrounds (warm orange, green, red, amber), dark text creates poor contrast and appears muddy.
    // In Material 3 Expressive, vibrant cards and saturated colors require crisp white text whenever luminance is not bright white.
    val maxC = maxOf(red, green, blue)
    val minC = minOf(red, green, blue)
    val saturation = if (maxC == 0f) 0f else (maxC - minC) / maxC

    // If luminance is below 0.58f, or in saturated colors (saturation > 0.45f and luminance < 0.65f), white text guarantees high legibility.
    val contrastWhite = 1.05f / (l + 0.05f)
    val contrastBlack = (l + 0.05f) / 0.05f

    return if (l < 0.58f || (saturation > 0.45f && l < 0.65f) || contrastWhite >= contrastBlack) lightText else darkText
}

/**
 * Distinctive protective text shadow according to Dark and Light mode standards (contrastingTextColor)
 * Guarantees maximum legibility over gradient/variable/dark/light backgrounds.
 */
fun Color.contrastingShadow(alpha: Float = 0.50f): androidx.compose.ui.graphics.Shadow {
    val isLight = (0.2126f * red + 0.7152f * green + 0.0722f * blue) > 0.5f
    return if (isLight) {
        // نص أبيض ناصع فوق خلفية دافئة/داكنة/متدرجة: ظل أسود عميق ناعم يبرز الأحرف بدقة متناهية
        androidx.compose.ui.graphics.Shadow(
            color = Color.Black.copy(alpha = (alpha * 1.15f).coerceIn(0.40f, 0.70f)),
            offset = androidx.compose.ui.geometry.Offset(0f, 2.5f),
            blurRadius = 6.0f
        )
    } else {
        // نص داكن فوق خلفية فاتحة/متدرجة: هالة ضوئية بيضاء واقية تحمي حواف النص من الاندماج
        androidx.compose.ui.graphics.Shadow(
            color = Color.White.copy(alpha = 0.65f),
            offset = androidx.compose.ui.geometry.Offset(0f, 1.5f),
            blurRadius = 4.5f
        )
    }
}

val LocalSuccessColor = androidx.compose.runtime.staticCompositionLocalOf { Color(0xFF28A745) }
val LocalSuccessContainerColor = androidx.compose.runtime.staticCompositionLocalOf { Color(0xFFE8F5E9) }
val LocalErrorColor = androidx.compose.runtime.staticCompositionLocalOf { Color(0xFFDC2626) }
val LocalErrorContainerColor = androidx.compose.runtime.staticCompositionLocalOf { Color(0xFFFFEBEE) }

val successColor: Color
    @Composable get() = LocalSuccessColor.current

val successContainerColor: Color
    @Composable get() = LocalSuccessContainerColor.current

val errorColor: Color
    @Composable get() = LocalErrorColor.current

val errorContainerColor: Color
    @Composable get() = LocalErrorContainerColor.current

fun parseHexColor(hex: String): Color {
    return try {
        val cleanHex = hex.removePrefix("#")
        val argb = if (cleanHex.length == 6) {
            "FF$cleanHex".toLong(16)
        } else {
            cleanHex.toLong(16)
        }
        Color(argb)
    } catch (e: Exception) {
        Color(0xFF6750A4) // Fallback primary
    }
}

@Composable
fun animateColorScheme(targetColorScheme: ColorScheme): ColorScheme {
    val animationSpec = tween<Color>(durationMillis = 250, easing = FastOutSlowInEasing)
    
    val primary = animateColorAsState(targetColorScheme.primary, animationSpec).value
    val onPrimary = animateColorAsState(targetColorScheme.onPrimary, animationSpec).value
    val primaryContainer = animateColorAsState(targetColorScheme.primaryContainer, animationSpec).value
    val onPrimaryContainer = animateColorAsState(targetColorScheme.onPrimaryContainer, animationSpec).value
    val inversePrimary = animateColorAsState(targetColorScheme.inversePrimary, animationSpec).value
    val secondary = animateColorAsState(targetColorScheme.secondary, animationSpec).value
    val onSecondary = animateColorAsState(targetColorScheme.onSecondary, animationSpec).value
    val secondaryContainer = animateColorAsState(targetColorScheme.secondaryContainer, animationSpec).value
    val onSecondaryContainer = animateColorAsState(targetColorScheme.onSecondaryContainer, animationSpec).value
    val tertiary = animateColorAsState(targetColorScheme.tertiary, animationSpec).value
    val onTertiary = animateColorAsState(targetColorScheme.onTertiary, animationSpec).value
    val tertiaryContainer = animateColorAsState(targetColorScheme.tertiaryContainer, animationSpec).value
    val onTertiaryContainer = animateColorAsState(targetColorScheme.onTertiaryContainer, animationSpec).value
    val background = animateColorAsState(targetColorScheme.background, animationSpec).value
    val onBackground = animateColorAsState(targetColorScheme.onBackground, animationSpec).value
    val surface = animateColorAsState(targetColorScheme.surface, animationSpec).value
    val onSurface = animateColorAsState(targetColorScheme.onSurface, animationSpec).value
    val surfaceVariant = animateColorAsState(targetColorScheme.surfaceVariant, animationSpec).value
    val onSurfaceVariant = animateColorAsState(targetColorScheme.onSurfaceVariant, animationSpec).value
    val surfaceTint = animateColorAsState(targetColorScheme.surfaceTint, animationSpec).value
    val inverseSurface = animateColorAsState(targetColorScheme.inverseSurface, animationSpec).value
    val inverseOnSurface = animateColorAsState(targetColorScheme.inverseOnSurface, animationSpec).value
    val error = animateColorAsState(targetColorScheme.error, animationSpec).value
    val onError = animateColorAsState(targetColorScheme.onError, animationSpec).value
    val errorContainer = animateColorAsState(targetColorScheme.errorContainer, animationSpec).value
    val onErrorContainer = animateColorAsState(targetColorScheme.onErrorContainer, animationSpec).value
    val outline = animateColorAsState(targetColorScheme.outline, animationSpec).value
    val outlineVariant = animateColorAsState(targetColorScheme.outlineVariant, animationSpec).value
    val scrim = animateColorAsState(targetColorScheme.scrim, animationSpec).value

    return targetColorScheme.copy(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primaryContainer,
        onPrimaryContainer = onPrimaryContainer,
        inversePrimary = inversePrimary,
        secondary = secondary,
        onSecondary = onSecondary,
        secondaryContainer = secondaryContainer,
        onSecondaryContainer = onSecondaryContainer,
        tertiary = tertiary,
        onTertiary = onTertiary,
        tertiaryContainer = tertiaryContainer,
        onTertiaryContainer = onTertiaryContainer,
        background = background,
        onBackground = onBackground,
        surface = surface,
        onSurface = onSurface,
        surfaceVariant = surfaceVariant,
        onSurfaceVariant = onSurfaceVariant,
        surfaceTint = surfaceTint,
        inverseSurface = inverseSurface,
        inverseOnSurface = inverseOnSurface,
        error = error,
        onError = onError,
        errorContainer = errorContainer,
        onErrorContainer = onErrorContainer,
        outline = outline,
        outlineVariant = outlineVariant,
        scrim = scrim
    )
}

fun hslToColor(h: Float, s: Float, l: Float, alpha: Float = 1f): Color {
    val c = (1f - kotlin.math.abs(2f * l - 1f)) * s
    val x = c * (1f - kotlin.math.abs((h * 6f) % 2f - 1f))
    val m = l - c / 2f
    val (r1, g1, b1) = when (((h * 6f).toInt()) % 6) {
        0 -> Triple(c, x, 0f)
        1 -> Triple(x, c, 0f)
        2 -> Triple(0f, c, x)
        3 -> Triple(0f, x, c)
        4 -> Triple(x, 0f, c)
        else -> Triple(c, 0f, x)
    }
    return Color(
        red = (r1 + m).coerceIn(0f, 1f),
        green = (g1 + m).coerceIn(0f, 1f),
        blue = (b1 + m).coerceIn(0f, 1f),
        alpha = alpha
    )
}

fun ensureReadableContrast(color: Color, isDark: Boolean): Color {
    val r = color.red
    val g = color.green
    val b = color.blue
    val max = maxOf(r, g, b)
    val min = minOf(r, g, b)
    val l = (max + min) / 2f

    val h: Float
    val s: Float
    if (max == min) {
        h = 0f
        s = 0f
    } else {
        val d = max - min
        s = if (l > 0.5f) d / (2f - max - min) else d / (max + min)
        val tempH = when (max) {
            r -> (g - b) / d + (if (g < b) 6f else 0f)
            g -> (b - r) / d + 2f
            else -> (r - g) / d + 4f
        }
        h = (tempH / 6f).let { if (it < 0f) it + 1f else if (it >= 1f) it - 1f else it }
    }

    return if (isDark) {
        if (l < 0.36f) {
            // Lighten while strictly preserving the royal hue and rich chroma (e.g. Royal Burgundy)
            hslToColor(h, s.coerceAtLeast(0.70f), 0.46f, color.alpha)
        } else color
    } else {
        if (l > 0.65f) {
            hslToColor(h, s, 0.45f, color.alpha)
        } else color
    }
}

@Composable
fun CaliqTheme(
    settings: AppSettings,
    dynamicColorScheme: ColorScheme? = null,
    content: @Composable () -> Unit
) {
    val darkTheme = when (settings.theme) {
        "dark" -> true
        "light" -> false
        else -> isSystemInDarkTheme()
    }

    val effectiveHex = if (settings.primaryColor.equals("#800020", ignoreCase = true) || settings.primaryColor.equals("#9E1B3E", ignoreCase = true)) "#d41145" else settings.primaryColor
    val rawSeedColor = parseHexColor(effectiveHex)
    val seedColor = rawSeedColor

    val resolvedDynamicScheme = if (settings.useMaterialYou) {
        dynamicColorScheme ?: getDynamicColorScheme(darkTheme)
    } else null

    val baseColorScheme = if (settings.useMaterialYou && resolvedDynamicScheme != null) {
        resolvedDynamicScheme.copy(
            error = if (darkTheme) Color(0xFFEF4444) else Color(0xFFDC2626),
            errorContainer = if (darkTheme) Color(0xFF7F1D1D) else Color(0xFFFFEBEE),
            onError = Color.White,
            onErrorContainer = if (darkTheme) Color(0xFFFFDAD6) else Color(0xFF410002)
        )
    } else {
        val generated = rememberDynamicColorScheme(
            seedColor = seedColor,
            isDark = darkTheme,
            isAmoled = false
        )
        generated.copy(
            primary = rawSeedColor,
            primaryContainer = if (darkTheme) rawSeedColor.copy(alpha = 0.28f) else rawSeedColor.copy(alpha = 0.15f),
            onPrimary = Color.White,
            onPrimaryContainer = if (darkTheme) Color.White else rawSeedColor,
            error = if (darkTheme) Color(0xFFEF4444) else Color(0xFFDC2626),
            errorContainer = if (darkTheme) Color(0xFF7F1D1D) else Color(0xFFFFEBEE),
            onError = Color.White,
            onErrorContainer = if (darkTheme) Color(0xFFFFDAD6) else Color(0xFF410002)
        )
    }

    val colorScheme = if (darkTheme) {
        if (settings.pureBlackMode) {
            baseColorScheme.copy(
                background = Color.Black,
                surface = Color(0xFF0D0D0E),
                surfaceVariant = Color(0xFF18181B),
                onBackground = Color(0xFFF8FAFC),
                onSurface = Color(0xFFF1F5F9),
                onSurfaceVariant = Color(0xFFE2E8F0),
                outline = Color(0xFF3F3F46),
                outlineVariant = Color(0xFF27272A)
            )
        } else if (settings.useMaterialYou && resolvedDynamicScheme != null) {
            baseColorScheme
        } else {
            baseColorScheme.copy(
                onBackground = Color(0xFFF8FAFC),
                onSurface = Color(0xFFF1F5F9),
                onSurfaceVariant = Color(0xFFCBD5E1)
            )
        }
    } else {
        if (settings.pureWhiteMode) {
            baseColorScheme.copy(
                background = Color.White,
                surface = Color.White,
                surfaceVariant = Color(0xFFF1F5F9),
                onBackground = Color(0xFF0F172A),
                onSurface = Color(0xFF0F172A),
                onSurfaceVariant = Color(0xFF334155),
                outline = Color(0xFFCBD5E1),
                outlineVariant = Color(0xFFE2E8F0)
            )
        } else if (settings.useMaterialYou && resolvedDynamicScheme != null) {
            baseColorScheme
        } else {
            baseColorScheme.copy(
                onBackground = Color(0xFF0F172A),
                onSurface = Color(0xFF1E293B),
                onSurfaceVariant = Color(0xFF475569)
            )
        }
    }

    val animatedColorScheme = animateColorScheme(colorScheme)

    val globalSelectionColors = androidx.compose.foundation.text.selection.TextSelectionColors(
        handleColor = animatedColorScheme.primary,
        backgroundColor = animatedColorScheme.primary.copy(alpha = if (darkTheme) 0.40f else 0.30f)
    )

    androidx.compose.runtime.CompositionLocalProvider(
        LocalIsDark provides darkTheme,
        LocalSuccessColor provides if (darkTheme) Color(0xFF4ADE80) else Color(0xFF28A745),
        LocalSuccessContainerColor provides if (darkTheme) Color(0xFF14532D) else Color(0xFFE8F5E9),
        LocalErrorColor provides if (darkTheme) Color(0xFFEF4444) else Color(0xFFDC2626),
        LocalErrorContainerColor provides if (darkTheme) Color(0xFF7F1D1D) else Color(0xFFFFEBEE),
        androidx.compose.foundation.text.selection.LocalTextSelectionColors provides globalSelectionColors
    ) {
        MaterialTheme(
            colorScheme = animatedColorScheme,
            typography = CaliqTypography,
            shapes = CaliqShapes,
            content = {
                SystemAppearance(isDark = darkTheme)
                content()
            }
        )
    }
}
