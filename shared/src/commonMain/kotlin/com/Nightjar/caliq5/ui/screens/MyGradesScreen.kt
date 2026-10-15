package com.Nightjar.caliq5.ui.screens

import kotlinx.coroutines.launch

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Nightjar.caliq5.data.*
import com.Nightjar.caliq5.theme.contrastingTextColor
import com.Nightjar.caliq5.ui.components.*
import com.Nightjar.caliq5.viewmodel.AppViewModel
import com.Nightjar.caliq5.viewmodel.ResultDisplayState

@kotlinx.serialization.Serializable
data class SubjectEntry(val id: String, var name: String, var record: SubjectGradeRecord)

@kotlinx.serialization.Serializable
data class StudentEditDraft(
    val studentId: String? = null,
    val name: String = "",
    val className: String = "",
    val subjects: List<SubjectEntry> = emptyList()
)

@Composable
fun ExpressiveActionButton(
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tintColor: Color,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val isInteractionPressed by rememberExpressivePressState(interactionSource)
    val isInstantPressed = remember { mutableStateOf(false) }
    val isPressed = isInteractionPressed || isInstantPressed.value
    
    val cornerRadius by animateDpAsState(
        targetValue = if (isPressed) 6.dp else 24.dp,
        animationSpec = spring(
            dampingRatio = 0.55f,
            stiffness = 450f
        ),
        label = "action_btn_corner_morph"
    )

    val buttonScale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1.0f,
        animationSpec = spring(
            dampingRatio = 0.55f,
            stiffness = 450f
        ),
        label = "action_btn_scale"
    )
    
    val shape = RoundedCornerShape(cornerRadius)
    val isDark = com.Nightjar.caliq5.theme.isAppInDarkTheme()

    val containerAlpha = if (isPressed) {
        if (isDark) 0.30f else 0.22f
    } else {
        if (isDark) 0.16f else 0.10f
    }
    
    val borderAlpha = if (isPressed) {
        if (isDark) 0.95f else 0.90f
    } else {
        if (isDark) 0.70f else 0.60f
    }

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        shape = shape,
        color = tintColor.copy(alpha = containerAlpha),
        border = BorderStroke(
            width = 1.2.dp,
            color = tintColor.copy(alpha = borderAlpha)
        ),
        modifier = modifier
            .instantExpressivePress { isInstantPressed.value = it }
            .graphicsLayer {
                scaleX = buttonScale
                scaleY = buttonScale
            }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tintColor,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = tintColor
            )
        }
    }
}

@Composable
fun MyGradesScreen(viewModel: AppViewModel) {
    val students by viewModel.students.collectAsState()
    val listGridState = androidx.compose.foundation.lazy.grid.rememberLazyGridState()
    var editingStudentId by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf<String?>(null) }
    var showEditView by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(false) }
    val editingStudent = remember(editingStudentId, students) {
        if (editingStudentId != null) students.find { it.id == editingStudentId } else null
    }

    LaunchedEffect(showEditView, editingStudentId) {
        if (showEditView) {
            if (editingStudentId != null) {
                viewModel.setEditingStudentInMyGrades("edit")
            } else {
                viewModel.setEditingStudentInMyGrades("add")
            }
        } else {
            viewModel.setEditingStudentInMyGrades(null)
        }
    }

    androidx.compose.animation.AnimatedContent(
        targetState = showEditView,
        transitionSpec = {
            val fluidSpringFloat = androidx.compose.animation.core.spring<Float>(
                dampingRatio = 0.76f, // رطب وسلس Fluid
                stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow
            )
            val fluidSpringOffset = androidx.compose.animation.core.spring<androidx.compose.ui.unit.IntOffset>(
                dampingRatio = 0.76f,
                stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow
            )
            (
                androidx.compose.animation.fadeIn(
                    animationSpec = androidx.compose.animation.core.tween(280, easing = androidx.compose.animation.core.FastOutSlowInEasing)
                ) +
                androidx.compose.animation.slideInVertically(animationSpec = fluidSpringOffset) { it / 6 } +
                androidx.compose.animation.scaleIn(
                    initialScale = 0.96f,
                    animationSpec = fluidSpringFloat
                )
            ).togetherWith(
                androidx.compose.animation.fadeOut(
                    animationSpec = androidx.compose.animation.core.tween(200, easing = androidx.compose.animation.core.FastOutLinearInEasing)
                ) +
                androidx.compose.animation.slideOutVertically(animationSpec = fluidSpringOffset) { -it / 6 } +
                androidx.compose.animation.scaleOut(
                    targetScale = 1.02f,
                    animationSpec = fluidSpringFloat
                )
            ).using(androidx.compose.animation.SizeTransform(clip = false))
        },
        label = "EditViewTransition"
    ) { isEditing ->
        if (isEditing) {
            StudentEditView(
                viewModel = viewModel,
                student = editingStudent,
                onSave = { studentRec, hasEmptyGrades ->
                    viewModel.saveStudent(studentRec)
                    showEditView = false
                    editingStudentId = null
                    if (hasEmptyGrades) {
                        viewModel.showAlert("تم الحفظ بنجاح (يرجى استكمال إدخال الدرجات الفارغة لاحقاً)", isError = true)
                    } else {
                        viewModel.showAlert("تم حفظ السجل بنجاح!")
                    }
                },
                onCancel = {
                    showEditView = false
                    editingStudentId = null
                },
                onDelete = {
                    viewModel.deleteStudent(it)
                    showEditView = false
                    editingStudentId = null
                    viewModel.showAlert("تم حذف السجل بنجاح!")
                },
                onShowError = { viewModel.showAlert(it, isError = true) },
                onShowMessage = { viewModel.showAlert(it) }
            )
        } else {
        CaliqScreen(title = "درجاتي (السجل الشامل)", onBack = { viewModel.goBack() }, scrollable = false) {
            androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                state = listGridState,
                columns = androidx.compose.foundation.lazy.grid.GridCells.Adaptive(minSize = 260.dp),
                modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                contentPadding = PaddingValues(top = WindowInsets.statusBars.add(WindowInsets.captionBar).asPaddingValues().calculateTopPadding() + 65.dp, bottom = 120.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                    Box(modifier = Modifier.fillMaxWidth().popInOnInitialLoad(0)) {
                        FeatureExplanationCard(
                            icon = "person_add",
                            text = "1. اضغط \"إضافة سجل صف / طالب\" وأدخل الاسم والصف.\n2. اختر نوع الإدخال (فصلي أم شهريات) لكل فصل.\n3. إذا كان الطالب معفى، سيتم قفل حقل الامتحان النهائي تلقائياً وتلوينه بالأخضر.\n4. اضغط 📄 لإنشاء ملف PDF أنيق!",
                        )
                    }
                }

                if (students.isEmpty()) {
                    item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                        Box(modifier = Modifier.fillMaxWidth().popInOnInitialLoad(1)) {
                            Text(
                                text = "لا توجد سجلات. قم بإضافة سجل درجات لتبدأ.",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                ),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                            )
                        }
                    }
                } else {
                    itemsIndexed(students, key = { _, it -> it.id }) { index, student ->
                        val isDark = com.Nightjar.caliq5.theme.isAppInDarkTheme()
                        val cardShape = RoundedCornerShape(24.dp)
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .popInOnInitialLoad(index + 1)
                                .padding(vertical = 4.dp),
                            shape = cardShape,
                            color = if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.40f) else MaterialTheme.colorScheme.surface,
                            shadowElevation = 0.dp,
                            border = BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDark) 0.30f else 0.45f)
                            )
                        ) {

                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    ExpressiveActionButton(
                                        onClick = { 
                                            val latest = students.find { it.id == student.id } ?: student
                                            viewModel.showResult(ResultDisplayState.StudentProfileResult("درجات ${latest.name}", latest)) 
                                        },
                                        icon = Icons.Outlined.Visibility,
                                        label = "معاينة",
                                        tintColor = MaterialTheme.colorScheme.secondary
                                    )
                                    
                                    ExpressiveActionButton(
                                        onClick = { 
                                            val latest = students.find { it.id == student.id } ?: student
                                            viewModel.exportToPdf(ResultDisplayState.StudentProfileResult("درجات ${latest.name}", latest)) 
                                        },
                                        icon = Icons.Outlined.PictureAsPdf,
                                        label = "طباعة",
                                        tintColor = com.Nightjar.caliq5.theme.successColor
                                    )
                                    
                                    ExpressiveActionButton(
                                        onClick = { editingStudentId = student.id; showEditView = true },
                                        icon = Icons.Outlined.Edit,
                                        label = "تعديل",
                                        tintColor = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable(
                                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                            indication = null
                                        ) { editingStudentId = student.id; showEditView = true },
                                    horizontalAlignment = Alignment.End
                                ) {
                                    Text(
                                        text = student.name,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold,
                                        ),
                                        textAlign = TextAlign.End,
                                    )
                                    Text(
                                        text = student.className,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MaterialTheme.colorScheme.secondary,
                                        ),
                                        textAlign = TextAlign.End,
                                    )
                                }
                            }
                        }
                    }
                }

                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
                        CaliqButton(
                            onClick = { editingStudentId = null; showEditView = true },
                            modifier = Modifier.actionButtonAdaptive(),
                            contentPadding = PaddingValues(14.dp)
                        ) {
                            Icon(Icons.Default.PersonAdd, null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("إضافة سجل صف / طالب", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentEditView(
    viewModel: AppViewModel,
    student: StudentRecord?,
    onSave: (StudentRecord, Boolean) -> Unit,
    onCancel: () -> Unit,
    onDelete: (String) -> Unit,
    onShowError: (String) -> Unit,
    onShowMessage: (String) -> Unit
) {
    var name by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(student?.name ?: "") }
    var selectedClassName by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(student?.className ?: "") }
    var showDeleteConfirm by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(false) }

    val json = remember { kotlinx.serialization.json.Json { ignoreUnknownKeys = true; encodeDefaults = true } }
    var savedSubjectsJson by androidx.compose.runtime.saveable.rememberSaveable {
        mutableStateOf(
            if (student != null) {
                json.encodeToString(student.subjects.map { (k, v) -> SubjectEntry("sub_${student.className}_$k", k, v) })
            } else ""
        )
    }

    val subjectsList = remember {
        androidx.compose.runtime.mutableStateListOf<SubjectEntry>().apply {
            if (savedSubjectsJson.isNotEmpty()) {
                try {
                    val decoded = json.decodeFromString<List<SubjectEntry>>(savedSubjectsJson)
                    addAll(decoded)
                } catch (e: Exception) {
                    student?.subjects?.forEach { (k, v) -> add(SubjectEntry(id = "sub_${student.className}_$k", name = k, record = v)) }
                }
            } else {
                student?.subjects?.forEach { (k, v) -> add(SubjectEntry(id = "sub_${student.className}_$k", name = k, record = v)) }
            }
        }
    }

    val subjectsMap = subjectsList.associate { it.name to it.record }
    
    val coroutineScope = rememberCoroutineScope()
    val classList = ClassData.classNames + "صف آخر (مخصص)"
    var expandedClassSelect by remember { mutableStateOf(false) }

    fun clearPersistentDraft() {
        coroutineScope.launch {
            com.Nightjar.caliq5.viewmodel.DataRepository.savePreference("student_edit_draft_v2", "")
        }
    }

    LaunchedEffect(Unit) {
        if (student == null) {
            val raw = com.Nightjar.caliq5.viewmodel.DataRepository.loadPreference("student_edit_draft_v2")
            if (!raw.isNullOrEmpty()) {
                try {
                    val draft = json.decodeFromString<StudentEditDraft>(raw)
                    if (name.isEmpty() && draft.name.isNotEmpty()) name = draft.name
                    if (selectedClassName.isEmpty() && draft.className.isNotEmpty()) selectedClassName = draft.className
                    if (subjectsList.isEmpty() && draft.subjects.isNotEmpty()) {
                        subjectsList.addAll(draft.subjects)
                    }
                } catch (e: Exception) {}
            }
        }
    }

    var hasUnsavedChanges by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(name, selectedClassName, subjectsMap.toMap()) {
        if (student == null) {
            hasUnsavedChanges = name.isNotEmpty() || selectedClassName.isNotEmpty() || subjectsMap.isNotEmpty()
        } else {
            val originalSubjects = student.subjects
            hasUnsavedChanges = name != student.name || selectedClassName != student.className || subjectsMap.toMap() != originalSubjects
        }
        if (hasUnsavedChanges) {
            kotlinx.coroutines.delay(400) // Debounce auto-draft writing to eliminate UI stutter
            val draft = StudentEditDraft(
                studentId = student?.id,
                name = name,
                className = selectedClassName,
                subjects = subjectsList.toList()
            )
            com.Nightjar.caliq5.viewmodel.DataRepository.savePreference("student_edit_draft_v2", json.encodeToString(draft))
        }
    }

    var showUnsavedDialog by remember { mutableStateOf(false) }

    // Listen to Top App Bar back button clicks from viewModel
    LaunchedEffect(Unit) {
        viewModel.myGradesBackEvents.collect {
            if (hasUnsavedChanges) {
                showUnsavedDialog = true
            } else {
                clearPersistentDraft()
                onCancel()
            }
        }
    }

    com.Nightjar.caliq5.utils.BackHandler(enabled = true) {
        if (expandedClassSelect) {
            expandedClassSelect = false
        } else if (hasUnsavedChanges) {
            showUnsavedDialog = true
        } else {
            clearPersistentDraft()
            onCancel()
        }
    }

    if (showUnsavedDialog) {
        CaliqConfirmationDialog(
            title = "تغييرات غير محفوظة",
            message = "هل أنت متأكد من الرجوع بدون حفظ الإدخالات الجديدة في السجل؟",
            confirmText = "حفظ الآن",
            dismissText = "الرجوع على أي حال",
            isDestructive = false,
            onConfirm = {
                showUnsavedDialog = false
                if (name.isBlank() || selectedClassName.isEmpty()) {
                    onShowError("الرجاء إدخال اسم الطالب واختيار الصف أولاً")
                } else {
                    val hasEmptyGrades = subjectsList.any { sub ->
                        if (sub.record.t1Val == "SKIPPED") false
                        else {
                            val t1Done = if (sub.record.t1Mode == TermMode.TERM) sub.record.t1Val.isNotBlank() else sub.record.t1Months.any { it.isNotBlank() }
                            val midDone = sub.record.midVal.isNotBlank()
                            val t2Done = if (sub.record.t2Mode == TermMode.TERM) sub.record.t2Val.isNotBlank() else sub.record.t2Months.any { it.isNotBlank() }
                            !t1Done || !midDone || !t2Done
                        }
                    }
                    clearPersistentDraft()
                    onSave(StudentRecord(student?.id ?: "st_${System.currentTimeMillis()}", name, selectedClassName, subjectsList.associate { it.name to it.record }), hasEmptyGrades)
                }
            },
            onDismiss = {
                showUnsavedDialog = false
                clearPersistentDraft()
                onCancel()
            }
        )
    }

    val isCustomClass = selectedClassName == "صف آخر (مخصص)"

    val isAllSubjectsEntered by remember {
        derivedStateOf {
            val nonSkipped = subjectsList.filter { sub ->
                val isOptional = !isCustomClass && (com.Nightjar.caliq5.data.ClassData.getSubjects(selectedClassName).find { it.name == sub.name }?.optional == true)
                !(isOptional && sub.record.t1Val == "SKIPPED")
            }
            nonSkipped.isNotEmpty() && nonSkipped.all { sub ->
                val r = sub.record
                val t1 = if (r.t1Mode == TermMode.TERM) r.t1Val.parseGradeToDouble() else {
                    val gr = r.t1Months.mapNotNull { it.parseGradeToDouble() }
                    if (gr.isNotEmpty()) gr.average() else null
                }
                val mid = r.midVal.parseGradeToDouble()
                val t2 = if (r.t2Mode == TermMode.TERM) r.t2Val.parseGradeToDouble() else {
                    val gr = r.t2Months.mapNotNull { it.parseGradeToDouble() }
                    if (gr.isNotEmpty()) gr.average() else null
                }
                t1 != null && mid != null && t2 != null
            }
        }
    }

    val genExemptData by remember {
        derivedStateOf {
            val nonSkipped = subjectsList.filter { sub ->
                val isOptional = !isCustomClass && (com.Nightjar.caliq5.data.ClassData.getSubjects(selectedClassName).find { it.name == sub.name }?.optional == true)
                !(isOptional && sub.record.t1Val == "SKIPPED")
            }
            if (!isAllSubjectsEntered || nonSkipped.isEmpty()) return@derivedStateOf Pair(false, 0.0)
            var totalSaei = 0.0
            var countSaei = 0
            var hasBelow75 = false
            nonSkipped.forEach { sub ->
                val r = sub.record
                val t1 = if (r.t1Mode == TermMode.TERM) r.t1Val.parseGradeToDouble() else {
                    val gr = r.t1Months.mapNotNull { it.parseGradeToDouble() }
                    if (gr.isNotEmpty()) gr.average() else null
                }
                val mid = r.midVal.parseGradeToDouble()
                val t2 = if (r.t2Mode == TermMode.TERM) r.t2Val.parseGradeToDouble() else {
                    val gr = r.t2Months.mapNotNull { it.parseGradeToDouble() }
                    if (gr.isNotEmpty()) gr.average() else null
                }
                if (t1 != null && mid != null && t2 != null) {
                    val num = (t1 + mid + t2) / 3.0
                    val fl = kotlin.math.floor(num)
                    val dec = num - fl
                    val saei = when {
                        dec >= 0.6 -> kotlin.math.ceil(num)
                        num >= 49.6 && num < 50.0 -> 50.0
                        num >= 74.6 && num < 75.0 -> 75.0
                        num >= 89.6 && num < 90.0 -> 90.0
                        else -> num
                    }
                    totalSaei += saei
                    countSaei++
                    if (saei < 75.0) {
                        hasBelow75 = true
                    }
                }
            }
            val overallAverage = if (countSaei > 0) totalSaei / countSaei else 0.0
            val isExempt = countSaei > 0 && countSaei == nonSkipped.size && overallAverage >= 85.0 && !hasBelow75
            Pair(isExempt, overallAverage)
        }
    }
    val isGenerallyExempt = genExemptData.first
    val generalAverage = genExemptData.second

    LaunchedEffect(isCustomClass) {
        if (isCustomClass && subjectsList.isEmpty()) {
            subjectsList.add(SubjectEntry(kotlin.random.Random.nextInt().toString(), "المادة 1", SubjectGradeRecord(TermMode.TERM, "", emptyList(), "", TermMode.TERM, "", emptyList(), "")))
        }
    }
    val predefinedSubjects = remember(selectedClassName, isCustomClass) {
        if (isCustomClass) emptyList() else com.Nightjar.caliq5.data.ClassData.getSubjects(selectedClassName).map { it.name }
    }

    val editGridState = androidx.compose.foundation.lazy.grid.rememberLazyGridState()

    CaliqScreen(title = if (student == null) "إضافة سجل درجاتي (السجل الشامل)" else "تعديل سجل درجاتي (السجل الشامل)", onBack = { if (hasUnsavedChanges) showUnsavedDialog = true else onCancel() }, scrollable = false) {
        Box(modifier = Modifier.fillMaxSize()) {
            androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
            state = editGridState,
            columns = androidx.compose.foundation.lazy.grid.GridCells.Adaptive(minSize = 260.dp),
            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
            contentPadding = PaddingValues(top = WindowInsets.statusBars.add(WindowInsets.captionBar).asPaddingValues().calculateTopPadding() + 65.dp, bottom = 120.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item(key = "student_info_header", span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .popInOnInitialLoad(0),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .widthIn(max = 760.dp)
                            .fillMaxWidth()
                            .shadow(
                                elevation = 6.dp,
                                shape = RoundedCornerShape(16.dp),
                                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            )
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                                    )
                                )
                            )
                    ) {
            Column(
modifier = Modifier.padding(16.dp).fillMaxWidth(),
verticalArrangement = Arrangement.spacedBy(16.dp)
) {
Box(modifier = Modifier.fillMaxWidth()) {
                    GradeInputField(
                        label = "اسم السجل / الطالب:",
                        value = name,
                        onValueChange = { name = it },
                        placeholder = "الاسم",
                        showPercent = false,
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Text
                    )
                }
                
                Box(modifier = Modifier.fillMaxWidth()) {
                    val arrowAngle by androidx.compose.animation.core.animateFloatAsState(
                        targetValue = if (expandedClassSelect) 180f else 0f,
                        animationSpec = androidx.compose.animation.core.spring(
                            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioLowBouncy,
                            stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow
                        ),
                        label = "class_dropdown_arrow"
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (com.Nightjar.caliq5.theme.isAppInDarkTheme()) 0.45f else 0.55f))
                            .border(
                                width = 1.dp,
                                color = if (selectedClassName.isNotEmpty()) MaterialTheme.colorScheme.primary.copy(alpha = 0.50f)
                                        else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                                shape = RoundedCornerShape(18.dp)
                            )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expandedClassSelect = !expandedClassSelect }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (selectedClassName.isNotEmpty()) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.School,
                                            contentDescription = null,
                                            tint = if (selectedClassName.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Column {
                                    Text(
                                        text = "الصف الدراسي",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                    val placeholderColor = if (com.Nightjar.caliq5.theme.isAppInDarkTheme()) Color.White.copy(alpha = 0.85f) else Color(0xFF334155)
                                    Text(
                                        text = if (selectedClassName.isNotEmpty()) selectedClassName else "اضغط هنا لاختيار الصف الدراسي",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (selectedClassName.isNotEmpty()) FontWeight.Bold else FontWeight.SemiBold,
                                            color = if (selectedClassName.isNotEmpty()) MaterialTheme.colorScheme.onSurface else placeholderColor
                                        )
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.rotate(arrowAngle).size(26.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Connected Accordion Dropdown
                        androidx.compose.animation.AnimatedVisibility(
                            visible = expandedClassSelect,
                            enter = androidx.compose.animation.expandVertically(
                                animationSpec = androidx.compose.animation.core.spring(
                                    dampingRatio = 0.85f,
                                    stiffness = 450f
                                )
                            ) + androidx.compose.animation.fadeIn(
                                animationSpec = androidx.compose.animation.core.tween(150)
                            ),
                            exit = androidx.compose.animation.shrinkVertically(
                                animationSpec = androidx.compose.animation.core.spring(
                                    dampingRatio = 0.90f,
                                    stiffness = 500f
                                )
                            ) + androidx.compose.animation.fadeOut(
                                animationSpec = androidx.compose.animation.core.tween(120)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                androidx.compose.material3.HorizontalDivider(
                                    modifier = Modifier.padding(bottom = 4.dp),
                                    thickness = 0.8.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                                )
                                classList.forEach { cls ->
                                    val isSelectedClass = cls == selectedClassName
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(14.dp))
                                            .clickable {
                                                expandedClassSelect = false
                                                if (selectedClassName != cls) {
                                                    selectedClassName = cls
                                                    val newSubjects = if (cls == "صف آخر (مخصص)") listOf(com.Nightjar.caliq5.data.Subject("المادة 1", false)) else com.Nightjar.caliq5.data.ClassData.getSubjects(cls)
                                                    val newEntries = newSubjects.map { sub ->
                                                        val existing = subjectsList.find { it.name == sub.name }?.record
                                                        SubjectEntry(id = "sub_${cls}_${sub.name}", name = sub.name, record = existing ?: SubjectGradeRecord(TermMode.TERM, "", emptyList(), "", TermMode.TERM, "", emptyList(), ""))
                                                    }
                                                    subjectsList.clear()
                                                    subjectsList.addAll(newEntries)
                                                }
                                            },
                                        shape = RoundedCornerShape(14.dp),
                                        color = if (isSelectedClass) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.surface.copy(alpha = if (com.Nightjar.caliq5.theme.isAppInDarkTheme()) 0.50f else 0.70f),
                                        border = if (isSelectedClass) null
                                                else BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.40f))
                                    ) {
                                        val isDark = com.Nightjar.caliq5.theme.isAppInDarkTheme()
                                        val itemBg = if (isSelectedClass) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.surface.copy(alpha = if (isDark) 0.50f else 0.70f)
                                        val itemTextColor = if (isSelectedClass) {
                                            MaterialTheme.colorScheme.primary.contrastingTextColor(darkText = Color(0xFF0F172A), lightText = Color.White)
                                        } else {
                                            if (isDark) Color.White else Color(0xFF0F172A)
                                        }

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 14.dp, vertical = 11.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = cls,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = if (isSelectedClass) FontWeight.Bold else FontWeight.Medium
                                                ),
                                                color = itemTextColor
                                            )
                                            if (isSelectedClass) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = itemTextColor,
                                                    modifier = Modifier.size(18.dp)
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

        if (selectedClassName.isNotEmpty()) {
            item(key = "general_exemption_card", span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = isGenerallyExempt,
                    enter = androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(300)) + 
                            androidx.compose.animation.expandVertically(
                                animationSpec = androidx.compose.animation.core.spring(
                                    dampingRatio = androidx.compose.animation.core.Spring.DampingRatioLowBouncy,
                                    stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow
                                )
                            ) +
                            androidx.compose.animation.slideInVertically(
                                animationSpec = androidx.compose.animation.core.spring(
                                    dampingRatio = androidx.compose.animation.core.Spring.DampingRatioLowBouncy,
                                    stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow
                                )
                            ) { -it / 2 } +
                            androidx.compose.animation.scaleIn(
                                initialScale = 0.94f,
                                animationSpec = androidx.compose.animation.core.spring(
                                    dampingRatio = androidx.compose.animation.core.Spring.DampingRatioLowBouncy,
                                    stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow
                                )
                            ),
                    exit = androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(200)) + 
                           androidx.compose.animation.shrinkVertically(
                               animationSpec = androidx.compose.animation.core.spring(
                                   dampingRatio = androidx.compose.animation.core.Spring.DampingRatioNoBouncy,
                                   stiffness = androidx.compose.animation.core.Spring.StiffnessMedium
                                )
                           ) +
                           androidx.compose.animation.slideOutVertically(
                               animationSpec = androidx.compose.animation.core.spring(
                                   dampingRatio = androidx.compose.animation.core.Spring.DampingRatioNoBouncy,
                                   stiffness = androidx.compose.animation.core.Spring.StiffnessMedium
                               )
                           ) { -it / 2 } +
                           androidx.compose.animation.scaleOut(
                               targetScale = 0.94f,
                               animationSpec = androidx.compose.animation.core.tween(200)
                           )
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .animateEnterExit()
                            .animateContentSize(
                                animationSpec = androidx.compose.animation.core.spring(
                                    dampingRatio = androidx.compose.animation.core.Spring.DampingRatioLowBouncy,
                                    stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow
                                )
                            ),
                        color = com.Nightjar.caliq5.theme.successColor.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = com.Nightjar.caliq5.theme.successColor.copy(alpha = 0.22f),
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Filled.Star,
                                        contentDescription = null,
                                        tint = com.Nightjar.caliq5.theme.successColor,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                            }
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    "مبروك ✨ 🎉 الطالب معفى إعفاء عام في جميع الدروس لهذا العام ونجحت الى صفك التالي..",
                                    fontWeight = FontWeight.Bold,
                                    color = com.Nightjar.caliq5.theme.successColor,
                                    fontSize = 15.5.sp,
                                    lineHeight = 22.sp
                                )
                                Text(
                                    "تم تحقيق شروط الإعفاء العام لجميع المواد بمعدل سعي ${formatDecimalGrade(generalAverage)}% وتغلق مواد النهائي تلقائياً.",
                                    color = com.Nightjar.caliq5.theme.successColor.copy(alpha = 0.92f),
                                    fontSize = 12.5.sp,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            }

            item(key = "subjects_section_header", span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Surface(
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            "درجات المواد",
                            style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

                val sortedEntries = if (isCustomClass) subjectsList.toList() else subjectsList.sortedBy { if (predefinedSubjects.contains(it.name)) predefinedSubjects.indexOf(it.name) else 999 }
                
                itemsIndexed(items = sortedEntries, key = { _, it -> it.id }) { index, entry ->
                    val isSubjectCustom = isCustomClass || !predefinedSubjects.contains(entry.name)
                    val isOptional = !isCustomClass && (com.Nightjar.caliq5.data.ClassData.getSubjects(selectedClassName).find { it.name == entry.name }?.optional == true)
                    val isSkipped = isOptional && entry.record.t1Val == "SKIPPED"
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .popInOnInitialLoad(index + 1)
                            .animateItem(
                                fadeInSpec = androidx.compose.animation.core.tween(350, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                                fadeOutSpec = androidx.compose.animation.core.tween(250, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                                placementSpec = androidx.compose.animation.core.spring(
                                    dampingRatio = androidx.compose.animation.core.Spring.DampingRatioLowBouncy,
                                    stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow
                                )
                            )
                    ) {

                        SubjectGradeEditCard(
                            name = entry.name,
                            record = entry.record,
                            isGenerallyExempt = isGenerallyExempt,
                            isAllSubjectsEntered = isAllSubjectsEntered,
                            isCustom = isSubjectCustom,
                            isOptional = isOptional,
                            isSkipped = isSkipped,
                            onSkipToggle = {
                                val newIndex = subjectsList.indexOfFirst { it.id == entry.id }
                                if (newIndex != -1) {
                                    val newSaei = if (isSkipped) "" else "SKIPPED"
                                    subjectsList[newIndex] = subjectsList[newIndex].copy(record = entry.record.copy(t1Val = newSaei))
                                }
                            },
                            onShowMessage = onShowMessage,
                            onRename = { newName ->
                                val index = subjectsList.indexOfFirst { it.id == entry.id }
                                if (index != -1) {
                                    subjectsList[index] = subjectsList[index].copy(name = newName)
                                }
                            },
                            onDelete = { 
                                subjectsList.removeAll { it.id == entry.id }
                                onShowMessage("تم الحذف بنجاح")
                            },
                            onUpdate = { newRecord ->
                                val index = subjectsList.indexOfFirst { it.id == entry.id }
                                if (index != -1) {
                                    subjectsList[index] = subjectsList[index].copy(record = newRecord)
                                }
                            }
                        )
                    }
                }
                
                item(key = "add_custom_btn", span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .popInOnInitialLoad(subjectsList.size + 1)
                            .animateItem(
                                placementSpec = androidx.compose.animation.core.spring(
                                    dampingRatio = androidx.compose.animation.core.Spring.DampingRatioLowBouncy,
                                    stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        CaliqOutlinedButton(
                            onClick = { 
                                val newId = if (isCustomClass) "المادة ${subjectsList.size + 1}" else "مادة مضافة ${subjectsList.size + 1}"
                                subjectsList.add(SubjectEntry(kotlin.random.Random.nextInt().toString(), newId, SubjectGradeRecord(TermMode.TERM, "", emptyList(), "", TermMode.TERM, "", emptyList(), "")))
                            },
                            modifier = Modifier
                                .actionButtonAdaptive()
                                .padding(top = 16.dp)
                        ) {
                            Icon(Icons.Filled.Add, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(if (isCustomClass) "إضافة مادة أخرى" else "إضافة مادة مخصصة", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            } // closes if (selectedClassName.isNotEmpty())

        item(key = "spacer_32", span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .popInOnInitialLoad(1)
                    .animateItem(
                        placementSpec = androidx.compose.animation.core.spring(
                            dampingRatio = 0.76f,
                            stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow
                        )
                    )
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier.widthIn(max = 540.dp).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))
                        @OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
                        androidx.compose.foundation.layout.FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CaliqOutlinedButton(
                            onClick = { if (hasUnsavedChanges) showUnsavedDialog = true else onCancel() },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(14.dp)
                        ) {
                            Text("إلغاء")
                        }

                        if (student != null) {
                            CaliqButton(
                                onClick = { showDeleteConfirm = true },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                contentPadding = PaddingValues(14.dp)
                            ) {
                                Icon(Icons.Default.Delete, null)
                                Spacer(Modifier.width(8.dp))
                                Text("حذف")
                            }
                        }

                        CaliqButton(
                            onClick = {
                                if (name.isBlank() || selectedClassName.isEmpty()) {
                                    onShowError("الرجاء إدخال اسم الطالب واختيار الصف")
                                } else {
                                    val hasEmptyGrades = subjectsMap.values.any { sub ->
                                        if (sub.t1Val == "SKIPPED") false
                                        else {
                                            val t1Done = if (sub.t1Mode == TermMode.TERM) sub.t1Val.isNotBlank() else sub.t1Months.any { it.isNotBlank() }
                                            val midDone = sub.midVal.isNotBlank()
                                            val t2Done = if (sub.t2Mode == TermMode.TERM) sub.t2Val.isNotBlank() else sub.t2Months.any { it.isNotBlank() }
                                            !t1Done || !midDone || !t2Done
                                        }
                                    }
                                    onSave(StudentRecord(student?.id ?: "st_${System.currentTimeMillis()}", name, selectedClassName, subjectsMap.toMap()), hasEmptyGrades)
                                }
                            },
                            modifier = Modifier.weight(if (student != null) 1f else 2f),
                            contentPadding = PaddingValues(14.dp)
                        ) {
                            Icon(Icons.Default.Add, null)
                            Spacer(Modifier.width(8.dp))
                            Text("حفظ البيانات", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

    }
}
    if (showDeleteConfirm && student != null) {
        DeleteConfirmDialog(
            onConfirm = { 
                showDeleteConfirm = false
                onDelete(student.id)
            },
            onDismiss = { showDeleteConfirm = false }
        )
    }
        }
    }
}

@Composable
fun MonthlyGradesInputList(
    months: List<String>,
    onUpdateMonths: (List<String>) -> Unit,
    onShowMessage: (String) -> Unit
) {
    var monthToDelete by remember { mutableStateOf<Int?>(null) }
    var animatingDeleteId by remember { mutableStateOf<String?>(null) }
    
    // Stable ID tracking for each month entry so animations never mismatch
    var idList by remember { mutableStateOf(List(months.size) { "m_${it}_${System.currentTimeMillis()}" }) }
    
    LaunchedEffect(months.size) {
        if (idList.size < months.size) {
            val needed = months.size - idList.size
            idList = idList + List(needed) { "m_${idList.size + it}_${System.currentTimeMillis()}" }
        } else if (idList.size > months.size) {
            idList = idList.take(months.size)
        }
    }
    
    val coroutineScope = rememberCoroutineScope()

    if (monthToDelete != null) {
        DeleteConfirmDialog(
            title = "حذف الشهر",
            message = "هل أنت متأكد من حذف هذا الشهر؟",
            onConfirm = { 
                val targetIdx = monthToDelete!!
                monthToDelete = null
                if (targetIdx in months.indices) {
                    val targetId = idList.getOrNull(targetIdx)
                    animatingDeleteId = targetId
                    coroutineScope.launch {
                        kotlinx.coroutines.delay(280)
                        if (targetIdx in months.indices) {
                            val updatedMonths = months.toMutableList().apply { removeAt(targetIdx) }
                            if (targetIdx in idList.indices) {
                                idList = idList.toMutableList().apply { removeAt(targetIdx) }
                            }
                            onUpdateMonths(updatedMonths)
                        }
                        animatingDeleteId = null
                        onShowMessage("تم الحذف بنجاح")
                    }
                }
            },
            onDismiss = { monthToDelete = null }
        )
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(
                    animationSpec = androidx.compose.animation.core.spring(
                        dampingRatio = 0.85f,
                        stiffness = 420f
                    )
                ),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            months.forEachIndexed { idx, valStr ->
                val itemId = idList.getOrElse(idx) { "m_$idx" }
                val isVisible = animatingDeleteId != itemId
                key(itemId) {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = isVisible,
                        enter = androidx.compose.animation.expandVertically(
                            animationSpec = androidx.compose.animation.core.spring(
                                dampingRatio = 0.82f,
                                stiffness = 400f
                            )
                        ) + androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(180)),
                        exit = androidx.compose.animation.shrinkVertically(
                            animationSpec = androidx.compose.animation.core.spring(
                                dampingRatio = 0.85f,
                                stiffness = 460f
                            ),
                            shrinkTowards = Alignment.Top
                        ) + androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(120))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            GradeInputField(
                                label = "الشهر ${idx + 1}:",
                                value = valStr,
                                onValueChange = { newVal ->
                                    val updated = months.toMutableList()
                                    updated[idx] = newVal
                                    onUpdateMonths(updated)
                                },
                                modifier = Modifier.weight(1f)
                            )
                            com.Nightjar.caliq5.ui.components.CaliqIconButton(
                                onClick = {
                                    monthToDelete = idx
                                },
                                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f),
                                modifier = Modifier.size(38.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = "حذف الشهر",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        CaliqOutlinedButton(
            onClick = {
                val updated = months.toMutableList()
                updated.add("")
                idList = idList + "m_${idList.size}_${kotlin.time.TimeSource.Monotonic.markNow().hashCode()}"
                onUpdateMonths(updated)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
        ) {
            Icon(Icons.Filled.Add, null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text("إضافة شهر", fontSize = 12.sp)
        }
    }
}

@Composable
fun SubjectGradeEditCard(
    name: String,
    record: SubjectGradeRecord,
    isGenerallyExempt: Boolean,
    isAllSubjectsEntered: Boolean,
    isCustom: Boolean = false,
    isOptional: Boolean = false,
    isSkipped: Boolean = false,
    onRename: ((String) -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    onSkipToggle: (() -> Unit)? = null,
    onUpdate: (SubjectGradeRecord) -> Unit,
    onShowMessage: (String) -> Unit = {}
) {
    // Local state to avoid parent recomposition on every keystroke
    val t1Val = record.t1Val
    val midVal = record.midVal
    val t2Val = record.t2Val
    val finalVal = record.finalVal

    val coroutineScope = rememberCoroutineScope()
    val t1D = if (record.t1Mode == TermMode.TERM) t1Val.parseGradeToDouble() else {
        val gr = record.t1Months.mapNotNull { it.parseGradeToDouble() }
        if (gr.isNotEmpty()) gr.average() else null
    }
    val midD = midVal.parseGradeToDouble()
    val t2D = if (record.t2Mode == TermMode.TERM) t2Val.parseGradeToDouble() else {
        val gr = record.t2Months.mapNotNull { it.parseGradeToDouble() }
        if (gr.isNotEmpty()) gr.average() else null
    }

    val saeiRaw = if (t1D != null && midD != null && t2D != null) (t1D + midD + t2D) / 3.0 else null
    val saeiRounded = saeiRaw?.let { num ->
        val fl = kotlin.math.floor(num)
        val dec = num - fl
        when {
            dec >= 0.6 -> kotlin.math.ceil(num)
            num >= 49.6 && num < 50.0 -> 50.0
            num >= 74.6 && num < 75.0 -> 75.0
            num >= 89.6 && num < 90.0 -> 90.0
            else -> num
        }
    }

    val isIndividualExempt = saeiRounded != null && saeiRounded >= 90.0
    val isExempt = isGenerallyExempt || isIndividualExempt
    
    var showSubjectDeleteConfirm by remember { mutableStateOf(false) }
    var isCardVisible by remember { mutableStateOf(true) }
    
    if (showSubjectDeleteConfirm) {
        DeleteConfirmDialog(
            title = "حذف المادة المخصصة",
            message = "هل أنت متأكد من حذف \"$name\"؟",
            onConfirm = { 
                showSubjectDeleteConfirm = false
                isCardVisible = false
                coroutineScope.launch {
                    kotlinx.coroutines.delay(260)
                    onDelete?.invoke()
                }
            },
            onDismiss = { showSubjectDeleteConfirm = false }
        )
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

    AnimatedVisibility(
        visible = isCardVisible,
        enter = androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(250)) +
                androidx.compose.animation.expandVertically(
                    animationSpec = androidx.compose.animation.core.spring(
                        dampingRatio = androidx.compose.animation.core.Spring.DampingRatioLowBouncy,
                        stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow
                    )
                ) +
                androidx.compose.animation.slideInVertically(
                    animationSpec = androidx.compose.animation.core.spring(
                        dampingRatio = androidx.compose.animation.core.Spring.DampingRatioLowBouncy,
                        stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow
                    )
                ) { -it / 3 },
        exit = androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(180)) +
                androidx.compose.animation.shrinkVertically(
                    animationSpec = androidx.compose.animation.core.spring(
                        dampingRatio = androidx.compose.animation.core.Spring.DampingRatioNoBouncy,
                        stiffness = androidx.compose.animation.core.Spring.StiffnessMedium
                    )
                ) +
                androidx.compose.animation.slideOutVertically(
                    animationSpec = androidx.compose.animation.core.spring(
                        dampingRatio = androidx.compose.animation.core.Spring.DampingRatioNoBouncy,
                        stiffness = androidx.compose.animation.core.Spring.StiffnessMedium
                    )
                ) { -it / 3 }
    ) {
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
                .animateContentSize(
                    animationSpec = androidx.compose.animation.core.spring(
                        dampingRatio = 0.85f,
                        stiffness = 420f
                    )
                )
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                if (isCustom && onRename != null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Icon(
                                imageVector = Icons.Default.EditNote,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        GradeInputField(
                            label = "",
                            value = name,
                            onValueChange = onRename,
                            placeholder = "اسم المادة",
                            showPercent = false,
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Text
                        )
                    }
                    if (onDelete != null) {
                        Spacer(Modifier.width(10.dp))
                        CaliqIconButton(
                            onClick = { showSubjectDeleteConfirm = true },
                            containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                            contentColor = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "حذف المادة",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSkipped) errorColor.copy(alpha = 0.12f) else if (isExempt) com.Nightjar.caliq5.theme.successColor.copy(alpha = 0.12f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Icon(
                                    imageVector = if (isExempt) Icons.Default.Stars else Icons.Default.MenuBook,
                                    contentDescription = null,
                                    tint = if (isSkipped) errorColor else if (isExempt) com.Nightjar.caliq5.theme.successColor else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Text(
                            name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isSkipped) errorColor else if (isExempt) com.Nightjar.caliq5.theme.successColor else MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Start
                        )
                    }

                    if (isOptional && onSkipToggle != null) {
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
                                label = "mygrades_chip_content_anim"
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
            }
            
            androidx.compose.animation.AnimatedContent(
                targetState = isSkipped,
                transitionSpec = {
                    (androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(220)) +
                     androidx.compose.animation.expandVertically(animationSpec = androidx.compose.animation.core.spring(dampingRatio = androidx.compose.animation.core.Spring.DampingRatioLowBouncy, stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow))
                    ).togetherWith(
                     androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(180)) +
                     androidx.compose.animation.shrinkVertically(animationSpec = androidx.compose.animation.core.spring(dampingRatio = androidx.compose.animation.core.Spring.DampingRatioNoBouncy, stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow))
                    )
                },
                label = "skip_morphing"
            ) { skipped ->
                if (skipped) {
                    Text(
                        text = "تم استبعاد هذه المادة من حساب السعي والمعدل العام والإعفاء.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = com.Nightjar.caliq5.theme.errorColor, fontWeight = FontWeight.SemiBold),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                    )
                } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("إدخال معدل الفصل الأول", style = MaterialTheme.typography.bodySmall)
                        com.Nightjar.caliq5.ui.components.SlidingSegmentedControl(
                            options = listOf("فصلي" to TermMode.TERM, "شهريات" to TermMode.MONTHLY),
                            selectedOption = record.t1Mode,
                            onOptionSelected = { newMode ->
                                val newT1Val = if (newMode == TermMode.TERM && record.t1Months.isNotEmpty()) {
                                    val valid = record.t1Months.mapNotNull { it.toDoubleOrNull() }
                                    if (valid.isNotEmpty()) formatDecimalGrade(valid.average()) else record.t1Val
                                } else record.t1Val
                                onUpdate(record.copy(t1Mode = newMode, t1Val = newT1Val))
                            },
                            modifier = Modifier.width(160.dp)
                        )
                    }

                    androidx.compose.animation.AnimatedContent(
                        targetState = record.t1Mode,
                        transitionSpec = {
                            (androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(250)) + androidx.compose.animation.expandVertically(animationSpec = androidx.compose.animation.core.tween(300, easing = androidx.compose.animation.core.FastOutSlowInEasing))).togetherWith(
                                androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(200)) + androidx.compose.animation.shrinkVertically(animationSpec = androidx.compose.animation.core.tween(250, easing = androidx.compose.animation.core.FastOutSlowInEasing))
                            )
                        },
                        label = "T1Mode"
                    ) { mode ->
                        if (mode == TermMode.TERM) {
                            GradeInputField(label = "", value = t1Val, onValueChange = { onUpdate(record.copy(t1Val = it)) })
                        } else {
                            MonthlyGradesInputList(
                                months = record.t1Months,
                                onUpdateMonths = { newMonths -> 
                                    val valid = newMonths.mapNotNull { it.toDoubleOrNull() }
                                    val avgStr = if (valid.isNotEmpty()) formatDecimalGrade(valid.average()) else ""
                                    onUpdate(record.copy(t1Months = newMonths, t1Val = avgStr)) 
                                },
                                onShowMessage = onShowMessage
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    GradeInputField(
                        label = "نصف السنة:",
                        value = midVal,
                        onValueChange = { onUpdate(record.copy(midVal = it)) }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("إدخال معدل الفصل الثاني", style = MaterialTheme.typography.bodySmall)
                        com.Nightjar.caliq5.ui.components.SlidingSegmentedControl(
                            options = listOf("فصلي" to TermMode.TERM, "شهريات" to TermMode.MONTHLY),
                            selectedOption = record.t2Mode,
                            onOptionSelected = { newMode ->
                                val newT2Val = if (newMode == TermMode.TERM && record.t2Months.isNotEmpty()) {
                                    val valid = record.t2Months.mapNotNull { it.toDoubleOrNull() }
                                    if (valid.isNotEmpty()) formatDecimalGrade(valid.average()) else record.t2Val
                                } else record.t2Val
                                onUpdate(record.copy(t2Mode = newMode, t2Val = newT2Val))
                            },
                            modifier = Modifier.width(160.dp)
                        )
                    }

                    androidx.compose.animation.AnimatedContent(
                        targetState = record.t2Mode,
                        transitionSpec = {
                            (androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(250)) + androidx.compose.animation.expandVertically(animationSpec = androidx.compose.animation.core.tween(300, easing = androidx.compose.animation.core.FastOutSlowInEasing))).togetherWith(
                                androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(200)) + androidx.compose.animation.shrinkVertically(animationSpec = androidx.compose.animation.core.tween(250, easing = androidx.compose.animation.core.FastOutSlowInEasing))
                            )
                        },
                        label = "T2Mode"
                    ) { mode ->
                        if (mode == TermMode.TERM) {
                            GradeInputField(label = "", value = t2Val, onValueChange = { onUpdate(record.copy(t2Val = it)) })
                        } else {
                            MonthlyGradesInputList(
                                months = record.t2Months,
                                onUpdateMonths = { newMonths -> 
                                    val valid = newMonths.mapNotNull { it.toDoubleOrNull() }
                                    val avgStr = if (valid.isNotEmpty()) formatDecimalGrade(valid.average()) else ""
                                    onUpdate(record.copy(t2Months = newMonths, t2Val = avgStr)) 
                                },
                                onShowMessage = onShowMessage
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))

                    val finalFieldAlpha by animateFloatAsState(
                        targetValue = if (isExempt) 0.50f else 1.0f,
                        animationSpec = androidx.compose.animation.core.tween(400, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                        label = "final_field_alpha"
                    )

                    GradeInputField(
                        label = if (isExempt) "الامتحان النهائي (معفى):" else "الامتحان النهائي:",
                        value = if (isExempt) "" else finalVal,
                        onValueChange = { onUpdate(record.copy(finalVal = it)) },
                        placeholder = if (isExempt) "معفى ✨" else "أدخل الدرجة",
                        enabled = !isExempt,
                        modifier = Modifier
                            .graphicsLayer { alpha = finalFieldAlpha }
                            .animateContentSize(
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            )
                    )

                    // Live Calculation box with smooth fluid content animation
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize(
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioLowBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                )
                            ),
                        shape = RoundedCornerShape(18.dp),
                        color = if (isExempt) com.Nightjar.caliq5.theme.successColor.copy(alpha = 0.08f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (isExempt) com.Nightjar.caliq5.theme.successColor.copy(alpha = 0.25f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.20f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (isExempt) {
                            val exemptRoundingNote = when {
                                saeiRaw == null -> null
                                saeiRaw >= 74.6 && saeiRaw < 75.0 -> "المعدل قبل الجبر (${formatDecimalGrade(saeiRaw)}%) - تم جبر الكسر إلى 75% للإعفاء"
                                saeiRaw >= 89.6 && saeiRaw < 90.0 -> "المعدل قبل الجبر (${formatDecimalGrade(saeiRaw)}%) - تم جبر الكسر إلى 90% للإعفاء الفردي"
                                saeiRaw >= 49.6 && saeiRaw < 50.0 -> "المعدل قبل الجبر (${formatDecimalGrade(saeiRaw)}%) - تم جبر الكسر إلى 50%"
                                (saeiRaw - kotlin.math.floor(saeiRaw)) >= 0.6 -> "المعدل قبل الجبر (${formatDecimalGrade(saeiRaw)}%) - تم جبر الكسر إلى ${kotlin.math.ceil(saeiRaw).toInt()}%"
                                else -> null
                            }
                            Text(
                                text = if (isGenerallyExempt) "مبروك ✨ 🎉 الطالب معفى إعفاء عام في جميع الدروس لهذا العام ونجحت الى صفك التالي.." else "🎉 مبروك! الطالب معفى إعفاء فردي في هذه المادة.",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = com.Nightjar.caliq5.theme.successColor),
                                textAlign = TextAlign.Start
                            )
                            Text(
                                text = "الدرجة النهائية ستكون السعي: ${saeiRaw?.let { formatDecimalGrade(it) }}%",
                                style = MaterialTheme.typography.labelSmall.copy(color = com.Nightjar.caliq5.theme.successColor),
                                textAlign = TextAlign.Start
                            )
                            if (exemptRoundingNote != null) {
                                Text(
                                    text = "💡 ملاحظة: $exemptRoundingNote",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = com.Nightjar.caliq5.theme.successColor,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    textAlign = TextAlign.Start
                                )
                            }
                        } else if (saeiRaw != null) {
                            val generalRoundingNote = when {
                                saeiRaw >= 74.6 && saeiRaw < 75.0 -> "المعدل قبل الجبر (${formatDecimalGrade(saeiRaw)}%) - تم جبر الكسر إلى 75%"
                                saeiRaw >= 89.6 && saeiRaw < 90.0 -> "المعدل قبل الجبر (${formatDecimalGrade(saeiRaw)}%) - تم جبر الكسر إلى 90%"
                                saeiRaw >= 49.6 && saeiRaw < 50.0 -> "المعدل قبل الجبر (${formatDecimalGrade(saeiRaw)}%) - تم جبر الكسر إلى 50%"
                                (saeiRaw - kotlin.math.floor(saeiRaw)) >= 0.6 -> "المعدل قبل الجبر (${formatDecimalGrade(saeiRaw)}%) - تم جبر الكسر إلى ${kotlin.math.ceil(saeiRaw).toInt()}%"
                                else -> null
                            }
                            Text(
                                text = "السعي السنوي المحسوب: ${formatDecimalGrade(saeiRaw)}%",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary),
                                textAlign = TextAlign.Start
                            )
                            if (generalRoundingNote != null) {
                                Text(
                                    text = "💡 ملاحظة: $generalRoundingNote",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    textAlign = TextAlign.Start
                                )
                            }
                            val fin = record.finalVal.toDoubleOrNull()
                            if (fin != null) {
                                val finalGrade = (saeiRaw + fin) / 2.0
                                Text(
                                    text = "الدرجة النهائية للمادة: ${formatDecimalGrade(finalGrade)}%",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary),
                                    textAlign = TextAlign.Start
                                )
                            } else {
                                Text(
                                    text = "أدخل درجة الامتحان النهائي لحساب المعدل النهائي.",
                                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.outline),
                                    textAlign = TextAlign.Start
                                )
                            }
                        } else if (t1D != null && midD != null && t2D == null) {
                            val req75 = 225.0 - t1D - midD
                            val req90 = 270.0 - t1D - midD
                            val text75 = if (req75 > 100) "غير ممكن" else if (req75 < 0) "0" else "${formatDecimalGrade(req75)} لـ 75%"
                            val text90 = if (req90 > 100) "غير ممكن" else if (req90 < 0) "0" else "${formatDecimalGrade(req90)} لـ 90%"
                            Text(
                                text = "تحتاج في الفصل الثاني إلى:",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary),
                                textAlign = TextAlign.Start
                            )
                            Text(
                                text = "$text75 أو $text90",
                                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                                textAlign = TextAlign.Start
                            )
                        } else if (saeiRaw == null) {
                            Text(
                                text = "بانتظار إدخال بقية الفصول...",
                                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.outline),
                                textAlign = TextAlign.Start
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
