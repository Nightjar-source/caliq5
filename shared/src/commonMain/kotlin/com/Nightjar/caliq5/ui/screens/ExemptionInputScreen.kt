package com.Nightjar.caliq5.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Nightjar.caliq5.data.ClassData
import com.Nightjar.caliq5.data.SubjectGradeEntry
import com.Nightjar.caliq5.ui.components.*
import com.Nightjar.caliq5.viewmodel.AppViewModel

@Composable
fun ExemptionInputScreen(viewModel: AppViewModel) {
    val selectedClass by viewModel.selectedClass.collectAsState()
    val exemptionGrades by viewModel.exemptionGrades.collectAsState()
    val customSubjects by viewModel.customSubjects.collectAsState()

    var selectedTab by rememberSaveable { mutableStateOf(0) }
    val predefinedSubjects = remember(selectedClass) { ClassData.getSubjects(selectedClass) }

    com.Nightjar.caliq5.utils.BackHandler(enabled = true) { viewModel.goBack() }

    CaliqScreen(
        title = "إدخال الدرجات - $selectedClass",
        onBack = { viewModel.goBack() },
        scrollable = false
    ) {
        val lazyGridState = androidx.compose.foundation.lazy.grid.rememberLazyGridState()
        var isControlVisible by remember { mutableStateOf(true) }

        LaunchedEffect(lazyGridState) {
            var prevIndex = 0
            var prevOffset = 0
            androidx.compose.runtime.snapshotFlow {
                Pair(lazyGridState.firstVisibleItemIndex, lazyGridState.firstVisibleItemScrollOffset)
            }.collect { (currentIndex, currentOffset) ->
                if (currentIndex > prevIndex) {
                    isControlVisible = false
                } else if (currentIndex < prevIndex) {
                    isControlVisible = true
                } else {
                    val delta = currentOffset - prevOffset
                    if (delta > 12 && currentOffset > 30) {
                        isControlVisible = false
                    } else if (delta < -12 || currentOffset <= 10) {
                        isControlVisible = true
                    }
                }
                prevOffset = currentOffset
                prevIndex = currentIndex
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            val bottomNavPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
            
            LazyVerticalGrid(
                state = lazyGridState,
                columns = GridCells.Adaptive(minSize = 290.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(
                    top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 115.dp,
                    bottom = 120.dp + bottomNavPadding
                ),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Explanation Card (Static container, text animates internally)
                item(key = "header_explanation", span = { GridItemSpan(maxLineSpan) }) {
                    Box(modifier = Modifier.fillMaxWidth().popInOnInitialLoad(0)) {
                        FeatureExplanationCard(
                            icon = "school",
                            text = if (selectedTab == 0)
                                "أدخل درجة السعي السنوي النهائية مباشرة لكل مادة."
                            else
                                "أدخل درجات الفصل الأول، نصف السنة، والفصل الثاني وسنستخرج السعي تلقائياً.",
                        )
                    }
                }

                // Predefined Subjects with Staggered Entrance
                itemsIndexed(
                    items = predefinedSubjects,
                    key = { _, it -> "pre_" + it.name }
                ) { index, subject ->
                    val entry = exemptionGrades[subject.name] ?: SubjectGradeEntry(
                        name = subject.name, grade = null, isSkipped = false
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .popInOnInitialLoad(index + 1)
                            .animateItem(
                                fadeInSpec = androidx.compose.animation.core.tween(250),
                                fadeOutSpec = androidx.compose.animation.core.tween(200),
                                placementSpec = spring(
                                    dampingRatio = Spring.DampingRatioLowBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        SubjectInputRow(
                            subjectName = subject.name,
                            isOptional = subject.optional,
                            entry = entry,
                            isDetailedMode = selectedTab == 1,
                            onGradeChange = { viewModel.updateSubjectGrade(subject.name, it) },
                            onDetailChange = { t1, mid, t2 ->
                                viewModel.updateSubjectDetailGrades(subject.name, t1, mid, t2)
                            },
                            onSkipToggle = { viewModel.toggleSkipSubject(subject.name) },
                        )
                    }
                }

                // Custom Subjects with Fluid animateItem and Staggered Entrance
                itemsIndexed(
                    items = customSubjects,
                    key = { _, it -> it.id }
                ) { index, subject ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .popInOnInitialLoad(predefinedSubjects.size + index + 1)
                            .animateItem(
                                fadeInSpec = androidx.compose.animation.core.tween(300),
                                fadeOutSpec = androidx.compose.animation.core.tween(200),
                                placementSpec = spring(
                                    dampingRatio = Spring.DampingRatioLowBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        CustomSubjectRow(
                            index = index,
                            entry = subject,
                            isDetailedMode = selectedTab == 1,
                            onNameChange = { viewModel.updateCustomSubjectName(index, it) },
                            onGradeChange = { viewModel.updateCustomSubjectGrade(index, it) },
                            onDetailChange = { t1, mid, t2 -> viewModel.updateCustomSubjectDetailGrades(index, t1, mid, t2) },
                            onRemove = { viewModel.removeCustomSubject(index) },
                        )
                    }
                }

                // Add Custom Subject Button
                item(key = "add_custom_button", span = { GridItemSpan(maxLineSpan) }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .popInOnInitialLoad(predefinedSubjects.size + customSubjects.size + 1)
                            .animateItem(
                                placementSpec = spring(
                                    dampingRatio = Spring.DampingRatioLowBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        CaliqOutlinedButton(
                            onClick = { viewModel.addCustomSubject() },
                            modifier = Modifier.actionButtonAdaptive(),
                            contentPadding = PaddingValues(vertical = 12.dp, horizontal = 20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("إضافة مادة مخصصة", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }

                // Calculate Exemption Results Button
                item(key = "calculate_button", span = { GridItemSpan(maxLineSpan) }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp, bottom = 12.dp)
                            .popInOnInitialLoad(predefinedSubjects.size + customSubjects.size + 2)
                            .animateItem(
                                placementSpec = spring(
                                    dampingRatio = Spring.DampingRatioLowBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        CaliqPrimaryButton(
                            onClick = { viewModel.calculateExemptionResults() },
                            text = "حساب الاعفاء الفردي والشامل",
                            icon = Icons.Default.Calculate,
                            modifier = Modifier.actionButtonAdaptive()
                        )
                    }
                }
            }

            // Floating Segmented Control at Top (Adapts to wide screens, matches title bar opacity, hides on scroll down)
            androidx.compose.animation.AnimatedVisibility(
                visible = isControlVisible,
                enter = slideInVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                ) { -it } + fadeIn(androidx.compose.animation.core.tween(220)),
                exit = slideOutVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                ) { -it } + fadeOut(androidx.compose.animation.core.tween(180)),
                modifier = Modifier
                    .widthIn(max = 420.dp)
                    .fillMaxWidth()
                    .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 52.dp)
                    .padding(horizontal = 16.dp, vertical = 2.dp)
                    .align(Alignment.TopCenter)
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
                    border = BorderStroke(
                        0.8.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                    ),
                    shadowElevation = 3.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(2.5.dp)) {
                        com.Nightjar.caliq5.ui.components.SlidingSegmentedControl(
                            options = listOf("السعي السنوي" to 0, "إدخال درجات الفصلي" to 1),
                            selectedOption = selectedTab,
                            onOptionSelected = { selectedTab = it },
                            modifier = Modifier.fillMaxWidth(),
                            height = 36.dp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SubjectInputRow(
    subjectName: String,
    isOptional: Boolean,
    entry: SubjectGradeEntry,
    isDetailedMode: Boolean,
    onGradeChange: (String) -> Unit,
    onDetailChange: (String, String, String) -> Unit,
    onSkipToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isSkipped = entry.isSkipped
    val transitionScale by animateFloatAsState(if (isSkipped) 0.98f else 1f)

    val gradeStr = entry.grade?.let { com.Nightjar.caliq5.ui.components.formatDecimalGrade(it) } ?: ""
    var localGrade by remember(isSkipped) { 
        mutableStateOf(if (isSkipped) "لا تشملني" else gradeStr)
    }
    LaunchedEffect(entry.grade, isSkipped) {
        if (isSkipped) {
            localGrade = "لا تشملني"
        } else if (entry.grade == null) {
            if (localGrade.isNotEmpty() && localGrade != "لا تشملني") localGrade = ""
        } else {
            val cur = localGrade.toDoubleOrNull()
            if (cur != entry.grade) {
                localGrade = com.Nightjar.caliq5.ui.components.formatDecimalGrade(entry.grade)
            }
        }
    }

    val isDark = com.Nightjar.caliq5.theme.isAppInDarkTheme()
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val errorColor = com.Nightjar.caliq5.theme.errorColor
    val cardColor = if (isSkipped) {
        MaterialTheme.colorScheme.errorContainer.copy(alpha = if (isDark) 0.18f else 0.12f)
    } else {
        surfaceVariant.copy(alpha = if (isDark) 0.40f else 0.45f)
    }
    val cardBorder = BorderStroke(
        width = 1.dp,
        color = if (isSkipped) errorColor.copy(alpha = 0.35f)
                else MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDark) 0.30f else 0.40f)
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = transitionScale
                scaleY = transitionScale
            }
            .padding(vertical = 4.dp)
            .shadow(
                elevation = 1.5.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = Color.Black.copy(alpha = 0.05f),
                spotColor = Color.Black.copy(alpha = 0.03f)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(cardColor)
            .border(cardBorder, RoundedCornerShape(24.dp))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSkipped) errorColor.copy(alpha = 0.12f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = null,
                                tint = if (isSkipped) errorColor else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Text(
                        text = subjectName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isSkipped) errorColor else MaterialTheme.colorScheme.onSurface
                        )
                    )
                }

                if (isOptional) {
                    com.Nightjar.caliq5.ui.components.CaliqToggleChip(
                        checked = !isSkipped,
                        onCheckedChange = onSkipToggle,
                        modifier = Modifier.defaultMinSize(minHeight = 32.dp, minWidth = 100.dp)
                    ) {
                        AnimatedContent(
                            targetState = !isSkipped,
                            transitionSpec = {
                                (fadeIn(animationSpec = androidx.compose.animation.core.tween(160)) + 
                                 scaleIn(initialScale = 0.90f, animationSpec = spring(dampingRatio = 0.70f, stiffness = 420f)))
                                    .togetherWith(fadeOut(animationSpec = androidx.compose.animation.core.tween(120)) + 
                                                  scaleOut(targetScale = 0.90f, animationSpec = spring(dampingRatio = 0.70f, stiffness = 420f)))
                                    .using(SizeTransform(clip = false))
                            },
                            contentAlignment = Alignment.Center,
                            label = "exemption_chip_content_anim"
                        ) { isIncluded ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (!isIncluded) Icons.Default.Close else Icons.Default.Check,
                                    contentDescription = null,
                                    tint = if (!isIncluded) errorColor else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = if (!isIncluded) "غير مشمولة" else "مشمولة",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (!isIncluded) errorColor else MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                        }
                    }
                }
            }

            AnimatedContent(
                targetState = Pair(isSkipped, isDetailedMode),
                transitionSpec = {
                    (fadeIn(animationSpec = androidx.compose.animation.core.tween(200)) + 
                     expandVertically(animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow))
                    ).togetherWith(
                        fadeOut(animationSpec = androidx.compose.animation.core.tween(150)) +
                        shrinkVertically(animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow))
                    )
                },
                label = "subject_row_input_mode"
            ) { (skipped, detailed) ->
                if (skipped) {
                    Text(
                        text = "هذه المادة غير مشمولة في حساب الإعفاء",
                        style = MaterialTheme.typography.bodyMedium.copy(color = com.Nightjar.caliq5.theme.errorColor, fontWeight = FontWeight.SemiBold),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        textAlign = TextAlign.Center
                    )
                } else if (!detailed) {
                    GradeInputField(
                        value = localGrade,
                        onValueChange = { newVal ->
                            cleanGradeInput(newVal)?.let {
                                localGrade = it
                                onGradeChange(it)
                            }
                        },
                        label = "",
                        placeholder = "درجة السعي",
                        enabled = !isSkipped,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    val initialT1 = entry.term1?.let { com.Nightjar.caliq5.ui.components.formatDecimalGrade(it) } ?: ""
                    val initialMid = entry.midYear?.let { com.Nightjar.caliq5.ui.components.formatDecimalGrade(it) } ?: ""
                    val initialT2 = entry.term2?.let { com.Nightjar.caliq5.ui.components.formatDecimalGrade(it) } ?: ""

                    var t1 by remember { mutableStateOf(initialT1) }
                    var mid by remember { mutableStateOf(initialMid) }
                    var t2 by remember { mutableStateOf(initialT2) }

                    LaunchedEffect(entry.term1) {
                        val cur = t1.toDoubleOrNull()
                        if (entry.term1 == null) {
                            if (t1.isNotEmpty()) t1 = ""
                        } else if (cur != entry.term1) {
                            t1 = com.Nightjar.caliq5.ui.components.formatDecimalGrade(entry.term1)
                        }
                    }
                    LaunchedEffect(entry.midYear) {
                        val cur = mid.toDoubleOrNull()
                        if (entry.midYear == null) {
                            if (mid.isNotEmpty()) mid = ""
                        } else if (cur != entry.midYear) {
                            mid = com.Nightjar.caliq5.ui.components.formatDecimalGrade(entry.midYear)
                        }
                    }
                    LaunchedEffect(entry.term2) {
                        val cur = t2.toDoubleOrNull()
                        if (entry.term2 == null) {
                            if (t2.isNotEmpty()) t2 = ""
                        } else if (cur != entry.term2) {
                            t2 = com.Nightjar.caliq5.ui.components.formatDecimalGrade(entry.term2)
                        }
                    }

                    val saeiStr = entry.grade?.let { com.Nightjar.caliq5.ui.components.formatDecimalGrade(it) } ?: ""

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            SmallGradeInput(
                                value = t1,
                                onValueChange = { v -> 
                                    cleanGradeInput(v)?.let { cleanedVal ->
                                         t1 = cleanedVal
                                        onDetailChange(cleanedVal, mid, t2)
                                    }
                                },
                                placeholder = "ف1",
                                modifier = Modifier.weight(1f),
                                enabled = !isSkipped
                            )
                            SmallGradeInput(
                                value = mid,
                                onValueChange = { v -> 
                                    cleanGradeInput(v)?.let { cleanedVal ->
                                        mid = cleanedVal
                                        onDetailChange(t1, cleanedVal, t2)
                                    }
                                },
                                placeholder = "نصف",
                                modifier = Modifier.weight(1f),
                                enabled = !isSkipped
                            )
                            SmallGradeInput(
                                value = t2,
                                onValueChange = { v -> 
                                    cleanGradeInput(v)?.let { cleanedVal ->
                                        t2 = cleanedVal
                                        onDetailChange(t1, mid, cleanedVal)
                                    }
                                },
                                placeholder = "ف2",
                                modifier = Modifier.weight(1f),
                                enabled = !isSkipped
                            )
                        }

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.20f))
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.Calculate,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "السعي السنوي المستخرج:",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontSize = 14.sp
                                        )
                                    )
                                }
                                Text(
                                    text = if (saeiStr.isNotEmpty()) "$saeiStr%" else "---",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 16.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomSubjectRow(
    index: Int,
    entry: SubjectGradeEntry,
    isDetailedMode: Boolean,
    onNameChange: (String) -> Unit,
    onGradeChange: (String) -> Unit,
    onDetailChange: (String, String, String) -> Unit,
    onRemove: () -> Unit,
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        DeleteConfirmDialog(
            onConfirm = { 
                showDeleteConfirm = false
                onRemove()
            }, 
            onDismiss = { showDeleteConfirm = false }
        )
    }

    val isDark = com.Nightjar.caliq5.theme.isAppInDarkTheme()
    val cardColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isDark) 0.40f else 0.45f)
    val cardBorder = BorderStroke(
        width = 1.dp,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDark) 0.30f else 0.40f)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .shadow(
                elevation = 1.5.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = Color.Black.copy(alpha = 0.05f),
                spotColor = Color.Black.copy(alpha = 0.03f)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(cardColor)
            .border(cardBorder, RoundedCornerShape(24.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    GradeInputField(
                        label = "",
                        value = entry.name,
                        onValueChange = onNameChange,
                        placeholder = "اسم المادة",
                        showPercent = false,
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Text
                    )
                }
                CaliqIconButton(
                    onClick = { showDeleteConfirm = true },
                    containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                    contentColor = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "حذف المادة",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            
            AnimatedContent(
                targetState = isDetailedMode,
                transitionSpec = {
                    (fadeIn(animationSpec = androidx.compose.animation.core.tween(200)) + 
                     expandVertically(animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow))
                    ).togetherWith(
                        fadeOut(animationSpec = androidx.compose.animation.core.tween(150)) +
                        shrinkVertically(animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow))
                    )
                },
                label = "custom_subject_row_input_mode"
            ) { detailed ->
                if (!detailed) {
                    val gradeStr = entry.grade?.let { com.Nightjar.caliq5.ui.components.formatDecimalGrade(it) } ?: ""
                    var localGrade by remember { mutableStateOf(gradeStr) }
                    LaunchedEffect(entry.grade) {
                        if (entry.grade == null) {
                            if (localGrade.isNotEmpty()) localGrade = ""
                        } else {
                            val cur = localGrade.toDoubleOrNull()
                            if (cur != entry.grade) {
                                localGrade = com.Nightjar.caliq5.ui.components.formatDecimalGrade(entry.grade)
                            }
                        }
                    }
                    GradeInputField(
                        value = localGrade,
                        onValueChange = { newVal ->
                            cleanGradeInput(newVal)?.let {
                                localGrade = it
                                onGradeChange(it)
                            }
                        },
                        label = "",
                        placeholder = "درجة السعي",
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    val initialT1 = entry.term1?.let { com.Nightjar.caliq5.ui.components.formatDecimalGrade(it) } ?: ""
                    val initialMid = entry.midYear?.let { com.Nightjar.caliq5.ui.components.formatDecimalGrade(it) } ?: ""
                    val initialT2 = entry.term2?.let { com.Nightjar.caliq5.ui.components.formatDecimalGrade(it) } ?: ""

                    var t1 by remember { mutableStateOf(initialT1) }
                    var mid by remember { mutableStateOf(initialMid) }
                    var t2 by remember { mutableStateOf(initialT2) }

                    LaunchedEffect(entry.term1) {
                        val cur = t1.toDoubleOrNull()
                        if (entry.term1 == null) {
                            if (t1.isNotEmpty()) t1 = ""
                        } else if (cur != entry.term1) {
                            t1 = com.Nightjar.caliq5.ui.components.formatDecimalGrade(entry.term1)
                        }
                    }
                    LaunchedEffect(entry.midYear) {
                        val cur = mid.toDoubleOrNull()
                        if (entry.midYear == null) {
                            if (mid.isNotEmpty()) mid = ""
                        } else if (cur != entry.midYear) {
                            mid = com.Nightjar.caliq5.ui.components.formatDecimalGrade(entry.midYear)
                        }
                    }
                    LaunchedEffect(entry.term2) {
                        val cur = t2.toDoubleOrNull()
                        if (entry.term2 == null) {
                            if (t2.isNotEmpty()) t2 = ""
                        } else if (cur != entry.term2) {
                            t2 = com.Nightjar.caliq5.ui.components.formatDecimalGrade(entry.term2)
                        }
                    }

                    val saeiStr = entry.grade?.let { com.Nightjar.caliq5.ui.components.formatDecimalGrade(it) } ?: ""

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            SmallGradeInput(
                                value = t1,
                                onValueChange = { v -> 
                                    cleanGradeInput(v)?.let { cleanedVal ->
                                        t1 = cleanedVal
                                        onDetailChange(cleanedVal, mid, t2)
                                    }
                                },
                                placeholder = "ف1",
                                modifier = Modifier.weight(1f)
                            )
                            SmallGradeInput(
                                value = mid,
                                onValueChange = { v -> 
                                    cleanGradeInput(v)?.let { cleanedVal ->
                                        mid = cleanedVal
                                        onDetailChange(t1, cleanedVal, t2)
                                    }
                                },
                                placeholder = "نصف",
                                modifier = Modifier.weight(1f)
                            )
                            SmallGradeInput(
                                value = t2,
                                onValueChange = { v -> 
                                    cleanGradeInput(v)?.let { cleanedVal ->
                                        t2 = cleanedVal
                                        onDetailChange(t1, mid, cleanedVal)
                                    }
                                },
                                placeholder = "ف2",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.20f))
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.Calculate,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "السعي السنوي المستخرج:",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontSize = 14.sp
                                        )
                                    )
                                }
                                Text(
                                    text = if (saeiStr.isNotEmpty()) "$saeiStr%" else "---",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 16.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}