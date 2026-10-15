package com.Nightjar.caliq5.ui.screens

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
import com.Nightjar.caliq5.ui.components.*
import com.Nightjar.caliq5.viewmodel.AppViewModel

@Composable
fun AboutScreen(viewModel: AppViewModel) {
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
    val gridState = androidx.compose.foundation.lazy.grid.rememberLazyGridState()
    CaliqScreen(title = "حول التطبيق", onBack = { viewModel.goBack() }, scrollable = false) {
        androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
            state = gridState,
            columns = androidx.compose.foundation.lazy.grid.GridCells.Adaptive(minSize = 280.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 120.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(), top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 65.dp),
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)
        ) {
            // App Icon & Animated Shimmer Title
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
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
                            org.jetbrains.compose.resources.painterResource(Res.drawable.icon)?.let { painter ->
                                androidx.compose.foundation.Image(
                                    painter = painter,
                                    contentDescription = "أيقونة التطبيق",
                                    modifier = Modifier.size(90.dp),
                                )
                            }
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
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // App Description Card
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                Box(modifier = Modifier.fillMaxWidth().popInOnInitialLoad(1)) {
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

            // Developer Section Header
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                Text(
                    text = "المطور والتواصل",
                    style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold),
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp).popInOnInitialLoad(2),
                    textAlign = TextAlign.Start
                )
            }

            // Developer Profile
            item {
                val isDark = com.Nightjar.caliq5.theme.isAppInDarkTheme()
                Surface(
                    modifier = Modifier.fillMaxWidth().popInOnInitialLoad(3),
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
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "عبودي",
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

            // Telegram Contact Card
            item {
                val isDark = com.Nightjar.caliq5.theme.isAppInDarkTheme()
                Surface(
                    onClick = { uriHandler.openUri("https://t.me/DevAboudi") },
                    modifier = Modifier.fillMaxWidth().popInOnInitialLoad(4),
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
                                modifier = Modifier.size(40.dp).background(Color(0xFF0088CC), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Text(
                                text = "عبر تيليجرام",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Golden Spiritual Card placed at the very bottom with compact size and shimmering calligraphy
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    GoldenSpiritualCard(modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}

