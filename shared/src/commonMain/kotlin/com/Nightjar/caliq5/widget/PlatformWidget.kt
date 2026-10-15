package com.Nightjar.caliq5.widget

/**
 * Requests the system to pin the App Widget to the home screen.
 */
expect fun pinAppWidget()

/**
 * Checks if the widget has already been pinned/added to the home screen.
 */
expect fun isAppWidgetPinned(): Boolean

/**
 * Updates the app widget.
 */
expect fun updateAppWidget()
