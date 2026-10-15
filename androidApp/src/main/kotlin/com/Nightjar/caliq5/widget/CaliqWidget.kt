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
}

class CaliqWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val settings = try {
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
        val size = LocalSize.current
        val isVertical = size.height > size.width * 1.15f

        val isDark = when (settings.theme) {
            "dark" -> true
            "light" -> false
            else -> {
                val nightModeFlags = context.resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
                nightModeFlags == android.content.res.Configuration.UI_MODE_NIGHT_YES
            }
        }

        val primaryColorObj = try {
            Color(android.graphics.Color.parseColor(settings.primaryColor))
        } catch (e: Exception) {
            Color(0xFF6750A4)
        }

        // Material 3 Translucent Dynamic Background (Samsung One UI & Google Pixel style)
        val widgetBg = if (isDark) {
            if (settings.pureBlackMode) Color.Black.copy(alpha = 0.90f) else Color(0xFF1E1C22).copy(alpha = 0.88f)
        } else {
            if (settings.pureWhiteMode) Color.White.copy(alpha = 0.94f) else Color(0xFFF9F7FA).copy(alpha = 0.90f)
        }

        val buttonContainerColor = if (isDark) {
            primaryColorObj.copy(alpha = 0.22f)
        } else {
            primaryColorObj.copy(alpha = 0.14f)
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
        action: WidgetAction,
        containerColor: Color,
        iconColor: Color,
        textColor: Color,
        showLabel: Boolean,
        iconSize: Dp,
        boxSize: Dp
    ) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(action.deepLink)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
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
        action: WidgetAction,
        containerColor: Color,
        iconColor: Color,
        textColor: Color,
        showLabel: Boolean,
        iconSize: Dp,
        boxSize: Dp
    ) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(action.deepLink)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
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
