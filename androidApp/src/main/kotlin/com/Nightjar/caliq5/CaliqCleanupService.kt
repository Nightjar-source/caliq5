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
        return START_NOT_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        try {
            // Immediate synchronous cache deletion across Android 7 to 17, Samsung One UI & Xiaomi HyperOS
            cleanDirectoryContents(applicationContext.cacheDir)
            cleanDirectoryContents(applicationContext.externalCacheDir)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                cleanDirectoryContents(applicationContext.codeCacheDir)
            }
            val userCache = java.io.File(applicationContext.filesDir, "cache")
            if (userCache.exists()) cleanDirectoryContents(userCache)
            val userTemp = java.io.File(applicationContext.filesDir, "temp")
            if (userTemp.exists()) cleanDirectoryContents(userTemp)

            MemoryManager.deepPurgeMemory(applicationContext)
        } catch (_: Throwable) {}
        stopSelf()
    }

    private fun cleanDirectoryContents(dir: java.io.File?) {
        try {
            dir?.listFiles()?.forEach { file ->
                try {
                    if (file.isDirectory) file.deleteRecursively() else file.delete()
                } catch (_: Throwable) {}
            }
        } catch (_: Throwable) {}
    }
}
