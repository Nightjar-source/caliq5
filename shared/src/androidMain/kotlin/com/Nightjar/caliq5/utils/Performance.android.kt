package com.Nightjar.caliq5.utils

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberIsHighPerformanceDevice(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        if (activityManager == null) return@remember true
        
        // 1. Android Go / Manufacturer Low RAM flag
        if (activityManager.isLowRamDevice) return@remember false
        
        // 2. Exact physical total RAM check (Graceful degradation for <= 3.5GB RAM devices)
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        val totalRamGb = memoryInfo.totalMem / (1024.0 * 1024.0 * 1024.0)
        if (totalRamGb <= 3.5) return@remember false
        
        // 3. CPU Core count check (Quad-core or lower is considered low-end)
        val cores = Runtime.getRuntime().availableProcessors()
        if (cores <= 4 && totalRamGb < 6.0) return@remember false

        // 4. Hardware-accelerated RenderEffect requires Android 12+ (API 31+)
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    }
}
