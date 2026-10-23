package com.Nightjar.caliq5.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Nightjar.caliq5.ui.components.*

import com.Nightjar.caliq5.viewmodel.AppViewModel

/**
 * Statistics/History Screen.
 * Now features Swipe-to-Dismiss and exact time display.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(viewModel: AppViewModel) {
    val history by viewModel.history.collectAsState()
    var itemToDelete by remember { mutableStateOf<String?>(null) }
    var deletingItemId by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    CaliqScreen(title = "السجل", onBack = { viewModel.goBack() }, scrollable = false) {
        val topSafeInset = WindowInsets.statusBars
            .union(WindowInsets.captionBar)
            .union(WindowInsets.displayCutout)
            .asPaddingValues()
            .calculateTopPadding()

        if (itemToDelete != null) {
            com.Nightjar.caliq5.ui.components.DeleteConfirmDialog(
                onConfirm = {
                    val id = itemToDelete!!
                    itemToDelete = null
                    coroutineScope.launch {
                        deletingItemId = id
                        kotlinx.coroutines.delay(220) // Smooth fluid collapse
                        viewModel.deleteHistoryItem(id)
                        deletingItemId = null
                    }
                },
                onDismiss = { itemToDelete = null }
            )
        }
        
        androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
            columns = androidx.compose.foundation.lazy.grid.GridCells.Adaptive(minSize = 340.dp),
            contentPadding = PaddingValues(bottom = 145.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(), top = topSafeInset + 65.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)
        ) {
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                FeatureExplanationCard(
                    icon = "school",
                    text = "هنا تجد جميع النتائج التي قمت بحسابها وحفظها مسبقاً."
                )
            }

            if (history.isEmpty()) {
                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                    Box(Modifier.fillMaxWidth().padding(32.dp).popInOnInitialLoad(1), contentAlignment = Alignment.Center) {
                        Text("لا توجد نتائج محفوظة في السجل بعد.", color = MaterialTheme.colorScheme.secondary)
                    }
                }
            } else {
                itemsIndexed(history, key = { _, item -> item.id }) { index, item ->
                    val isDeleting = deletingItemId == item.id
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = {
                            if (it == SwipeToDismissBoxValue.EndToStart) {
                                itemToDelete = item.id
                                false
                            } else false
                        }
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateItem(
                                fadeInSpec = tween(240, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                                fadeOutSpec = tween(200, easing = androidx.compose.animation.core.FastOutLinearInEasing),
                                placementSpec = androidx.compose.animation.core.spring(
                                    dampingRatio = 0.78f,
                                    stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow
                                )
                            )
                            .popInOnInitialLoad(index + 1)
                    ) {
                        AnimatedVisibility(
                            visible = !isDeleting,
                            enter = fadeIn() + expandVertically(),
                            exit = shrinkVertically(
                                animationSpec = androidx.compose.animation.core.spring(
                                    dampingRatio = 0.82f,
                                    stiffness = 420f
                                )
                            ) + fadeOut(animationSpec = tween(180))
                        ) {
                        SwipeToDismissBox(
                            state = dismissState,
                            backgroundContent = {
                                val isSwiping = dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart && 
                                    (try { kotlin.math.abs(dismissState.requireOffset()) > 20f } catch (e: Exception) { false })
                                if (isSwiping) {
                                    Box(
                                        Modifier
                                            .fillMaxSize()
                                            .background(MaterialTheme.colorScheme.errorContainer, MaterialTheme.shapes.medium)
                                            .padding(horizontal = 20.dp),
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        Icon(Icons.Filled.Delete, "Delete", tint = MaterialTheme.colorScheme.onErrorContainer)
                                    }
                                }
                            },
                            enableDismissFromStartToEnd = false,
                            content = {
                                val isDark = com.Nightjar.caliq5.theme.isAppInDarkTheme()
                                Surface(
                                    onClick = { viewModel.showHistoryItemResult(item) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .neuralAmbientGlow(cornerRadius = 18.dp, elevation = 3.dp, tintColor = MaterialTheme.colorScheme.primary, hasSaltMotes = false),
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        androidx.compose.ui.graphics.Brush.verticalGradient(
                                            colors = listOf(
                                                if (isDark) androidx.compose.ui.graphics.Color.White.copy(alpha = 0.18f) else androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.08f),
                                                MaterialTheme.colorScheme.primary.copy(alpha = if (isDark) 0.35f else 0.25f)
                                            )
                                        )
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        val pColor = MaterialTheme.colorScheme.primary

                                        Text(
                                            text = item.title,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = pColor
                                            ),
                                            softWrap = true,
                                            maxLines = 2,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Start,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.End,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = item.date,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 12.sp
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
}



