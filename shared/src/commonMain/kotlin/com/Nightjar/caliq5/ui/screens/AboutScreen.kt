package com.Nightjar.caliq5.ui.screens

import androidx.compose.ui.draw.clipToBounds
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import caliq5.shared.generated.resources.Res
import caliq5.shared.generated.resources.icon
import caliq5.shared.generated.resources.developer_karber
import com.Nightjar.caliq5.ui.components.*
import com.Nightjar.caliq5.viewmodel.AppViewModel

@Composable
fun AboutScreen(viewModel: AppViewModel) {
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
    val gridState = androidx.compose.foundation.lazy.grid.rememberLazyGridState()
    val topSafeInset = WindowInsets.statusBars
        .union(WindowInsets.captionBar)
        .union(WindowInsets.displayCutout)
        .asPaddingValues()
        .calculateTopPadding()
    val bottomSafeInset = WindowInsets.navigationBars
        .asPaddingValues()
        .calculateBottomPadding()

    CaliqScreen(title = "حول التطبيق", onBack = { viewModel.goBack() }, scrollable = false) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            val isLandscape = maxWidth > maxHeight
            val isPhoneLandscape = isLandscape && (maxHeight < 540.dp || maxWidth < 840.dp)
            val gridColumns = when {
                isPhoneLandscape -> androidx.compose.foundation.lazy.grid.GridCells.Fixed(2)
                maxWidth >= 600.dp -> androidx.compose.foundation.lazy.grid.GridCells.Adaptive(minSize = 340.dp)
                else -> androidx.compose.foundation.lazy.grid.GridCells.Fixed(1)
            }

            val containerMaxWidth = when {
                maxWidth >= 1400.dp -> 1340.dp
                maxWidth >= 1100.dp -> 1140.dp
                maxWidth >= 840.dp -> 960.dp
                else -> maxWidth
            }

            androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                state = gridState,
                columns = gridColumns,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = topSafeInset + 65.dp, bottom = 145.dp + bottomSafeInset),
                modifier = Modifier.widthIn(max = containerMaxWidth).fillMaxWidth().clipToBounds().padding(horizontal = 16.dp)
            ) {
            // App Icon & Animated Shimmer Title
            item(key = "about_header", span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp).popInOnInitialLoad(0),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        modifier = Modifier
                            .size(90.dp)
                            .shadow(
                                elevation = 6.dp,
                                shape = androidx.compose.foundation.shape.CircleShape,
                                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                            )
                            .clip(androidx.compose.foundation.shape.CircleShape),
                        shape = androidx.compose.foundation.shape.CircleShape,
                        color = Color.Transparent
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            androidx.compose.foundation.Image(
                                painter = org.jetbrains.compose.resources.painterResource(Res.drawable.icon),
                                contentDescription = "أيقونة التطبيق",
                                modifier = Modifier.size(90.dp),
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    com.Nightjar.caliq5.ui.components.ShimmeringAppTitle(
                        text = "حاسبة الاعفاء والسعي",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = com.Nightjar.caliq5.theme.ReemKufiFontFamily
                        ),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = com.Nightjar.caliq5.data.AppVersion.FULL_DISPLAY,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                uriHandler.openUri(com.Nightjar.caliq5.data.AppVersion.GITHUB_RELEASES_URL)
                            }
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }

            // App Description Card (dedicated at the top, max width 600.dp)
            item(key = "about_desc", span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                Box(modifier = Modifier.fillMaxWidth().popInOnInitialLoad(1), contentAlignment = Alignment.Center) {
                    Box(modifier = Modifier.widthIn(max = 500.dp).fillMaxWidth()) {
                        ResultCard {
                            Text(
                                text = "تطبيق شامل ومطور خصيصاً لمساعدة الطلبة في العراق لحساب الإعفاء العام والفردي، السعي السنوي، المعدل الفصلي، ومتابعة الدرجات بدقة عالية وحفظها عبر ميزة (السجل الشامل).",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Start,
                                lineHeight = 24.sp,
                                color = MaterialTheme.colorScheme.onSurface 
                            )
                        }
                    }
                }
            }

            // Developer Profile (dedicated card, max width 600.dp, centered)
            item(key = "about_dev", span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                val isDark = com.Nightjar.caliq5.theme.isAppInDarkTheme()
                Box(modifier = Modifier.fillMaxWidth().popInOnInitialLoad(2), contentAlignment = Alignment.Center) {
                    Surface(
                        modifier = Modifier.widthIn(max = 500.dp).fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDark) 0.25f else 0.45f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Surface(
                                modifier = Modifier.size(54.dp).clip(CircleShape),
                                shape = CircleShape,
                                shadowElevation = 3.dp,
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                androidx.compose.foundation.Image(
                                    painter = org.jetbrains.compose.resources.painterResource(Res.drawable.developer_karber),
                                    contentDescription = "KarBer",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "KarBer",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    textAlign = TextAlign.Start
                                )
                                Text(
                                    text = "مطور التطبيق",
                                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.secondary),
                                    textAlign = TextAlign.Start
                                )
                            }
                        }
                    }
                }
            }

            // Channels and Bots Section Header
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                Text(
                    text = "قنواتنا وخدماتنا على تليغرام",
                    style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold),
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp).popInOnInitialLoad(4),
                    textAlign = TextAlign.Start
                )
            }

            // 1. Official Channel: https://t.me/iraqitrb
            item(key = "about_chan_official") {
                val isDark = com.Nightjar.caliq5.theme.isAppInDarkTheme()
                Surface(
                    onClick = { uriHandler.openUri("https://t.me/iraqitrb") },
                    modifier = Modifier.fillMaxWidth().popInOnInitialLoad(5),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDark) 0.25f else 0.45f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier.size(42.dp).background(Color(0xFF0088CC), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "قناتنا على تليغرام",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "@iraqitrb • القناة الرسمية للأخبار والتعليمات",
                                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // 2. Books & Guides Channel: https://t.me/bookiraqi
            item(key = "about_chan_books") {
                val isDark = com.Nightjar.caliq5.theme.isAppInDarkTheme()
                Surface(
                    onClick = { uriHandler.openUri("https://t.me/bookiraqi") },
                    modifier = Modifier.fillMaxWidth().popInOnInitialLoad(6),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDark) 0.25f else 0.45f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier.size(42.dp).background(Color(0xFF2E7D32), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MenuBook,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "قناة الكتب والملازم",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "@bookiraqi • تحميل جميع المناهج والملازم",
                                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // 3. Books Download Bot: @Bookiraqbot
            item(
                key = "about_bot_books",
                span = {
                    if (maxLineSpan == 2) {
                        androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan)
                    } else {
                        androidx.compose.foundation.lazy.grid.GridItemSpan(1)
                    }
                }
            ) {
                val isDark = com.Nightjar.caliq5.theme.isAppInDarkTheme()
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        onClick = { uriHandler.openUri("https://t.me/Bookiraqbot") },
                        modifier = Modifier.widthIn(max = 500.dp).fillMaxWidth().popInOnInitialLoad(7),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDark) 0.25f else 0.45f)
                        )
                    ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier.size(42.dp).background(Color(0xFFE65100), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SmartToy,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "بوت تحميل الكتب والملازم",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "@Bookiraqbot • بحث وتحميل فوري عبر تيليجرام",
                                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
            }

            // Golden Spiritual Card placed at the very bottom with compact size and shimmering calligraphy
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    GoldenSpiritualCard(modifier = Modifier.widthIn(max = 480.dp).fillMaxWidth())
                }
            }
        }
        }
    }
}

