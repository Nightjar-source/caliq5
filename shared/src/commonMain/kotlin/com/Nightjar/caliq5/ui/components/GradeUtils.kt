package com.Nightjar.caliq5.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Helper to convert Eastern Arabic digits to English digits
 */
fun String.toEnDigits(): String {
    return this.replace('٠', '0').replace('١', '1').replace('٢', '2')
        .replace('٣', '3').replace('٤', '4').replace('٥', '5')
        .replace('٦', '6').replace('٧', '7').replace('٨', '8')
        .replace('٩', '9')
}

/**
 * Parses a string to Double handling Eastern Arabic/Persian digits and various decimal commas/marks.
 */
fun String.parseGradeToDouble(): Double? {
    if (this.isBlank()) return null
    val cleaned = this.toEnDigits()
        .replace(',', '.')
        .replace('،', '.')
        .replace('٫', '.')
        .replace('٬', '.')
        .trim()
    return cleaned.toDoubleOrNull()
}

/**
 * Formats a grade cleanly with up to maxDecimals places, stripping unnecessary trailing zeros (e.g. 85, 85.5, 88.949416).
 * Guarantees exact number, fraction, and dot consistency across all screens, equations, cards, and PDF export.
 */
fun formatDecimalGrade(num: Double, maxDecimals: Int = 6): String {
    if (num.isNaN() || num.isInfinite()) return "0"
    val roundedInt = kotlin.math.round(num)
    if (kotlin.math.abs(num - roundedInt) < 1e-9) {
        return roundedInt.toLong().toString()
    }

    val rawStr = num.toString()
    if (!rawStr.contains('E') && !rawStr.contains('e')) {
        val parts = rawStr.split('.')
        if (parts.size == 2) {
            val whole = parts[0]
            val frac = parts[1]
            if (frac.length <= maxDecimals) {
                val trimmed = frac.trimEnd('0')
                return if (trimmed.isEmpty()) whole else "$whole.$trimmed"
            }
        }
    }

    var factor = 1.0
    repeat(maxDecimals) { factor *= 10.0 }
    val rounded = kotlin.math.round(num * factor) / factor
    val roundedStr = rounded.toString()
    val parts = roundedStr.split('.')
    if (parts.size == 2) {
        val whole = parts[0]
        val trimmed = parts[1].trimEnd('0')
        return if (trimmed.isEmpty()) whole else "$whole.$trimmed"
    }
    return roundedStr
}

/**
 * Validates and sanitizes a grade input string (range 0.0 to 100.0).
 * Handles Eastern Arabic digits (٠١٢٣٤٥٦٧٨٩), Arabic commas (،), decimals (٫, ٬, .),
 * ensures user can enter fractions (e.g. 88.949416), typing a dot first becomes "0.",
 * and ensures numbers never exceed 100.0.
 */
fun cleanGradeInput(input: String): String? {
    if (input.isEmpty()) return ""

    // 1. Convert Eastern Arabic & Persian digits to ASCII
    var cleaned = input.toEnDigits()

    // 2. Normalize decimal separators (English comma, Arabic comma, Arabic decimal separator) to '.'
    cleaned = cleaned
        .replace(',', '.')
        .replace('،', '.')
        .replace('٫', '.')
        .replace('٬', '.')
        .trim()

    // 3. If user typed just '.', immediately treat as "0."
    if (cleaned == ".") return "0."

    // 4. If input starts with '.', prepend '0' (e.g. ".5" -> "0.5")
    if (cleaned.startsWith(".")) {
        cleaned = "0$cleaned"
    }

    // 5. Allow only valid decimal numbers (digits, optional dot, optional trailing digits)
    if (!cleaned.matches(Regex("""^\d+(\.\d*)?$"""))) return null

    // 6. Range check: must not exceed 100.0
    if (cleaned.endsWith(".")) {
        val prefix = cleaned.dropLast(1)
        val num = prefix.toDoubleOrNull() ?: return null
        if (num < 0.0 || num > 100.0) return null
        return cleaned
    }

    val num = cleaned.toDoubleOrNull() ?: return null
    if (num < 0.0 || num > 100.0) return null

    return cleaned
}

@Composable
fun Modifier.adaptiveWidth(maxWidth: Dp = 380.dp): Modifier = this.composed {
    this.widthIn(max = maxWidth).fillMaxWidth()
}

fun Modifier.actionButtonAdaptive(maxWidth: Dp = 380.dp): Modifier = this
    .widthIn(max = maxWidth)
    .fillMaxWidth()
