package com.Nightjar.caliq5.ui.screens

import androidx.compose.foundation.background
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
fun UpdatesScreen(viewModel: AppViewModel) {
    val gridState = androidx.compose.foundation.lazy.grid.rememberLazyGridState()
    val topSafeInset = WindowInsets.statusBars
        .union(WindowInsets.captionBar)
        .union(WindowInsets.displayCutout)
        .asPaddingValues()
        .calculateTopPadding()
    val bottomSafeInset = WindowInsets.navigationBars
        .asPaddingValues()
        .calculateBottomPadding()

    CaliqScreen(title = "التحديثات", onBack = { viewModel.goBack() }, scrollable = false) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            val isLandscape = maxWidth > maxHeight
            val isPhoneLandscape = isLandscape && (maxHeight < 540.dp || maxWidth < 840.dp)
            val gridColumns = when {
                isPhoneLandscape -> androidx.compose.foundation.lazy.grid.GridCells.Fixed(2)
                maxWidth >= 1200.dp -> androidx.compose.foundation.lazy.grid.GridCells.Adaptive(minSize = 290.dp)
                maxWidth >= 840.dp -> androidx.compose.foundation.lazy.grid.GridCells.Adaptive(minSize = 310.dp)
                maxWidth >= 600.dp -> androidx.compose.foundation.lazy.grid.GridCells.Fixed(2)
                else -> androidx.compose.foundation.lazy.grid.GridCells.Fixed(1)
            }

            val containerMaxWidth = when {
                maxWidth >= 1400.dp -> 1340.dp
                maxWidth >= 1100.dp -> 1140.dp
                maxWidth >= 840.dp -> 960.dp
                maxWidth >= 600.dp -> 840.dp
                else -> maxWidth
            }

            androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                state = gridState,
                columns = gridColumns,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = topSafeInset + 65.dp, bottom = 145.dp + bottomSafeInset),
                modifier = Modifier.widthIn(max = containerMaxWidth).fillMaxWidth().padding(horizontal = 16.dp)
            ) {
            item(key = "updates_header", span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                Box(modifier = Modifier.fillMaxWidth().popInOnInitialLoad(0)) {
                    ResultCard {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "التحديثات المباشرة (قيد التطوير)",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "هنا سيتم عرض الإشعارات المستلمة من السيرفر مباشرة.",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                Box(modifier = Modifier.fillMaxWidth().popInOnInitialLoad(1)) {
                    Text(
                        text = "إشعارات سابقة (تصميم تجريبي)",
                        style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                        textAlign = TextAlign.Start
                    )
                }
            }

            item {
                Box(modifier = Modifier.fillMaxWidth().popInOnInitialLoad(2)) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        tonalElevation = 1.dp
                    ) {

                    Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .fillMaxHeight()
                                .background(com.Nightjar.caliq5.theme.successColor)
                        )
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "تحديث جديد متاح 🚀",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                ),
                                textAlign = TextAlign.Start
                            )
                            Text(
                                text = "تم إطلاق الإصدار الجديد من حاسبة الإعفاء بميزات حصرية، يمكنك التحديث الآن لتجربة أفضل.",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                                textAlign = TextAlign.Start,
                                lineHeight = 18.sp
                            )
                            CaliqButton(
                                onClick = { },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            ) {
                                Text("التفاصيل", style = MaterialTheme.typography.labelMedium)
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

