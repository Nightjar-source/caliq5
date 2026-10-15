package com.Nightjar.caliq5

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.Nightjar.caliq5.App

import androidx.activity.SystemBarStyle

class MainActivity : ComponentActivity() {
    private var deepLinkUri = androidx.compose.runtime.mutableStateOf<String?>(null)
    @Volatile
    private var isComposeDrawn = false

    override fun onCreate(savedInstanceState: Bundle?) {
        window.setBackgroundDrawableResource(R.drawable.splash_background)
        val splashScreen = installSplashScreen()

        // تثبيت العرض (setKeepOnScreenCondition): يمنع إغلاق شاشة النظام قبل اكتمال رسم شاشة Compose تحتها تماماً
        splashScreen.setKeepOnScreenCondition { !isComposeDrawn }
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.auto(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
        )
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
            window.isStatusBarContrastEnforced = false
        }
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        super.onCreate(savedInstanceState)
        
        if (savedInstanceState != null) {
            val savedUri = savedInstanceState.getString("savedDeepLinkUri")
            if (savedUri != null) deepLinkUri.value = savedUri
        } else {
            deepLinkUri.value = intent.data?.toString()
        }

        try {
            startService(android.content.Intent(this, CaliqCleanupService::class.java))
        } catch (_: Throwable) {}

        setContent {
            androidx.compose.runtime.LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(200) // Small delay to prevent native splash exit flicker on Xiaomi/HyperOS
                isComposeDrawn = true
            }
            App(
                deepLinkUri = deepLinkUri.value,
                isRestoredFromProcessDeath = savedInstanceState != null,
                onSplashFinishedAction = {
                    // Request notification permission for Android 13+ (API 33+) after splash finishes
                    if (android.os.Build.VERSION.SDK_INT >= 33) {
                        val permission = android.Manifest.permission.POST_NOTIFICATIONS
                        if (androidx.core.content.ContextCompat.checkSelfPermission(this, permission) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                            androidx.core.app.ActivityCompat.requestPermissions(this, arrayOf(permission), 101)
                        }
                    }
                }
            )
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        deepLinkUri.value = intent.data?.toString()
    }

    override fun onResume() {
        super.onResume()
        applyTransparentBars()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            applyTransparentBars()
        }
    }

    private fun applyTransparentBars() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
            window.isStatusBarContrastEnforced = false
        }
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("savedDeepLinkUri", deepLinkUri.value)
    }

    /**
     * AI Agent & Digital Assistant Semantic Knowledge Provider (Android Assist API / Gemini Nano).
     * Provides structured Schema.org JSON-LD and canonical deep links to digital assistants
     * (Google Assistant, Gemini, Samsung Bixby) when invoked by voice or agentic actions,
     * eliminating reliance on OCR and enabling high-precision semantic understanding.
     */
    override fun onProvideAssistContent(outContent: android.app.assist.AssistContent?) {
        super.onProvideAssistContent(outContent)
        if (outContent != null) {
            val currentUri = deepLinkUri.value ?: "caliq://exemption"
            outContent.webUri = android.net.Uri.parse(currentUri)

            val structuredJson = org.json.JSONObject().apply {
                put("@context", "https://schema.org")
                put("@type", "SoftwareApplication")
                put("name", "Caliq")
                put("alternateName", "حاسبة كالك")
                put("applicationCategory", "EducationalApplication")
                put("operatingSystem", "Android")
                put("version", com.Nightjar.caliq5.data.AppVersion.VERSION_NAME)
                put("description", "حاسبة درجات ومعدلات وإعفاء نظام التعليم العراقي")
                put("url", currentUri)
            }
            outContent.structuredData = structuredJson.toString()
        }
    }

    override fun onProvideAssistData(data: Bundle?) {
        super.onProvideAssistData(data)
        data?.putString("appName", "Caliq")
        data?.putString("version", com.Nightjar.caliq5.data.AppVersion.VERSION_NAME)
        data?.putString("activeRoute", deepLinkUri.value ?: "caliq://exemption")
        data?.putString("targetCountry", "Iraq")
        data?.putString("systemStandard", "Iraqi Educational System")
    }

    // Modern predictive back is handled directly by OnBackPressedDispatcher and Compose BackHandler

    /**
     * Google February 2027 Memory Optimization Guidelines:
     * Focus exclusively on TRIM_MEMORY_UI_HIDDEN and TRIM_MEMORY_BACKGROUND
     * to prevent out-of-memory errors and optimize battery life across all device tiers.
     */
    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        when (level) {
            android.content.ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN -> {
                // UI is hidden - release transient drawing caches and invoke proactive GC on low RAM devices
                com.Nightjar.caliq5.utils.MemoryManager.trimMemoryOnBackground(this)
            }
            android.content.ComponentCallbacks2.TRIM_MEMORY_BACKGROUND -> {
                // App is in background and system is under memory pressure - prune stale cache & temporary files
                com.Nightjar.caliq5.utils.MemoryManager.deepPurgeMemory(this)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Proactively purge transient cache on exit so cache never accumulates
        com.Nightjar.caliq5.utils.MemoryManager.deepPurgeMemory(this)
    }
}
