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
        purgeAppCache(applicationContext)
        stopSelf()
    }

    companion object {
        fun purgeAppCache(context: android.content.Context) {
            try {
                cleanDirectoryContents(context.cacheDir)
                cleanDirectoryContents(context.externalCacheDir)
                val userCache = java.io.File(context.filesDir, "cache")
                if (userCache.exists()) cleanDirectoryContents(userCache)
                val userTemp = java.io.File(context.filesDir, "temp")
                if (userTemp.exists()) cleanDirectoryContents(userTemp)

                MemoryManager.deepPurgeMemory(context)
            } catch (_: Throwable) {}
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
}
