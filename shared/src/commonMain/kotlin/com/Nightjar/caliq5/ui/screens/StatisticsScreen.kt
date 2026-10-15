package com.Nightjar.caliq5.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.Nightjar.caliq5.ui.components.*

import com.Nightjar.caliq5.viewmodel.AppViewModel

/**
 * Statistics/History Screen.
 * Now features elegant card deletion animations and active focus state beneath the confirmation dialog.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(viewModel: AppViewModel) {
    val history by viewModel.history.collectAsState()
    var itemToDelete by remember { mutableStateOf<String?>(null) }
    var deletingItemId by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    CaliqScreen(title = "السجل", onBack = { viewModel.goBack() }, scrollable = false) {
        if (itemToDelete != null) {
            com.Nightjar.caliq5.ui.components.DeleteConfirmDialog(
                title = "حذف السجل",
                message = "هل أنت متأكد من رغبتك في حذف هذا السجل من الإحصائيات؟",
                onConfirm = {
                    val id = itemToDelete ?: return@DeleteConfirmDialog
                    itemToDelete = null
                    coroutineScope.launch {
                        deletingItemId = id
                        delay(340)
                        viewModel.deleteHistoryItem(id)
                        deletingItemId = null
                    }
                },
                onDismiss = { itemToDelete = null }
            )
        }
        
        androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
            columns = androidx.compose.foundation.lazy.grid.GridCells.Adaptive(minSize = 280.dp),
            contentPadding = PaddingValues(
                bottom = 120.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(), 
                top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 65.dp
            ),
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
                    val isPendingDeletion = itemToDelete == item.id
                    val isBeingDeleted = deletingItemId == item.id

                    val infinitePulse = rememberInfiniteTransition(label = "pulse_${item.id}")
                    val pulseAlpha by infinitePulse.animateFloat(
                        initialValue = 0.35f,
                        targetValue = 0.85f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(650, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "pulse_alpha"
                    )

                    val focusScale by animateFloatAsState(
                        targetValue = if (isPendingDeletion) 0.96f else 1f,
                        animationSpec = spring(dampingRatio = 0.65f, stiffness = 300f),
                        label = "focus_scale"
                    )

                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = {
                            if (it == SwipeToDismissBoxValue.EndToStart) {
                                itemToDelete = item.id
                                false
                            } else false
                        }
                    )

                    AnimatedVisibility(
                        visible = !isBeingDeleted,
                        enter = fadeIn(tween(200)) + expandVertically(tween(250)),
                        exit = shrinkVertically(
                            animationSpec = tween(320, easing = FastOutSlowInEasing),
                            shrinkTowards = Alignment.CenterVertically
                        ) + fadeOut(
                            animationSpec = tween(260, easing = LinearOutSlowInEasing)
                        ) + scaleOut(
                            targetScale = 0.70f,
                            animationSpec = tween(300, easing = FastOutSlowInEasing)
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .popInOnInitialLoad(index + 1)
                                .graphicsLayer {
                                    scaleX = focusScale
                                    scaleY = focusScale
                                }
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
                                        onClick = { 
                                            if (!isPendingDeletion && !isBeingDeleted) {
                                                viewModel.showHistoryItemResult(item) 
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .neuralAmbientGlow(
                                                cornerRadius = 18.dp, 
                                                elevation = if (isPendingDeletion) 6.dp else 3.dp, 
                                                tintColor = if (isPendingDeletion) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary, 
                                                hasSaltMotes = false
                                            ),
                                        shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
                                        color = if (isPendingDeletion) {
                                            MaterialTheme.colorScheme.errorContainer.copy(alpha = if (isDark) 0.35f else 0.18f)
                                        } else {
                                            MaterialTheme.colorScheme.surface
                                        },
                                        border = androidx.compose.foundation.BorderStroke(
                                            width = if (isPendingDeletion) 1.5.dp else 1.dp,
                                            brush = if (isPendingDeletion) {
                                                androidx.compose.ui.graphics.Brush.verticalGradient(
                                                    colors = listOf(
                                                        MaterialTheme.colorScheme.error.copy(alpha = pulseAlpha),
                                                        MaterialTheme.colorScheme.error.copy(alpha = pulseAlpha * 0.5f)
                                                    )
                                                )
                                            } else {
                                                androidx.compose.ui.graphics.Brush.verticalGradient(
                                                    colors = listOf(
                                                        if (isDark) androidx.compose.ui.graphics.Color.White.copy(alpha = 0.18f) else androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.08f),
                                                        MaterialTheme.colorScheme.primary.copy(alpha = if (isDark) 0.35f else 0.25f)
                                                    )
                                                )
                                            }
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(16.dp), 
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            val pColor = if (isPendingDeletion) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary

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
                                                horizontalArrangement = Arrangement.SpaceBetween,
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
                                                IconButton(
                                                    onClick = { itemToDelete = item.id },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.DeleteOutline,
                                                        contentDescription = "حذف السجل",
                                                        tint = if (isPendingDeletion) {
                                                            MaterialTheme.colorScheme.error
                                                        } else {
                                                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                                                        },
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
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
