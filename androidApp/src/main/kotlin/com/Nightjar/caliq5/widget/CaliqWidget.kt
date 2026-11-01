package com.Nightjar.caliq5.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.color.ColorProvider
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.Nightjar.caliq5.R
import com.Nightjar.caliq5.data.AppSettings
import com.Nightjar.caliq5.viewmodel.DataRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

data class WidgetConfigData(
    val bgStyle: String,
    val opacity: Float,
    val effectiveTheme: String,
    val effectivePrimaryColor: String,
    val prefUseMaterialYou: Boolean,
    val prefPureBlack: Boolean,
    val prefPureWhite: Boolean,
    val updateTimestamp: Long
)

class CaliqWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CaliqWidget()

    companion object {
        val widgetUpdateTrigger = mutableStateOf(System.currentTimeMillis())

        @JvmStatic
        fun updateWidgetSync(context: Context, targetWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID) {
            widgetUpdateTrigger.value = System.currentTimeMillis()
            val widget = CaliqWidget()
            try {
                val manager = GlanceAppWidgetManager(context)
                runBlocking {
                    if (targetWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                        try {
                            val glanceId = manager.getGlanceIdBy(targetWidgetId)
                            widget.update(context, glanceId)
                        } catch (_: Throwable) {
                            widget.updateAll(context)
                        }
                    } else {
                        val ids = manager.getGlanceIds(CaliqWidget::class.java)
                        if (ids.isNotEmpty()) {
                            ids.forEach { widget.update(context, it) }
                        } else {
                            widget.updateAll(context)
                        }
                    }
                }
            } catch (_: Throwable) {
                runBlocking {
                    try {
                        widget.updateAll(context)
                    } catch (_: Throwable) {}
                }
            }

            // Force notify AppWidgetManager on Xiaomi HyperOS / MIUI / Samsung
            try {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val provider = ComponentName(context, CaliqWidgetReceiver::class.java)
                val ids = if (targetWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                    intArrayOf(targetWidgetId)
                } else {
                    appWidgetManager.getAppWidgetIds(provider)
                }
                if (ids != null && ids.isNotEmpty()) {
                    val updateIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE).apply {
                        component = provider
                        putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                    }
                    context.sendBroadcast(updateIntent)
                }
            } catch (_: Throwable) {}
        }

        @JvmStatic
        fun updateWidget(context: Context) {
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                updateWidgetSync(context, AppWidgetManager.INVALID_APPWIDGET_ID)
            }
        }
    }
}

class CaliqWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        try {
            DataRepository.loadSettings()
        } catch (_: Throwable) {}

        provideContent {
            val updateTick = CaliqWidgetReceiver.widgetUpdateTrigger.value
            val settings = DataRepository.cachedSettings ?: AppSettings()

            val widgetPrefs = context.getSharedPreferences("caliq_widget_prefs", Context.MODE_PRIVATE)
            val prefTheme = widgetPrefs.getString("theme", null)
            val prefPrimaryColor = widgetPrefs.getString("primary_color", null)
            val prefUseMaterialYou = if (widgetPrefs.contains("use_material_you")) widgetPrefs.getBoolean("use_material_you", false) else settings.useMaterialYou
            val prefPureBlack = if (widgetPrefs.contains("pure_black_mode")) widgetPrefs.getBoolean("pure_black_mode", false) else settings.pureBlackMode
            val prefPureWhite = if (widgetPrefs.contains("pure_white_mode")) widgetPrefs.getBoolean("pure_white_mode", false) else settings.pureWhiteMode

            val config = WidgetConfigData(
                bgStyle = widgetPrefs.getString("widget_bg_style", "app_theme") ?: "app_theme",
                opacity = widgetPrefs.getFloat("widget_opacity", 0.88f),
                effectiveTheme = prefTheme ?: settings.theme,
                effectivePrimaryColor = prefPrimaryColor ?: settings.primaryColor,
                prefUseMaterialYou = prefUseMaterialYou,
                prefPureBlack = prefPureBlack,
                prefPureWhite = prefPureWhite,
                updateTimestamp = updateTick
            )

            WidgetContent(context, settings, config)
        }
    }

    private data class WidgetAction(
        val label: String,
        @DrawableRes val iconRes: Int,
        val deepLink: String
    )

    @Composable
    private fun WidgetContent(context: Context, settings: AppSettings, config: WidgetConfigData) {
        val size = LocalSize.current
        val isVertical = size.height > size.width * 1.15f

        val isDark = when (config.effectiveTheme) {
            "dark" -> true
            "light" -> false
            else -> {
                val nightModeFlags = context.resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
                nightModeFlags == android.content.res.Configuration.UI_MODE_NIGHT_YES
            }
        }

        val isDynamic = config.prefUseMaterialYou && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S

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
                val themeHex = config.effectivePrimaryColor
                val effectiveHex = if (themeHex.equals("#800020", ignoreCase = true) || themeHex.equals("#9E1B3E", ignoreCase = true)) "#d41145" else themeHex
                Color(android.graphics.Color.parseColor(effectiveHex))
            } catch (_: Throwable) {
                Color(0xFF6750A4)
            }
        }

        val isFrosted = config.bgStyle == "frosted_blur" || config.bgStyle == "frosted_glass"
        val isPureBlack = config.bgStyle == "pure_black"

        // Background Resolution based on style
        val widgetBg = when {
            isFrosted -> {
                val frostedAlpha = config.opacity.coerceIn(0.20f, 0.90f)
                if (isDark) Color(0xFF1E202B).copy(alpha = frostedAlpha)
                else Color(0xFFEEF2F8).copy(alpha = frostedAlpha)
            }
            isPureBlack -> Color.Black.copy(alpha = config.opacity.coerceIn(0.0f, 1.0f))
            else -> { // "app_theme" or "material_you"
                if (isDynamic) {
                    try {
                        val surfaceRes = if (isDark) android.R.color.system_neutral1_900 else android.R.color.system_neutral1_50
                        val surfaceColor = Color(androidx.core.content.ContextCompat.getColor(context, surfaceRes))
                        if (isDark) {
                            if (config.prefPureBlack) Color.Black.copy(alpha = 0.95f) else surfaceColor.copy(alpha = config.opacity.coerceIn(0.0f, 1.0f))
                        } else {
                            if (config.prefPureWhite) Color.White.copy(alpha = 0.95f) else surfaceColor.copy(alpha = config.opacity.coerceIn(0.0f, 1.0f))
                        }
                    } catch (_: Throwable) {
                        if (isDark) Color(0xFF1B1A22).copy(alpha = config.opacity.coerceIn(0.0f, 1.0f)) else Color(0xFFFAF8FC).copy(alpha = config.opacity.coerceIn(0.0f, 1.0f))
                    }
                } else {
                    if (isDark) {
                        if (config.prefPureBlack) Color.Black.copy(alpha = 0.95f)
                        else Color(
                            red = 0.08f * primaryColorObj.red + 0.92f * 0.10f,
                            green = 0.08f * primaryColorObj.green + 0.92f * 0.10f,
                            blue = 0.08f * primaryColorObj.blue + 0.92f * 0.13f,
                            alpha = config.opacity.coerceIn(0.0f, 1.0f)
                        )
                    } else {
                        if (config.prefPureWhite) Color.White.copy(alpha = 0.95f)
                        else Color(
                            red = 0.06f * primaryColorObj.red + 0.94f * 0.97f,
                            green = 0.06f * primaryColorObj.green + 0.94f * 0.97f,
                            blue = 0.06f * primaryColorObj.blue + 0.94f * 0.99f,
                            alpha = config.opacity.coerceIn(0.0f, 1.0f)
                        )
                    }
                }
            }
        }

        val isNearZeroOpacity = config.opacity <= 0.22f && !isFrosted

        val buttonContainerColor = when {
            isFrosted -> if (isDark) Color.White.copy(alpha = 0.16f) else Color.White.copy(alpha = 0.55f)
            isPureBlack -> Color.White.copy(alpha = 0.16f)
            isNearZeroOpacity -> if (isDark) Color.White.copy(alpha = 0.22f) else Color.Black.copy(alpha = 0.16f)
            isDynamic -> {
                try {
                    val containerRes = if (isDark) android.R.color.system_accent1_700 else android.R.color.system_accent1_100
                    Color(androidx.core.content.ContextCompat.getColor(context, containerRes)).copy(alpha = if (isDark) 0.40f else 0.30f)
                } catch (_: Throwable) {
                    primaryColorObj.copy(alpha = if (isDark) 0.22f else 0.15f)
                }
            }
            else -> primaryColorObj.copy(alpha = if (isDark) 0.22f else 0.15f)
        }

        val iconColor = when {
            isFrosted -> primaryColorObj
            isPureBlack -> primaryColorObj
            isNearZeroOpacity -> if (isDark) Color.White else primaryColorObj
            else -> primaryColorObj
        }

        val textColor = when {
            isFrosted -> if (isDark) Color(0xFFEBEBF5) else Color(0xFF1C1B1F)
            isPureBlack -> Color.White
            isNearZeroOpacity -> if (isDark) Color.White else Color(0xFF1C1B1F)
            isDark -> Color(0xFFE6E1E5)
            else -> Color(0xFF1C1B1F)
        }

        val allActions = listOf(
            WidgetAction("الاعفاء", R.drawable.ic_widget_school, "caliq://exemption"),
            WidgetAction("السعي", R.drawable.ic_widget_functions, "caliq://annual"),
            WidgetAction("الفصلي", R.drawable.ic_widget_calendar, "caliq://monthly"),
            WidgetAction("فصل 2", R.drawable.ic_widget_task, "caliq://term2"),
            WidgetAction("السجل", R.drawable.ic_widget_grades, "caliq://grades")
        )

        val rootModifier = GlanceModifier
            .fillMaxSize()
            .background(widgetBg)
            .cornerRadius(28.dp)

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
                modifier = rootModifier.padding(vertical = 8.dp, horizontal = 4.dp),
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
                modifier = rootModifier.padding(horizontal = 8.dp, vertical = 4.dp),
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
