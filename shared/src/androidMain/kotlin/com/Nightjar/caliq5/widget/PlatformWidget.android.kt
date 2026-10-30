import kotlinx.coroutines.launch
package com.Nightjar.caliq5.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.content.ComponentName
import android.os.Build
import com.Nightjar.caliq5.CaliqApplication

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
            val widgetClass = Class.forName("com.Nightjar.caliq5.widget.CaliqWidget")
            val instance = widgetClass.getDeclaredConstructor().newInstance()
            val updateAllMethod = widgetClass.getMethod("updateAll", android.content.Context::class.java)
            updateAllMethod.invoke(instance, context)
        } catch (_: Throwable) {
            try {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val myProvider = ComponentName(context, "com.Nightjar.caliq5.widget.CaliqWidgetReceiver")
                val ids = appWidgetManager.getAppWidgetIds(myProvider)
                if (ids != null && ids.isNotEmpty()) {
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
