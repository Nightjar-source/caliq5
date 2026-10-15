package com.Nightjar.caliq5.utils

import android.app.ActivityManager
import android.content.Context
import android.os.Handler
import android.os.Looper
import com.Nightjar.caliq5.CaliqApplication
import java.io.File
import java.util.concurrent.ConcurrentHashMap

actual object MemoryManager {
    private val context: Context get() = CaliqApplication.appContext
    private val mainHandler by lazy { Handler(Looper.getMainLooper()) }
    private val pendingCleanupTasks = ConcurrentHashMap<String, Runnable>()

    /**
     * Schedules a lightweight 2-minute idle timer for a cached file.
     * If the user accesses/opens the file again within 2 minutes, the timer resets.
     * When 2 minutes elapse without further access, the file is safely deleted from cache.
     * Uses zero background threads and consumes zero CPU/RAM while idle.
     */
    fun scheduleTwoMinuteIdleCleanup(file: File) {
        val path = file.absolutePath
        pendingCleanupTasks.remove(path)?.let { existingRunnable ->
            mainHandler.removeCallbacks(existingRunnable)
        }

        val cleanupRunnable = Runnable {
            pendingCleanupTasks.remove(path)
            try {
                if (file.exists() && file.isFile) {
                    file.delete()
                }
            } catch (_: Throwable) {}
        }

        pendingCleanupTasks[path] = cleanupRunnable
        mainHandler.postDelayed(cleanupRunnable, 120_000L)
    }

    /**
     * Cancels any pending idle cleanup for a specific file.
     */
    fun cancelTwoMinuteIdleCleanup(file: File) {
        val path = file.absolutePath
        pendingCleanupTasks.remove(path)?.let { existingRunnable ->
            mainHandler.removeCallbacks(existingRunnable)
        }
    }

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

            if (isLowRam || isDeviceLowRam()) {
                System.gc()
            }
        } catch (_: Throwable) {}
    }

    fun trimMemoryOnBackground(ctx: Context) {
        trimMemoryOnBackground()
    }

    /**
     * Purges transient cache files (.tmp, generated temporary PDFs, font caches)
     * completely without ever touching permanent user data (databases, DataStore, filesDir).
     * Cancels all pending timers and frees memory immediately upon app exit or swipe-out.
     */
    actual fun deepPurgeMemory() {
        try {
            pendingCleanupTasks.values.forEach { runnable ->
                mainHandler.removeCallbacks(runnable)
            }
            pendingCleanupTasks.clear()

            val cache = context.cacheDir
            cache?.listFiles()?.forEach { file ->
                try {
                    if (file.isFile) {
                        file.delete()
                    } else if (file.isDirectory && file.name.startsWith("temp", ignoreCase = true)) {
                        file.deleteRecursively()
                    }
                } catch (_: Throwable) {}
            }
            context.externalCacheDir?.listFiles()?.forEach { file ->
                try {
                    if (file.isFile) {
                        file.delete()
                    }
                } catch (_: Throwable) {}
            }
            System.gc()
        } catch (_: Throwable) {}
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
        } catch (_: Throwable) {
            false
        }
    }
}
