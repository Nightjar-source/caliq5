package com.Nightjar.caliq5.utils

import androidx.compose.runtime.Composable

@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
    // iOS swipe-to-back is handled by the system UINavigationController.
    // Compose multiplatform doesn't fully support custom back handlers on iOS yet.
}
