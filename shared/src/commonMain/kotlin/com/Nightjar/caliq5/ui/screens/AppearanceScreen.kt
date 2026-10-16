package com.Nightjar.caliq5.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Nightjar.caliq5.data.AppSettings
import com.Nightjar.caliq5.ui.components.*
import com.Nightjar.caliq5.viewmodel.AppViewModel
import com.Nightjar.caliq5.theme.parseHexColor
import com.Nightjar.caliq5.theme.ensureReadableContrast

data class ThemeColorItem(val hex: String, val name: String, val color: Color = parseHexColor(hex))

@Composable
fun AppearanceScreen(viewModel: AppViewModel) {
    val settings by viewModel.settings.collectAsState()

    val colorPairs = remember {
        listOf(
            Pair(
                ThemeColorItem("#ec4899", "وردي سحابي"),
                ThemeColorItem("#d41145", "عنابي ملكي")
            ),
            Pair(
                ThemeColorItem("#dc2626", "أحمر ياقوتي"),
                ThemeColorItem("#ea580c", "برتقالي دافئ")
            ),
            Pair(
                ThemeColorItem("#d97706", "ذهبي كهرماني"),
                ThemeColorItem("#16a34a", "أخضر زمردي")
            ),
            Pair(
                ThemeColorItem("#0d9488", "فيروزي هادئ"),
                ThemeColorItem("#0284c7", "أزرق سماوي")
            ),
            Pair(
                ThemeColorItem("#2563eb", "أزرق ملكي"),
                ThemeColorItem("#4f46e5", "نيلي فاخر")
            ),
            Pair(
                ThemeColorItem("#7c3aed", "بنفسجي ملكي"),
                ThemeColorItem("#c026d3", "ماجنتا فوشيا")
            )
        )
    }

    CaliqScreen(
        title = "إعدادات المظهر",
        onBack = { viewModel.goBack() },
        scrollable = true
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val isWide = maxWidth >= 580.dp

            Column(
                modifier = Modifier
                    .widthIn(max = 780.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isWide) {
                    // Wide screens & Tablets in landscape: Theme Mode & Advanced Themes side-by-side
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        ThemeModeCard(
                            settings = settings,
                            onThemeChange = { viewModel.updateSettings(settings.copy(theme = it)) },
                            modifier = Modifier.weight(1f)
                        )
                        AdvancedThemesCard(
                            settings = settings,
                            onWhiteChange = { viewModel.updateSettings(settings.copy(pureWhiteMode = it)) },
                            onBlackChange = { viewModel.updateSettings(settings.copy(pureBlackMode = it)) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    // Portrait Compact Phones: stacked vertically
                    ThemeModeCard(
                        settings = settings,
                        onThemeChange = { viewModel.updateSettings(settings.copy(theme = it)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    AdvancedThemesCard(
                        settings = settings,
                        onWhiteChange = { viewModel.updateSettings(settings.copy(pureWhiteMode = it)) },
                        onBlackChange = { viewModel.updateSettings(settings.copy(pureBlackMode = it)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Card 3: Color Palette selection
                val paletteArrowAngle by androidx.compose.animation.core.animateFloatAsState(
                    targetValue = if (!settings.useMaterialYou) 180f else 0f,
                    animationSpec = androidx.compose.animation.core.spring(dampingRatio = 0.65f, stiffness = 450f),
                    label = "palette_arrow"
                )
                val paletteArrowScale by androidx.compose.animation.core.animateFloatAsState(
                    targetValue = if (!settings.useMaterialYou) 1.15f else 1.0f,
                    animationSpec = androidx.compose.animation.core.spring(dampingRatio = 0.65f, stiffness = 450f),
                    label = "palette_arrow_scale"
                )

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .popInOnInitialLoad(2),
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Column {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "ألوان التطبيق",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { viewModel.updateSettings(settings.copy(useMaterialYou = !settings.useMaterialYou)) },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "ألوان النظام الديناميكية (Material You)",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                    Text(
                                        text = "تطبيق ألوان متناسقة مع خلفية جهازك",
                                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = "تبديل القائمة",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.graphicsLayer {
                                            rotationZ = paletteArrowAngle
                                            scaleX = paletteArrowScale
                                            scaleY = paletteArrowScale
                                        }
                                    )
                                    Switch(
                                        checked = settings.useMaterialYou,
                                        onCheckedChange = { viewModel.updateSettings(settings.copy(useMaterialYou = it)) }
                                    )
                                }
                            }
                        }

                        AnimatedVisibility(
                            visible = !settings.useMaterialYou,
                            modifier = Modifier.fillMaxWidth().clipToBounds(),
                            enter = expandVertically(
                                animationSpec = androidx.compose.animation.core.spring(
                                    dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                                    stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow
                                )
                            ) + fadeIn(animationSpec = androidx.compose.animation.core.tween(180)),
                            exit = shrinkVertically(
                                animationSpec = androidx.compose.animation.core.spring(
                                    dampingRatio = androidx.compose.animation.core.Spring.DampingRatioNoBouncy,
                                    stiffness = androidx.compose.animation.core.Spring.StiffnessMedium
                                ),
                                shrinkTowards = Alignment.Top
                            ) + fadeOut(animationSpec = androidx.compose.animation.core.tween(130))
                        ) {
                            Box(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().clipToBounds(),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = "اختر لونك المفضل (12 لوناً متناسقاً):",
                                        style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    )
                                
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    val isDark = com.Nightjar.caliq5.theme.isAppInDarkTheme()
                                    val cardShape = remember { RoundedCornerShape(16.dp) }
                                    val outlineVariant = MaterialTheme.colorScheme.outlineVariant
                                    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

                                    colorPairs.forEach { (colorA, colorB) ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            listOf(colorA, colorB).forEach { colorItem ->
                                                val isSelected = (!settings.useMaterialYou) && (
                                                    settings.primaryColor.equals(colorItem.hex, ignoreCase = true) ||
                                                    (colorItem.hex.equals("#d41145", ignoreCase = true) && (settings.primaryColor.equals("#800020", ignoreCase = true) || settings.primaryColor.equals("#9E1B3E", ignoreCase = true)))
                                                )
                                                val colorObj = remember(colorItem.hex, isDark) {
                                                    ensureReadableContrast(parseHexColor(colorItem.hex), isDark)
                                                }
                                                val borderColor = if (isSelected) colorObj else outlineVariant.copy(alpha = if (isDark) 0.55f else 0.65f)
                                                val cardBorder = remember(isSelected, colorObj, isDark) {
                                                    BorderStroke(width = if (isSelected) 2.5.dp else 1.5.dp, color = borderColor)
                                                }
                                                
                                                val cardScale by androidx.compose.animation.core.animateFloatAsState(
                                                    targetValue = if (isSelected) 1.025f else 1f,
                                                    animationSpec = androidx.compose.animation.core.spring(
                                                        dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                                                        stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow
                                                    ),
                                                    label = "color_card_scale"
                                                )

                                                val circleScale by androidx.compose.animation.core.animateFloatAsState(
                                                    targetValue = if (isSelected) 1.10f else 1f,
                                                    animationSpec = androidx.compose.animation.core.spring(
                                                        dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                                                        stiffness = androidx.compose.animation.core.Spring.StiffnessLow
                                                    ),
                                                    label = "color_circle_scale"
                                                )

                                                Surface(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .graphicsLayer {
                                                            scaleX = cardScale
                                                            scaleY = cardScale
                                                        }
                                                        .clip(cardShape)
                                                        .clickable {
                                                            viewModel.updateSettings(settings.copy(primaryColor = colorItem.hex, useMaterialYou = false))
                                                        },
                                                    shape = cardShape,
                                                    color = if (isSelected) colorObj.copy(alpha = if (isDark) 0.22f else 0.15f)
                                                            else surfaceVariant.copy(alpha = if (isDark) 0.35f else 0.45f),
                                                    border = cardBorder
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(34.dp)
                                                                .graphicsLayer {
                                                                    scaleX = circleScale
                                                                    scaleY = circleScale
                                                                }
                                                                .clip(CircleShape)
                                                                .background(colorObj)
                                                                .border(1.8.dp, if (isSelected) Color.White else Color.White.copy(alpha = 0.55f), CircleShape),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            androidx.compose.animation.AnimatedVisibility(
                                                                visible = isSelected,
                                                                enter = androidx.compose.animation.scaleIn(
                                                                    animationSpec = androidx.compose.animation.core.spring(
                                                                        dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                                                                        stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow
                                                                    )
                                                                ) + androidx.compose.animation.fadeIn(),
                                                                exit = androidx.compose.animation.scaleOut() + androidx.compose.animation.fadeOut()
                                                            ) {
                                                                Icon(
                                                                    imageVector = Icons.Default.Done,
                                                                    contentDescription = "Selected",
                                                                    tint = Color.White,
                                                                    modifier = Modifier.size(18.dp)
                                                                )
                                                            }
                                                        }
                                                        Text(
                                                            text = colorItem.name,
                                                            style = MaterialTheme.typography.bodySmall.copy(
                                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                                color = if (isSelected) colorObj else MaterialTheme.colorScheme.onSurface
                                                            ),
                                                            maxLines = 1,
                                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
}

@Composable
private fun ThemeModeCard(
    settings: AppSettings,
    onThemeChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .popInOnInitialLoad(0)
            .animateContentSize(
                animationSpec = tween(350, easing = FastOutSlowInEasing)
            ),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "وضع المظهر",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            )

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
            ) {
                val options = listOf("light", "dark", "system")
                val selectedIndex = options.indexOf(settings.theme).takeIf { it >= 0 } ?: 2
                
                val tabWidth = maxWidth / 3
                val indicatorOffset by androidx.compose.animation.core.animateDpAsState(
                    targetValue = tabWidth * selectedIndex,
                    animationSpec = androidx.compose.animation.core.spring(
                        dampingRatio = androidx.compose.animation.core.Spring.DampingRatioNoBouncy,
                        stiffness = androidx.compose.animation.core.Spring.StiffnessMedium
                    )
                )

                // Sliding Indicator
                Box(
                    modifier = Modifier
                        .offset(x = indicatorOffset)
                        .width(tabWidth)
                        .fillMaxHeight()
                        .padding(4.dp)
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(10.dp))
                )

                Row(modifier = Modifier.fillMaxSize()) {
                    ThemeOptionButton(
                        label = "فاتح",
                        isSelected = settings.theme == "light",
                        onClick = { onThemeChange("light") },
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    )
                    ThemeOptionButton(
                        label = "مظلم",
                        isSelected = settings.theme == "dark",
                        onClick = { onThemeChange("dark") },
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    )
                    ThemeOptionButton(
                        label = "تلقائي",
                        isSelected = settings.theme == "system",
                        onClick = { onThemeChange("system") },
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    )
                }
            }
        }
    }
}

@Composable
private fun AdvancedThemesCard(
    settings: AppSettings,
    onWhiteChange: (Boolean) -> Unit,
    onBlackChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val isCurrentlyDark = com.Nightjar.caliq5.theme.isAppInDarkTheme()

    Surface(
        modifier = modifier
            .popInOnInitialLoad(1)
            .animateContentSize(
                animationSpec = spring(dampingRatio = 0.85f, stiffness = 400f)
            ),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "السمات المتقدمة",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            )

            AnimatedContent(
                targetState = isCurrentlyDark,
                transitionSpec = {
                    (fadeIn(spring(dampingRatio = 0.8f, stiffness = 380f)) + 
                     expandVertically(spring(dampingRatio = 0.8f, stiffness = 380f)))
                        .togetherWith(
                            fadeOut(tween(140)) + 
                            shrinkVertically(spring(dampingRatio = 0.85f, stiffness = 420f))
                        )
                },
                label = "advanced_theme_morph"
            ) { dark ->
                if (!dark) {
                    // Pure White option
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "الوضع الأبيض النقي",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                text = "خلفية بيضاء نقية تماماً لجميع عناصر التطبيق",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                        Switch(
                            checked = settings.pureWhiteMode,
                            onCheckedChange = onWhiteChange
                        )
                    }
                } else {
                    // Pure Black option
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "الوضع الأسود النقي (OLED)",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                text = "خلفية سوداء حالكة لتوفير بطارية الشاشات",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                        Switch(
                            checked = settings.pureBlackMode,
                            onCheckedChange = onBlackChange
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemeOptionButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val textColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    
    Box(
        modifier = modifier
            .padding(2.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(
                    color = textColor,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                ),
                textAlign = TextAlign.Center
            )
    }
}
