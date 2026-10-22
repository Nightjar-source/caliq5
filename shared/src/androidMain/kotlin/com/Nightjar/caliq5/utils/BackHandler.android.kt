package com.Nightjar.caliq5.utils

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.coroutines.CancellationException

@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
    val currentOnBack = rememberUpdatedState(onBack)
    PredictiveBackHandler(enabled = enabled) { progress ->
        try {
            progress.collect { backEvent ->
                // Collects predictive back progress smoothly (0.0 to 1.0)
            }
            currentOnBack.value()
        } catch (e: CancellationException) {
            // Swipe cancelled by the user returning finger to edge
        } catch (e: Throwable) {
            // Safe fallback
        }
    }
}
