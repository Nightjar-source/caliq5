package com.Nightjar.caliq5.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberIsHighPerformanceDevice(): Boolean {
    // في بيئة الويب (Wasm)، المتصفح (مثل كروم) يستخدم CanvasKit لتشغيل الرسوميات.
    // بشكل عام، الأجهزة التي تتصفح الويب قادرة على تشغيل التأثيرات بشكل ممتاز.
    // يمكن مستقبلاً قراءة (navigator.deviceMemory) أو (navigator.hardwareConcurrency) عبر JS Interop 
    // لتحديد قوة الجهاز، لكن الافتراضي هنا هو الأداء العالي.
    return remember { true }
}
