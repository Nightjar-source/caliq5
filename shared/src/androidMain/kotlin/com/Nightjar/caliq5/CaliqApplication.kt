package com.Nightjar.caliq5

import android.app.Application
import android.content.ComponentCallbacks2
import android.content.Context

open class CaliqApplication : Application() {
    companion object {
        lateinit var appContext: Context
            private set
    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level >= ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) {
            System.gc()
        }
    }
}
