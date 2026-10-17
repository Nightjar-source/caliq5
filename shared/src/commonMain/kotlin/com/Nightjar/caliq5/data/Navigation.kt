package com.Nightjar.caliq5.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector


/**
 * App navigation destinations.
 * Matches the page IDs and bottom nav structure in caliq5.html.
 */
enum class Screen {
    // Bottom Nav items
    EXEMPTION_SELECT,       // page-general-exemption-select
    EXEMPTION_INPUT,        // page-general-exemption-input (sub-page)
    ANNUAL_AVERAGE,         // page-annual-average
    MONTHLY_AVERAGE,        // page-monthly-average
    TERM2_CALCULATION,      // page-term2-calculation

    // Side Menu pages
    MY_GRADES,              // page-my-grades
    STATISTICS,             // page-statistics
    INSTRUCTIONS,           // page-instructions
    ABOUT,                  // page-about
    NEWS,                   // page-news
    UPDATES,                // page-updates
    WIDGET,                 // page-widget
    APPEARANCE,             // page-appearance
}

/**
 * Bottom navigation items (visible in the main bottom nav bar).
 */
val bottomNavItems = listOf(
    NavItem(Screen.EXEMPTION_SELECT, "الإعفاء", Icons.Default.School),
    NavItem(Screen.ANNUAL_AVERAGE,   "السعي",    Icons.Default.Calculate),
    NavItem(Screen.MONTHLY_AVERAGE,  "الفصلي",   Icons.Default.DateRange),
    NavItem(Screen.TERM2_CALCULATION,"فصل 2",    Icons.Default.Done),
)

/**
 * Side menu items.
 */
val sideMenuItems = listOf(
    NavItem(Screen.MY_GRADES,    "درجاتي (السجل الشامل)", Icons.Default.AutoStories),
    NavItem(Screen.NEWS,         "آخر الأخبار",           Icons.Default.Newspaper),
    NavItem(Screen.UPDATES,      "التحديثات",             Icons.Default.Notifications),
    NavItem(Screen.STATISTICS,   "السجل",                 Icons.Default.QueryStats),
    NavItem(Screen.INSTRUCTIONS, "التعليمات",             @Suppress("DEPRECATION") Icons.Default.HelpOutline),
    NavItem(Screen.ABOUT,        "حول التطبيق",           Icons.Default.Info),
    NavItem(Screen.WIDGET,       "إضافة التطبيق المصغر",  Icons.Default.Widgets),
    NavItem(Screen.APPEARANCE,   "إعدادات المظهر",        Icons.Default.Palette),
)

data class NavItem(
    val screen: Screen,
    val label: String,
    val icon: ImageVector,
)

object AppVersion {
    const val VERSION_NAME = "5.14.0"
    const val VERSION_CODE = 51400
    const val EDITION = "Material 3 Expressive"
    val FULL_DISPLAY = "الإصدار $VERSION_NAME ($EDITION)"
}
