package com.Nightjar.caliq5.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.content.ComponentName
import android.content.Context
import android.os.Build
import com.Nightjar.caliq5.CaliqApplication
import kotlinx.coroutines.launch

actual fun pinAppWidget() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val context = CaliqApplication.appContext
        val appWidgetManager = context.getSystemService(AppWidgetManager::class.java)
        
        val myProvider = ComponentName(context, "com.Nightjar.caliq5.widget.CaliqWidgetReceiver")
        if (appWidgetManager.isRequestPinAppWidgetSupported) {
            appWidgetManager.requestPinAppWidget(myProvider, null, null)
        }
    }
}

actual fun isAppWidgetPinned(): Boolean {
    val context = CaliqApplication.appContext
    val appWidgetManager = AppWidgetManager.getInstance(context)
    val myProvider = ComponentName(context, "com.Nightjar.caliq5.widget.CaliqWidgetReceiver")
    val ids = appWidgetManager.getAppWidgetIds(myProvider)
    return ids != null && ids.isNotEmpty()
}

actual fun updateAppWidget() {
    val context = CaliqApplication.appContext
    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
        try {
            val receiverClass = Class.forName("com.Nightjar.caliq5.widget.CaliqWidgetReceiver")
            try {
                val updateSyncMethod = receiverClass.getMethod("updateWidgetSync", Context::class.java, Int::class.javaPrimitiveType)
                updateSyncMethod.invoke(null, context, AppWidgetManager.INVALID_APPWIDGET_ID)
            } catch (_: Throwable) {
                val updateMethod = receiverClass.getMethod("updateWidget", Context::class.java)
                updateMethod.invoke(null, context)
            }
        } catch (_: Throwable) {
            try {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val myProvider = ComponentName(context, "com.Nightjar.caliq5.widget.CaliqWidgetReceiver")
                val ids = appWidgetManager.getAppWidgetIds(myProvider)
                if (ids != null && ids.isNotEmpty()) {
                    appWidgetManager.notifyAppWidgetViewDataChanged(ids, android.R.id.background)
                    val intent = Intent(context, Class.forName("com.Nightjar.caliq5.widget.CaliqWidgetReceiver")).apply {
                        action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                        putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                    }
                    context.sendBroadcast(intent)
                }
            } catch (_: Throwable) {}
        }
    }
}

actual fun syncWidgetSettings(
    theme: String,
    primaryColor: String,
    useMaterialYou: Boolean,
    pureBlackMode: Boolean,
    pureWhiteMode: Boolean
) {
    try {
        val context = CaliqApplication.appContext
        val widgetPrefs = context.getSharedPreferences("caliq_widget_prefs", Context.MODE_PRIVATE)
        widgetPrefs.edit()
            .putString("theme", theme)
            .putString("primary_color", primaryColor)
            .putBoolean("use_material_you", useMaterialYou)
            .putBoolean("pure_black_mode", pureBlackMode)
            .putBoolean("pure_white_mode", pureWhiteMode)
            .commit()
    } catch (_: Throwable) {}
}
