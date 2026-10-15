package com.Nightjar.caliq5.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberIsHighPerformanceDevice(): Boolean {
    // iPhones and iPads supported by Compose Multiplatform (iOS 14+) 
    // have highly optimized Metal GPUs. We can safely default to true.
    return remember { true }
}
