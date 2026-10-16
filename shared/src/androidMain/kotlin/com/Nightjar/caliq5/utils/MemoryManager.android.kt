package com.Nightjar.caliq5.utils

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import com.Nightjar.caliq5.CaliqApplication

actual object MemoryManager {
    private val context: Context get() = CaliqApplication.appContext

    /**
     * February 2027 Android Memory Guidelines:
     * Focuses on TRIM_MEMORY_UI_HIDDEN and background memory optimization.
     * Prunes transient caches and triggers garbage collection on low-RAM devices
     * to prevent LMKD termination and reduce system pressure while in recent apps.
     */
    actual fun trimMemoryOnBackground() {
        try {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            val isLowRam = activityManager?.isLowRamDevice == true

            // On older and low-RAM devices (<= 3.2GB RAM), invoke System.gc() when backgrounded
            // to reclaim memory held by Compose layouts and transient graphics buffers immediately.
            if (isLowRam || isDeviceLowRam()) {
                System.gc()
            }
        } catch (e: Throwable) {
            // Safe guard against platform differences
        }
    }

    fun trimMemoryOnBackground(ctx: Context) {
        trimMemoryOnBackground()
    }

    /**
     * Purges transient cache files (.tmp, generated temporary PDFs, image caches)
     * completely without ever touching permanent user data (databases, DataStore, filesDir).
     * Prevents cache bloat when user exits or navigates away.
     */
    actual fun deepPurgeMemory() {
        try {
            val cache = context.cacheDir
            cache?.listFiles()?.forEach { file ->
                try {
                    if (file.isFile) {
                        file.delete()
                    } else if (file.isDirectory) {
                        file.deleteRecursively()
                    }
                } catch (_: Throwable) {}
            }
            context.externalCacheDir?.listFiles()?.forEach { file ->
                try {
                    if (file.isFile) {
                        file.delete()
                    } else if (file.isDirectory) {
                        file.deleteRecursively()
                    }
                } catch (_: Throwable) {}
            }
        } catch (_: Throwable) {
            // Ignore errors during cache cleanup
        }
    }

    fun deepPurgeMemory(ctx: Context) {
        deepPurgeMemory()
    }

    private fun isDeviceLowRam(): Boolean {
        return try {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager ?: return false
            val memoryInfo = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memoryInfo)
            val totalRamGb = memoryInfo.totalMem / (1024.0 * 1024.0 * 1024.0)
            totalRamGb <= 3.2
        } catch (e: Throwable) {
            false
        }
    }
}
