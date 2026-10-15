package com.Nightjar.caliq5.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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

@Composable
fun WidgetScreen(viewModel: AppViewModel) {
    CaliqScreen(title = "إعداد التطبيق المصغر", onBack = { viewModel.goBack() }, scrollable = false) {
        androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
            columns = androidx.compose.foundation.lazy.grid.GridCells.Adaptive(minSize = 280.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 120.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(), top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 65.dp),
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)
        ) {
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .popInOnInitialLoad(0),
                    contentAlignment = Alignment.Center
                ) {
                    Box(modifier = Modifier.widthIn(max = 520.dp).fillMaxWidth()) {
                        ResultCard {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Build,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                
                                Text(
                                    text = "يمكنك إضافة \"ودجيت\" (تطبيق مصغر) إلى شاشتك الرئيسية للوصول السريع إلى حاسبة الإعفاء ومتابعة معدلك مباشرة.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                
                                Text(
                                    text = "طريقة الإضافة:\nاضغط على الزر أدناه لإضافة الودجيت إلى الشاشة الرئيسية مباشرة.",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                
                                CaliqButton(
                                    onClick = { viewModel.requestPinWidget() },
                                    modifier = Modifier.actionButtonAdaptive(maxWidth = 360.dp),
                                    contentPadding = PaddingValues(14.dp)
                                ) {
                                    Icon(Icons.Default.Home, null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("إضافة للشاشة الرئيسية", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
