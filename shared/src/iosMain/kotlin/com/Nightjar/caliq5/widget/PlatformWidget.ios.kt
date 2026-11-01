package com.Nightjar.caliq5.widget

actual fun pinAppWidget() {}

actual fun isAppWidgetPinned(): Boolean = false

actual fun updateAppWidget() {}

actual fun syncWidgetSettings(
    theme: String,
    primaryColor: String,
    useMaterialYou: Boolean,
    pureBlackMode: Boolean,
    pureWhiteMode: Boolean
) {}
