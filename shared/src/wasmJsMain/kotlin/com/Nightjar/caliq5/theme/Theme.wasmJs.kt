package com.Nightjar.caliq5.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

actual @Composable
fun getDynamicColorScheme(darkTheme: Boolean): ColorScheme? = null

actual @Composable
fun SystemAppearance(isDark: Boolean) {
    // Browser PWA theme-color handled via manifest.json
}
