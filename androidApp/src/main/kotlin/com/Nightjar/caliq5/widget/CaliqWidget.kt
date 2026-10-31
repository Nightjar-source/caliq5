package com.Nightjar.caliq5.widget

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.launch
import androidx.glance.color.ColorProvider
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.Nightjar.caliq5.R
import com.Nightjar.caliq5.data.AppSettings
import com.Nightjar.caliq5.viewmodel.DataRepository

class CaliqWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CaliqWidget()

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        updateWidget(context)
    }

    companion object {
        @JvmStatic
        fun updateWidget(context: Context) {
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                try {
                    CaliqWidget().updateAll(context)
                } catch (_: Throwable) {}

                try {
                    val appWidgetManager = android.appwidget.AppWidgetManager.getInstance(context)
                    val myProvider = android.content.ComponentName(context, CaliqWidgetReceiver::class.java)
                    val ids = appWidgetManager.getAppWidgetIds(myProvider)
                    if (ids != null && ids.isNotEmpty()) {
                        val intent = Intent(context, CaliqWidgetReceiver::class.java).apply {
                            action = android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE
                            putExtra(android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                        }
                        context.sendBroadcast(intent)
                    }
                } catch (_: Throwable) {}
            }
        }
    }
}

class CaliqWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val settings = DataRepository.cachedSettings ?: try {
            DataRepository.loadSettings()
        } catch (e: Exception) {
            AppSettings()
        }

        provideContent {
            WidgetContent(context, settings)
        }
    }

    private data class WidgetAction(
        val label: String,
        @DrawableRes val iconRes: Int,
        val deepLink: String
    )

    @Composable
    private fun WidgetContent(context: Context, settings: AppSettings) {
        val widgetPrefs = context.getSharedPreferences("caliq_widget_prefs", Context.MODE_PRIVATE)
        val prefTheme = widgetPrefs.getString("theme", null)
        val prefPrimaryColor = widgetPrefs.getString("primary_color", null)
        val prefUseMaterialYou = if (widgetPrefs.contains("use_material_you")) widgetPrefs.getBoolean("use_material_you", true) else settings.useMaterialYou
        val prefPureBlack = if (widgetPrefs.contains("pure_black_mode")) widgetPrefs.getBoolean("pure_black_mode", false) else settings.pureBlackMode
        val prefPureWhite = if (widgetPrefs.contains("pure_white_mode")) widgetPrefs.getBoolean("pure_white_mode", false) else settings.pureWhiteMode

        val effectiveTheme = prefTheme ?: settings.theme
        val effectivePrimaryColor = prefPrimaryColor ?: settings.primaryColor

        val size = LocalSize.current
        val isVertical = size.height > size.width * 1.15f

        val isDark = when (effectiveTheme) {
            "dark" -> true
            "light" -> false
            else -> {
                val nightModeFlags = context.resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
                nightModeFlags == android.content.res.Configuration.UI_MODE_NIGHT_YES
            }
        }

        val isDynamic = prefUseMaterialYou && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S

        // Resolved primary color (either from Material You or app's selected theme)
        val primaryColorObj = if (isDynamic) {
            try {
                val res = if (isDark) android.R.color.system_accent1_200 else android.R.color.system_accent1_600
                Color(androidx.core.content.ContextCompat.getColor(context, res))
            } catch (_: Throwable) {
                Color(0xFF6750A4)
            }
        } else {
            try {
                val themeHex = effectivePrimaryColor
                val effectiveHex = if (themeHex.equals("#800020", ignoreCase = true) || themeHex.equals("#9E1B3E", ignoreCase = true)) "#d41145" else themeHex
                Color(android.graphics.Color.parseColor(effectiveHex))
            } catch (_: Throwable) {
                Color(0xFF6750A4)
            }
        }

        val bgStyle = widgetPrefs.getString("widget_bg_style", "material_you") ?: "material_you"
        val customOpacity = widgetPrefs.getFloat("widget_opacity", 0.88f)

        // Custom Background Resolution based on user choice (Blur Glass, Material You, Pure Black, App Theme)
        val widgetBg = when (bgStyle) {
            "frosted_glass" -> {
                if (isDark) Color(0xFF1E1E24).copy(alpha = customOpacity.coerceIn(0.60f, 0.95f))
                else Color(0xFFFFFFFF).copy(alpha = customOpacity.coerceIn(0.65f, 0.95f))
            }
            "pure_black" -> Color.Black.copy(alpha = 0.98f)
            "app_theme" -> {
                if (isDark) {
                    Color(
                        red = 0.12f * primaryColorObj.red + 0.88f * 0.08f,
                        green = 0.12f * primaryColorObj.green + 0.88f * 0.08f,
                        blue = 0.12f * primaryColorObj.blue + 0.88f * 0.10f,
                        alpha = customOpacity
                    )
                } else {
                    Color(
                        red = 0.08f * primaryColorObj.red + 0.92f * 0.96f,
                        green = 0.08f * primaryColorObj.green + 0.92f * 0.96f,
                        blue = 0.08f * primaryColorObj.blue + 0.92f * 0.98f,
                        alpha = customOpacity
                    )
                }
            }
            else -> { // "material_you" or default
                if (isDynamic) {
                    try {
                        val surfaceRes = if (isDark) android.R.color.system_neutral1_900 else android.R.color.system_neutral1_50
                        val surfaceColor = Color(androidx.core.content.ContextCompat.getColor(context, surfaceRes))
                        if (isDark) {
                            if (prefPureBlack) Color.Black.copy(alpha = 0.95f) else surfaceColor.copy(alpha = customOpacity)
                        } else {
                            if (prefPureWhite) Color.White.copy(alpha = 0.95f) else surfaceColor.copy(alpha = customOpacity)
                        }
                    } catch (_: Throwable) {
                        if (isDark) Color(0xFF1B1A22).copy(alpha = customOpacity) else Color(0xFFFAF8FC).copy(alpha = customOpacity)
                    }
                } else {
                    if (isDark) {
                        if (prefPureBlack) Color.Black.copy(alpha = 0.95f)
                        else Color(
                            red = 0.08f * primaryColorObj.red + 0.92f * 0.10f,
                            green = 0.08f * primaryColorObj.green + 0.92f * 0.10f,
                            blue = 0.08f * primaryColorObj.blue + 0.92f * 0.13f,
                            alpha = customOpacity
                        )
                    } else {
                        if (prefPureWhite) Color.White.copy(alpha = 0.95f)
                        else Color(
                            red = 0.06f * primaryColorObj.red + 0.94f * 0.97f,
                            green = 0.06f * primaryColorObj.green + 0.94f * 0.97f,
                            blue = 0.06f * primaryColorObj.blue + 0.94f * 0.99f,
                            alpha = customOpacity
                        )
                    }
                }
            }
        }

        val buttonContainerColor = if (isDynamic) {
            try {
                val containerRes = if (isDark) android.R.color.system_accent1_700 else android.R.color.system_accent1_100
                Color(androidx.core.content.ContextCompat.getColor(context, containerRes)).copy(alpha = if (isDark) 0.40f else 0.30f)
            } catch (_: Throwable) {
                primaryColorObj.copy(alpha = if (isDark) 0.22f else 0.15f)
            }
        } else {
            primaryColorObj.copy(alpha = if (isDark) 0.22f else 0.15f)
        }

        val iconColor = primaryColorObj
        val textColor = if (isDark) Color(0xFFE6E1E5) else Color(0xFF1C1B1F)

        val allActions = listOf(
            WidgetAction("الاعفاء", R.drawable.ic_widget_school, "caliq://exemption"),
            WidgetAction("السعي", R.drawable.ic_widget_functions, "caliq://annual"),
            WidgetAction("الفصلي", R.drawable.ic_widget_calendar, "caliq://monthly"),
            WidgetAction("فصل 2", R.drawable.ic_widget_task, "caliq://term2"),
            WidgetAction("السجل", R.drawable.ic_widget_grades, "caliq://grades")
        )

        if (isVertical) {
            // Vertical orientation (Column layout)
            val showLabels = size.width >= 70.dp
            val visibleActions = if (size.height < 180.dp) {
                listOf(allActions[0], allActions[1], allActions[4])
            } else {
                allActions
            }
            val iconBoxSize = if (size.width < 70.dp || size.height < 220.dp) 36.dp else 42.dp
            val iconSize = if (size.width < 70.dp || size.height < 220.dp) 20.dp else 24.dp

            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(widgetBg)
                    .cornerRadius(28.dp)
                    .padding(vertical = 8.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                visibleActions.forEach { action ->
                    VerticalWidgetButton(
                        context = context,
                        action = action,
                        containerColor = buttonContainerColor,
                        iconColor = iconColor,
                        textColor = textColor,
                        showLabel = showLabels,
                        iconSize = iconSize,
                        boxSize = iconBoxSize
                    )
                }
            }
        } else {
            // Horizontal orientation (Row layout)
            val isNarrow = size.width < 220.dp
            val showLabels = size.height >= 54.dp && size.width >= 160.dp
            val visibleActions = if (isNarrow) {
                listOf(allActions[0], allActions[1], allActions[4])
            } else {
                allActions
            }
            val iconBoxSize = if (size.height < 56.dp) 36.dp else 42.dp
            val iconSize = if (size.height < 56.dp) 20.dp else 24.dp

            Row(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(widgetBg)
                    .cornerRadius(28.dp)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                visibleActions.forEach { action ->
                    HorizontalWidgetButton(
                        context = context,
                        action = action,
                        containerColor = buttonContainerColor,
                        iconColor = iconColor,
                        textColor = textColor,
                        showLabel = showLabels,
                        iconSize = iconSize,
                        boxSize = iconBoxSize
                    )
                }
            }
        }
    }

    @Composable
    private fun RowScope.HorizontalWidgetButton(
        context: Context,
        action: WidgetAction,
        containerColor: Color,
        iconColor: Color,
        textColor: Color,
        showLabel: Boolean,
        iconSize: Dp,
        boxSize: Dp
    ) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(action.deepLink)).apply {
            setClass(context, com.Nightjar.caliq5.MainActivity::class.java)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        Column(
            modifier = GlanceModifier
                .fillMaxHeight()
                .defaultWeight()
                .padding(2.dp)
                .cornerRadius(24.dp)
                .clickable(actionStartActivity(intent)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = GlanceModifier
                    .size(boxSize)
                    .background(containerColor)
                    .cornerRadius(100.dp)
                    .padding(6.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    provider = ImageProvider(action.iconRes),
                    contentDescription = action.label,
                    colorFilter = ColorFilter.tint(ColorProvider(day = iconColor, night = iconColor)),
                    modifier = GlanceModifier.size(iconSize)
                )
            }
            if (showLabel) {
                Spacer(modifier = GlanceModifier.height(3.dp))
                Text(
                    text = action.label,
                    style = TextStyle(
                        fontSize = 10.sp,
                        color = ColorProvider(day = textColor, night = textColor),
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    ),
                    maxLines = 1
                )
            }
        }
    }

    @Composable
    private fun ColumnScope.VerticalWidgetButton(
        context: Context,
        action: WidgetAction,
        containerColor: Color,
        iconColor: Color,
        textColor: Color,
        showLabel: Boolean,
        iconSize: Dp,
        boxSize: Dp
    ) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(action.deepLink)).apply {
            setClass(context, com.Nightjar.caliq5.MainActivity::class.java)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        Box(
            modifier = GlanceModifier
                .fillMaxWidth()
                .defaultWeight()
                .padding(2.dp)
                .cornerRadius(24.dp)
                .clickable(actionStartActivity(intent)),
            contentAlignment = Alignment.Center
        ) {
            if (showLabel) {
                Row(
                    modifier = GlanceModifier.fillMaxSize().padding(horizontal = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = GlanceModifier
                            .size(boxSize)
                            .background(containerColor)
                            .cornerRadius(100.dp)
                            .padding(6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            provider = ImageProvider(action.iconRes),
                            contentDescription = action.label,
                            colorFilter = ColorFilter.tint(ColorProvider(day = iconColor, night = iconColor)),
                            modifier = GlanceModifier.size(iconSize)
                        )
                    }
                    Spacer(modifier = GlanceModifier.width(6.dp))
                    Text(
                        text = action.label,
                        style = TextStyle(
                            fontSize = 11.sp,
                            color = ColorProvider(day = textColor, night = textColor),
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Start
                        ),
                        maxLines = 1
                    )
                }
            } else {
                Box(
                    modifier = GlanceModifier
                        .size(boxSize)
                        .background(containerColor)
                        .cornerRadius(100.dp)
                        .padding(6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        provider = ImageProvider(action.iconRes),
                        contentDescription = action.label,
                        colorFilter = ColorFilter.tint(ColorProvider(day = iconColor, night = iconColor)),
                        modifier = GlanceModifier.size(iconSize)
                    )
                }
            }
        }
    }
}
