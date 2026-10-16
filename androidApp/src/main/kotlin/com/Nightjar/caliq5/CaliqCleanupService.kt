package com.Nightjar.caliq5

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.Nightjar.caliq5.utils.MemoryManager

/**
 * Background cleanup service triggered when app is swiped away from Recents (onTaskRemoved).
 * Ensures instant cache & temporary file purge when user leaves / terminates the app.
 */
class CaliqCleanupService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        try {
            // Immediate synchronous cache deletion across Android 7 to 17, Samsung One UI & Xiaomi HyperOS
            applicationContext.cacheDir?.deleteRecursively()
            applicationContext.externalCacheDir?.deleteRecursively()
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                applicationContext.codeCacheDir?.deleteRecursively()
            }
            val userCache = java.io.File(applicationContext.filesDir, "cache")
            if (userCache.exists()) userCache.deleteRecursively()
            val userTemp = java.io.File(applicationContext.filesDir, "temp")
            if (userTemp.exists()) userTemp.deleteRecursively()

            MemoryManager.deepPurgeMemory(applicationContext)
        } catch (_: Throwable) {}
        stopSelf()
    }
}
