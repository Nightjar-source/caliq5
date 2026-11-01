package com.Nightjar.caliq5.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.Nightjar.caliq5.data.AppSettings
import com.Nightjar.caliq5.theme.CaliqTheme
import com.Nightjar.caliq5.ui.background.MathBackground
import com.Nightjar.caliq5.ui.components.CaliqButton
import com.Nightjar.caliq5.ui.components.CaliqOutlinedButton
import com.Nightjar.caliq5.viewmodel.DataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

class WidgetConfigActivity : ComponentActivity() {
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.auto(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
        )
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
            window.isStatusBarContrastEnforced = false
        }
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        super.onCreate(savedInstanceState)
        setResult(Activity.RESULT_CANCELED)

        val extras = intent.extras
        if (extras != null) {
            appWidgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )
        }

        setContent {
            val settings = remember { DataRepository.cachedSettings ?: AppSettings() }
            CaliqTheme(settings = settings) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        MathBackground(modifier = Modifier.fillMaxSize())
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
    }

    private fun savePreferencesAndFinish(bgStyle: String, opacity: Float) {
        lifecycleScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO) {
                val prefs = getSharedPreferences("caliq_widget_prefs", Context.MODE_PRIVATE)
                prefs.edit()
                    .putString("widget_bg_style", bgStyle)
                    .putFloat("widget_opacity", opacity)
                    .commit()

                // Immediate synchronous update to Glance widget and AppWidgetManager
                try {
                    val receiverClass = Class.forName("com.Nightjar.caliq5.widget.CaliqWidgetReceiver")
                    try {
                        val updateSyncMethod = receiverClass.getMethod("updateWidgetSync", Context::class.java, Int::class.javaPrimitiveType)
                        updateSyncMethod.invoke(null, applicationContext, appWidgetId)
                    } catch (_: Throwable) {
                        val updateMethod = receiverClass.getMethod("updateWidget", Context::class.java)
                        updateMethod.invoke(null, applicationContext)
                    }
                } catch (_: Throwable) {
                    updateAppWidget()
                }
            }

            val resultValue = Intent().apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            setResult(Activity.RESULT_OK, resultValue)
            finish()
        }
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
        val saved = prefs.getString("widget_bg_style", "app_theme") ?: "app_theme"
        // Normalize legacy keys
        val normalized = if (saved == "material_you" || saved == "app_theme") "app_theme" else saved
        mutableStateOf(normalized)
    }
    var selectedOpacity by remember {
        mutableStateOf(prefs.getFloat("widget_opacity", 0.88f))
    }

    val styles = listOf(
        ConfigOption(
            key = "app_theme",
            title = "ثيم وتصميم التطبيق (Material You / الثيم المختار)",
            subtitle = "يتطابق تلقائياً مع خياراتك داخل التطبيق (ألوان متريال يو الديناميكية الرسمية، أو لون الثيم المخصص)"
        ),
        ConfigOption(
            key = "frosted_blur",
            title = "خلفية زجاجية غائمة (Frosted Blur Glass)",
            subtitle = "مظهر زجاجي مثلج حقيقي مع تأثير ضبابي وانعكاس بلوري نقي لخلفية هاتفك"
        ),
        ConfigOption(
            key = "pure_black",
            title = "أسود فاحم (OLED Black)",
            subtitle = "خلفية داكنة عميقة موفرة للطاقة مع تباين فائق للأيقونات والنصوص"
        )
    )

    val isFrostedGlass = selectedStyle == "frosted_blur" || selectedStyle == "frosted_glass"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                modifier = Modifier.size(46.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                shadowElevation = 2.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "تخصيص مظهر التطبيق المصغر",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "تحكم بنمط الخلفية ونسبة التباين والانعكاس الفوري",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.40f))

        // Background Style Selection
        Text(
            text = "اختر نمط الخلفية:",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            styles.forEach { option ->
                val isSelected = selectedStyle == option.key || (selectedStyle == "frosted_glass" && option.key == "frosted_blur")
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .clickable { selectedStyle = option.key },
                    shape = RoundedCornerShape(18.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.50f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { selectedStyle = option.key }
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = option.title,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = option.subtitle,
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                    }
                }
            }
        }

        if (isFrostedGlass) {
            // Authentic Frosted Blur info card (No fake opacity slider)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "خلفية زجاجية بضبابية بصرية نقية (Real Frosted Blur): تم ضبط تأثير الضبابية البلوري بدقة لعكس خلفية هاتفك بانسيابية حقيقية دون الاعتماد على نسبة الشفافية.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 18.sp
                        )
                    )
                }
            }
        } else {
            // Opacity Slider (0% to 100%) for App Theme / OLED Black
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.40f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.30f))
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Tune, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Text(
                                text = "درجة شفافية الخلفية:",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            )
                        }
                        val pct = (selectedOpacity * 100).roundToInt()
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                text = "$pct%",
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Slider(
                        value = selectedOpacity,
                        onValueChange = { selectedOpacity = it },
                        valueRange = 0f..1f,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Quick preset buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val presets = listOf(
                            0.0f to "0% (شفاف)",
                            0.50f to "50%",
                            0.85f to "85% (متوازن)",
                            1.0f to "100% (معتم)"
                        )
                        presets.forEach { (alphaVal, label) ->
                            val isPresetActive = kotlin.math.abs(selectedOpacity - alphaVal) < 0.04f
                            FilterChip(
                                selected = isPresetActive,
                                onClick = { selectedOpacity = alphaVal },
                                label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            CaliqOutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Close, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("إلغاء")
            }
            CaliqButton(
                onClick = { onSave(selectedStyle, selectedOpacity) },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("حفظ وتطبيق")
            }
        }
    }
}
