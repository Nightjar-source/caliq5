package com.Nightjar.caliq5.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberIsHighPerformanceDevice(): Boolean {
    // أجهزة الكمبيوتر (ويندوز، ماك، لينكس) تمتلك عتاداً قوياً جداً مقارنة بالهواتف.
    // الدالة Runtime.getRuntime().maxMemory() تعود بذاكرة ضخمة، لذا نفترض الأداء العالي دائماً.
    return remember { true }
}
