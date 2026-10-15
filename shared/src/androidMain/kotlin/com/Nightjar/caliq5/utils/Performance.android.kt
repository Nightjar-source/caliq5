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
        
        // 2. Exact physical total RAM check (Graceful degradation for <= 3.2GB RAM devices)
        // Works universally across all Android versions (including Android 15, 16, 17+)
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        val totalRamGb = memoryInfo.totalMem / (1024.0 * 1024.0 * 1024.0)
        if (totalRamGb <= 3.2) return@remember false
        
        // 3. CPU Core count check (Quad-core or lower is considered low-end)
        val cores = Runtime.getRuntime().availableProcessors()
        if (cores <= 4 && totalRamGb < 4.0) return@remember false

        // 4. OS Version check (Android 9 and older have legacy Skia drivers)
        Build.VERSION.SDK_INT >= 29
    }
}
