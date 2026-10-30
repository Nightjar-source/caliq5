package com.Nightjar.caliq5.utils

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState

@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
    val currentOnBack = rememberUpdatedState(onBack)
    androidx.activity.compose.BackHandler(enabled = enabled) {
        currentOnBack.value()
    }
}

