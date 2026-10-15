package com.Nightjar.caliq5.utils

import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import platform.Foundation.NSURL

actual fun isNotificationPermissionGranted(): Boolean {
    // For iOS, checking permissions synchronously is tricky without completion handlers.
    // For now we'll return true to skip the custom dialog on iOS, as requested mostly for Android.
    return true
}

actual fun openAppSettings() {
    val url = NSURL(string = UIApplicationOpenSettingsURLString)
    UIApplication.sharedApplication.openURL(url)
}
