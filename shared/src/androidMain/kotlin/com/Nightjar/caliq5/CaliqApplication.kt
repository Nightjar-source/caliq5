package com.Nightjar.caliq5

import android.app.Application
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
}
