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
            MemoryManager.deepPurgeMemory()
        } catch (_: Throwable) {}
        stopSelf()
    }
}
