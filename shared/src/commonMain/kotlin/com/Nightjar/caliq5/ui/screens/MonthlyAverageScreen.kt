package com.Nightjar.caliq5.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.Nightjar.caliq5.ui.components.*
import com.Nightjar.caliq5.viewmodel.AppViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MonthlyAverageScreen(viewModel: AppViewModel) {
    val subjectName by viewModel.monthlySubjectName.collectAsState()
    val grades by viewModel.monthlyGrades.collectAsState()
    
    val gridState = androidx.compose.foundation.lazy.grid.rememberLazyGridState()
    val coroutineScope = rememberCoroutineScope()
    var previousGradesSize by remember { mutableStateOf(grades.size) }



    // Disable default CaliqScreen scroll so we can use LazyColumn for smooth item animations
    CaliqScreen(title = "حاسبة الفصلي", onBack = { viewModel.goBack() }, scrollable = false) {
        val topSafeInset = WindowInsets.statusBars
            .union(WindowInsets.captionBar)
            .union(WindowInsets.displayCutout)
            .asPaddingValues()
            .calculateTopPadding()
        val bottomNavPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        val density = androidx.compose.ui.platform.LocalDensity.current
        val isImeVisible = WindowInsets.ime.getBottom(density) > 0
        val dynamicBottomPadding by androidx.compose.animation.core.animateDpAsState(
            targetValue = if (isImeVisible) 20.dp else (160.dp + bottomNavPadding),
            animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow),
            label = "monthlyBottomPadding"
        )
        
        androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
            state = gridState,
            columns = androidx.compose.foundation.lazy.grid.GridCells.Adaptive(minSize = 220.dp),
            modifier = Modifier.fillMaxSize().imePadding().padding(horizontal = 12.dp),
            contentPadding = PaddingValues(top = topSafeInset + 90.dp, bottom = dynamicBottomPadding, start = 4.dp, end = 4.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                Box(modifier = Modifier.fillMaxWidth().popInOnInitialLoad(0)) {
                    FeatureExplanationCard(
                        icon = "calendar_today",
                        text = "لحساب معدل فصل دراسي واحد (أشهر). أدخل اسم المادة ودرجات الأشهر وسنقوم بجمعها وقسمتها على عددها مع جبر الكسور."
                    )
                }
            }

            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                Box(modifier = Modifier.fillMaxWidth().popInOnInitialLoad(1), contentAlignment = Alignment.Center) {
                    GradeInputField(
                        label = "اسم المادة:",
                        value = subjectName,
                        onValueChange = { viewModel.updateMonthlyField("name", it) },
                        placeholder = "مثال: الرياضيات",
                        showPercent = false,
                        keyboardType = KeyboardType.Text,
                        modifier = Modifier.widthIn(max = 480.dp).fillMaxWidth()
                    )
                }
            }

            val monthNames = listOf("الأول", "الثاني", "الثالث", "الرابع", "الخامس", "السادس", "السابع", "الثامن", "التاسع", "العاشر")

            items(
                count = grades.size, 
                key = { index -> grades[index].id },
                span = { index ->
                    if (grades.size % 2 != 0 && index == grades.lastIndex) {
                        androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan)
                    } else {
                        androidx.compose.foundation.lazy.grid.GridItemSpan(1)
                    }
                }
            ) { index ->
                val gradeItem = grades[index]
                val isTrailingOdd = grades.size % 2 != 0 && index == grades.lastIndex

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .popInOnInitialLoad(index + 2)
                        .animateItem(
                            fadeInSpec = androidx.compose.animation.core.tween(350, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                            fadeOutSpec = androidx.compose.animation.core.tween(250, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                            placementSpec = androidx.compose.animation.core.spring(
                                dampingRatio = androidx.compose.animation.core.Spring.DampingRatioLowBouncy,
                                stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    MonthRowItem(
                        index = index,
                        gradeItem = gradeItem,
                        monthNames = monthNames,
                        totalCount = grades.size,
                        isTrailingOdd = isTrailingOdd,
                        onValueChange = { viewModel.updateMonthGrade(index, it) },
                        onRemove = { viewModel.removeMonthGrade(index) }
                    )
                }
            }

            item(key = "action_buttons", span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .popInOnInitialLoad(grades.size + 2)
                        .animateItem(
                            placementSpec = androidx.compose.animation.core.spring(
                                dampingRatio = androidx.compose.animation.core.Spring.DampingRatioLowBouncy,
                                stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CaliqOutlinedButton(
                            onClick = { 
                                viewModel.addMonthGrade()
                            },
                            modifier = Modifier.actionButtonAdaptive()
                        ) {
                            Icon(Icons.Filled.Add, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("إضافة شهر آخر", style = MaterialTheme.typography.labelLarge)
                        }

                        CaliqPrimaryButton(
                            onClick = { viewModel.calculateMonthlyAverage() },
                            text = "حساب المعدل الفصلي",
                            icon = Icons.Filled.PieChart,
                            modifier = Modifier.actionButtonAdaptive()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthRowItem(
    index: Int,
    gradeItem: com.Nightjar.caliq5.viewmodel.MonthlyGrade,
    monthNames: List<String>,
    totalCount: Int,
    isTrailingOdd: Boolean,
    onValueChange: (String) -> Unit,
    onRemove: () -> Unit
) {
    val labelText = if (index < monthNames.size) "الشهر ${monthNames[index]}:" else "الشهر ${index + 1}:"
    var localGrade by remember { mutableStateOf(gradeItem.grade) }
    LaunchedEffect(gradeItem.grade) {
        if (localGrade != gradeItem.grade) {
            localGrade = gradeItem.grade
        }
    }

    Row(
        modifier = (if (isTrailingOdd) Modifier.widthIn(max = 380.dp).fillMaxWidth() else Modifier.fillMaxWidth()),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        GradeInputField(
            label = labelText,
            value = localGrade,
            placeholder = if (index < monthNames.size) "درجة الشهر ${monthNames[index]}" else "درجة الشهر ${index + 1}",
            showPercent = true,
            onValueChange = { newVal ->
                localGrade = newVal
                onValueChange(newVal)
            },
            modifier = Modifier.weight(1f)
        )
        
        AnimatedVisibility(
            visible = totalCount > 2 && index >= 2,
            enter = fadeIn(androidx.compose.animation.core.tween(250)) + expandHorizontally(androidx.compose.animation.core.spring(dampingRatio = androidx.compose.animation.core.Spring.DampingRatioLowBouncy, stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow)),
            exit = fadeOut(androidx.compose.animation.core.tween(200)) + shrinkHorizontally(androidx.compose.animation.core.spring(dampingRatio = androidx.compose.animation.core.Spring.DampingRatioNoBouncy, stiffness = androidx.compose.animation.core.Spring.StiffnessMedium))
        ) {
            CaliqIconButton(
                onClick = onRemove,
                containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                contentColor = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(Icons.Filled.Delete, contentDescription = "حذف الشهر", modifier = Modifier.size(20.dp))
            }
        }
    }
}
