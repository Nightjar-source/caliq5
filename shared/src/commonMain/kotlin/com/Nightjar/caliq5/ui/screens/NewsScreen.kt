package com.Nightjar.caliq5.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Nightjar.caliq5.ui.components.*
import com.Nightjar.caliq5.viewmodel.AppViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

@Composable
fun NewsScreen(viewModel: AppViewModel) {
    val gridState = androidx.compose.foundation.lazy.grid.rememberLazyGridState()
    CaliqScreen(title = "آخر الأخبار", onBack = { viewModel.goBack() }, scrollable = false) {
        androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
            state = gridState,
            columns = androidx.compose.foundation.lazy.grid.GridCells.Adaptive(minSize = 280.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 120.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(), top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 65.dp),
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)
        ) {
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                Box(modifier = Modifier.fillMaxWidth().popInOnInitialLoad(0)) {
                    ResultCard {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "شاشة الأخبار عبر RSS (قيد التطوير)",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "هنا يتم شاشة عرض اخر الاخبار من rss",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.surface,
                                shape = MaterialTheme.shapes.small,
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primaryContainer)
                            ) {
                                Text(
                                    text = "https://feeds.feedburner.com/karraraliraqii/gkhbdxas1m0",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.primary,
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                    ),
                                    modifier = Modifier.padding(10.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                Box(modifier = Modifier.fillMaxWidth().popInOnInitialLoad(1)) {
                    Text(
                        text = "أخبار مقترحة (تصميم تجريبي)",
                        style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 8.dp),
                        textAlign = TextAlign.Start
                    )
                }
            }

            item {
                Box(modifier = Modifier.fillMaxWidth().popInOnInitialLoad(2)) {
                    NewsItemCard(
                        title = "وزارة التربية تعلن ضوابط الإعفاء",
                        description = "أعلنت وزارة التربية عن التعليمات الجديدة الخاصة بالامتحانات والإعفاء العام والفردي...",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

        }
    }
}

@Composable
private fun NewsItemCard(title: String, description: String, modifier: Modifier = Modifier.fillMaxWidth()) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        tonalElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                ),
                textAlign = TextAlign.Start
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                textAlign = TextAlign.Start,
                lineHeight = 18.sp
            )
            CaliqButton(
                onClick = { /* WebView */ },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Text("قراءة المزيد", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}
