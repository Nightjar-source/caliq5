package com.Nightjar.caliq5.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.abs

class WidgetConfigActivity : ComponentActivity() {
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set result CANCELED by default in case user exits without saving
        setResult(Activity.RESULT_CANCELED)

        val intent = intent
        val extras = intent.extras
        if (extras != null) {
            appWidgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )
        }

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WidgetConfigScreen(
                        onSave = { bgStyle, opacity ->
                            savePreferencesAndFinish(bgStyle, opacity)
                        },
                        onCancel = {
                            finish()
                        }
                    )
                }
            }
        }
    }

    private fun savePreferencesAndFinish(bgStyle: String, opacity: Float) {
        val prefs = getSharedPreferences("caliq_widget_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("widget_bg_style", bgStyle)
            .putFloat("widget_opacity", opacity)
            .commit()

        // Immediate update to all widgets via PlatformWidget
        updateAppWidget()

        val resultValue = Intent().apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        setResult(Activity.RESULT_OK, resultValue)
        finish()
    }
}

private data class ConfigOption(
    val key: String,
    val title: String,
    val subtitle: String
)

@Composable
fun WidgetConfigScreen(
    onSave: (String, Float) -> Unit,
    onCancel: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences("caliq_widget_prefs", Context.MODE_PRIVATE) }

    var selectedStyle by remember {
        mutableStateOf(prefs.getString("widget_bg_style", "material_you") ?: "material_you")
    }
    var selectedOpacity by remember {
        mutableStateOf(prefs.getFloat("widget_opacity", 0.88f))
    }

    val styles = listOf(
        ConfigOption("material_you", "متريال يو ديناميكي (Material You)", "يتطابق مع خلفية شاشة هاتفك وألوان النظام تلقائياً"),
        ConfigOption("frosted_glass", "خلفية زجاجية غائمة (Blur Glass)", "مظهر زجاجي شفاف فاخر يعكس ما خلفه بضبابية أنيقة"),
        ConfigOption("pure_black", "أسود فاحم (OLED Black)", "أسود داكن عميق موفر للبطارية مع عناصر عالية التباين"),
        ConfigOption("app_theme", "ثيم التطبيق الأساسي", "يتماشى مع لون الثيم المختار داخل التطبيق بدقة")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Column {
                Text(
                    text = "تخصيص مظهر الويدجت",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "اختر نمط الخلفية والشفافية المناسبة لواجهة هاتفك",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }
        }

        HorizontalDivider()

        // Background Style Selection
        Text(
            text = "نمط خلفية الويدجت:",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        )

        styles.forEach { option ->
            val isSelected = selectedStyle == option.key
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { selectedStyle = option.key },
                shape = RoundedCornerShape(16.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                border = BorderStroke(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = { selectedStyle = option.key }
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = option.title,
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = option.subtitle,
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }
            }
        }

        // Opacity Selection
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "درجة شفافية الخلفية:",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val opacities = listOf(
                0.72f to "شفاف (72%)",
                0.88f to "متوازن (88%)",
                0.96f to "شبه صلب (96%)"
            )
            opacities.forEach { (alphaVal, label) ->
                val isSelected = abs(selectedOpacity - alphaVal) < 0.05f
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedOpacity = alphaVal },
                    label = { Text(label, style = MaterialTheme.typography.bodySmall) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("إلغاء")
            }
            Button(
                onClick = { onSave(selectedStyle, selectedOpacity) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("حفظ وتطبيق")
            }
        }
    }
}
