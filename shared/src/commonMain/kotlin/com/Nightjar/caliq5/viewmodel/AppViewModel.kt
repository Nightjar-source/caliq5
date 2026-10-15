package com.Nightjar.caliq5.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.Nightjar.caliq5.data.*
import com.Nightjar.caliq5.logic.GradeCalculator
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.TimeZone
import com.Nightjar.caliq5.ui.components.parseGradeToDouble

/**
 * Sealed class representing the result state to display in the overlay.
 * Each variant holds the pre-computed display data.
 */
sealed class ResultDisplayState {
    abstract val title: String

    data class ExemptionResult(
        override val title: String,
        val data: ExemptionResultData,
    ) : ResultDisplayState()

    data class AnnualResult(
        override val title: String,
        val data: AnnualResultData,
        var isExpanded: Boolean = false,
    ) : ResultDisplayState()

    data class MonthlyResult(
        override val title: String,
        val data: MonthlyResultData,
        var isExpanded: Boolean = false,
    ) : ResultDisplayState()

    data class Term2Result(
        override val title: String,
        val data: Term2ResultData,
        var is75Expanded: Boolean = false,
        var is90Expanded: Boolean = false,
    ) : ResultDisplayState()

    data class StudentProfileResult(
        override val title: String,
        val student: StudentRecord,
    ) : ResultDisplayState()
}

data class SnackbarMessage(
    val message: String,
    val isError: Boolean = false,
)

/**
 * Main ViewModel for the entire app.
 * Handles navigation, calculation, autosave state, and result display.
 * Mirrors the global state management in caliq5.html JavaScript.
 */
@kotlinx.serialization.Serializable
data class MonthlyGrade(val id: Int, val grade: String)

class AppViewModel : ViewModel() {
    /**
     * الفصل الذكي بين "القتل في الخلفية" (Process Death) و "الإغلاق الكامل" (Cold Start):
     * - إذا كان isProcessDeath == true: يستعيد فوراً نفس الشاشة ونفس مكدس التنقل والبيانات.
     * - إذا كان isProcessDeath == false: يمسح حالات التنقل السابقة ويفتح نظيفاً على الشاشة الرئيسية (EXEMPTION_SELECT).
     */
    fun initLaunchState(isProcessDeath: Boolean) {
        viewModelScope.launch {
            if (isProcessDeath) {
                val savedScreenName = DataRepository.loadPreference("last_active_screen")
                if (!savedScreenName.isNullOrEmpty()) {
                    try {
                        val restoredScreen = Screen.valueOf(savedScreenName)
                        _currentScreen.value = restoredScreen
                        if (restoredScreen != Screen.EXEMPTION_SELECT) {
                            _backStack.value = listOf(Screen.EXEMPTION_SELECT, restoredScreen)
                            _canGoBack.value = true
                            _isNavHidden.value = restoredScreen == Screen.EXEMPTION_INPUT
                        }
                    } catch (e: Throwable) {
                        _currentScreen.value = Screen.EXEMPTION_SELECT
                        _backStack.value = listOf(Screen.EXEMPTION_SELECT)
                        _canGoBack.value = false
                    }
                }
            } else {
                // الفتح البارد: تنظيف مسار التنقل والبدء نظيفاً على الشاشة الرئيسية مع بقاء كافة المسودات والبيانات
                _currentScreen.value = Screen.EXEMPTION_SELECT
                _backStack.value = listOf(Screen.EXEMPTION_SELECT)
                _canGoBack.value = false
                _isNavHidden.value = false
                DataRepository.savePreference("last_active_screen", Screen.EXEMPTION_SELECT.name)
            }
        }
    }

    /**
     * Backward-compatible alias for initLaunchState
     */
    fun checkAndRestoreScreen(isRestoredFromProcessDeath: Boolean) {
        initLaunchState(isRestoredFromProcessDeath)
    }


    // ===== Navigation =====
    private val _currentScreen = MutableStateFlow(Screen.EXEMPTION_SELECT)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _isNavHidden = MutableStateFlow(false)
    val isNavHidden: StateFlow<Boolean> = _isNavHidden.asStateFlow()

    // Google Modern Compose Immutable Safe Navigation BackStack
    private val _backStack = MutableStateFlow<List<Screen>>(listOf(Screen.EXEMPTION_SELECT))
    val backStack: StateFlow<List<Screen>> = _backStack.asStateFlow()
    private val _canGoBack = MutableStateFlow(false)
    val canGoBack: StateFlow<Boolean> = _canGoBack.asStateFlow()

    private val _editingStudentMode = MutableStateFlow<String?>(null) // null, "add", "edit"
    val editingStudentMode: StateFlow<String?> = _editingStudentMode.asStateFlow()
    val isEditingStudentInMyGrades: StateFlow<Boolean> = MutableStateFlow(false)

    private val _myGradesBackEvents = kotlinx.coroutines.flow.MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val myGradesBackEvents: kotlinx.coroutines.flow.SharedFlow<Unit> = _myGradesBackEvents

    fun setEditingStudentInMyGrades(mode: String?) {
        _editingStudentMode.value = mode
    }

    fun setEditingStudentInMyGrades(isEditing: Boolean) {
        _editingStudentMode.value = if (isEditing) "add" else null
    }

    fun handleTopBarBack(): Boolean {
        try {
            if (_resultState.value != null) {
                hideResult()
                return true
            }
            if (_showSideMenu.value) {
                closeMenu()
                return true
            }
            if (_editingStudentMode.value != null) {
                _myGradesBackEvents.tryEmit(Unit)
                return true
            }
            if (_currentScreen.value == Screen.EXEMPTION_INPUT) {
                _isNavigatingBack.value = true
                _backStack.value = listOf(Screen.EXEMPTION_SELECT)
                _currentScreen.value = Screen.EXEMPTION_SELECT
                _canGoBack.value = false
                _isNavHidden.value = false
                savePreference("last_active_screen", Screen.EXEMPTION_SELECT.name, immediate = true)
                return true
            }
            if (popPage()) {
                return true
            }
            if (_currentScreen.value != Screen.EXEMPTION_SELECT) {
                _isNavigatingBack.value = true
                _backStack.value = listOf(Screen.EXEMPTION_SELECT)
                _currentScreen.value = Screen.EXEMPTION_SELECT
                _canGoBack.value = false
                _isNavHidden.value = false
                savePreference("last_active_screen", Screen.EXEMPTION_SELECT.name, immediate = true)
                return true
            }
        } catch (e: Throwable) {
            _isNavigatingBack.value = true
            _currentScreen.value = Screen.EXEMPTION_SELECT
            _canGoBack.value = false
            savePreference("last_active_screen", Screen.EXEMPTION_SELECT.name, immediate = true)
            return true
        }
        return false
    }

    private val _isNavigatingBack = MutableStateFlow(false)
    val isNavigatingBack: StateFlow<Boolean> = _isNavigatingBack.asStateFlow()
    
    fun navigateTo(screen: Screen) {
        savePreference("last_active_screen", screen.name, immediate = true)
        if (screen == Screen.EXEMPTION_SELECT) {
            _backStack.value = listOf(Screen.EXEMPTION_SELECT)
            _canGoBack.value = false
            _currentScreen.value = screen
            _isNavHidden.value = false
            _isNavigatingBack.value = false
            return
        }
        val currentStack = _backStack.value
        if (currentStack.lastOrNull() == screen) {
            _currentScreen.value = screen
            return
        }
        _isNavigatingBack.value = false
        val newStack = currentStack + screen
        _backStack.value = newStack
        _canGoBack.value = newStack.size > 1
        _currentScreen.value = screen
        // Hide nav for detail pages
        _isNavHidden.value = screen == Screen.EXEMPTION_INPUT
    }

    /**
     * Immutable Safe Navigation: Pops the current screen from the back stack.
     * Uses dropLast(1) to produce a pure immutable snapshot:
     * - Zero risk of NoSuchMethodError on older Android (Samsung J7 Android 8).
     * - Thread-safe and race-condition free during rapid back presses.
     * - Zero index out of bounds exceptions.
     */
    fun popPage(): Boolean {
        try {
            val currentStack = _backStack.value
            if (currentStack.size > 1) {
                _isNavigatingBack.value = true
                val newStack = currentStack.dropLast(1)
                _backStack.value = newStack
                val previousScreen = newStack.lastOrNull() ?: Screen.EXEMPTION_SELECT
                _currentScreen.value = previousScreen
                _isNavHidden.value = previousScreen == Screen.EXEMPTION_INPUT
                _canGoBack.value = newStack.size > 1 || previousScreen != Screen.EXEMPTION_SELECT
                savePreference("last_active_screen", previousScreen.name, immediate = true)
                return true
            }
        } catch (e: Throwable) {
            _backStack.value = listOf(Screen.EXEMPTION_SELECT)
            _currentScreen.value = Screen.EXEMPTION_SELECT
            _canGoBack.value = false
            savePreference("last_active_screen", Screen.EXEMPTION_SELECT.name, immediate = true)
            return true
        }
        return false // Let the system handle exit
    }

    /**
     * Backward-compatible alias for popPage()
     */
    fun navigateBack(): Boolean = popPage()

    fun goBack() {
        handleTopBarBack()
    }

    fun resetToDeepLink(screen: Screen) {
        savePreference("last_active_screen", screen.name, immediate = true)
        _backStack.value = if (screen == Screen.EXEMPTION_SELECT) {
            listOf(Screen.EXEMPTION_SELECT)
        } else {
            listOf(Screen.EXEMPTION_SELECT, screen)
        }
        _currentScreen.value = screen
        _isNavHidden.value = screen == Screen.EXEMPTION_INPUT
        _canGoBack.value = screen != Screen.EXEMPTION_SELECT
    }

    // ===== Side Menu =====
    private val _showSideMenu = MutableStateFlow(false)
    val showSideMenu: StateFlow<Boolean> = _showSideMenu.asStateFlow()

    fun openMenu() { _showSideMenu.value = true }
    fun closeMenu() { _showSideMenu.value = false }

    // ===== Appearance Settings =====
    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    fun updateSettings(newSettings: AppSettings) {
        _settings.value = newSettings
        viewModelScope.launch {
            DataRepository.saveSettings(newSettings)
        com.Nightjar.caliq5.widget.updateAppWidget()
        }
    }

    // ===== Result Overlay =====
    private val _resultState = MutableStateFlow<ResultDisplayState?>(null)
    val resultState: StateFlow<ResultDisplayState?> = _resultState.asStateFlow()

    fun showResult(result: ResultDisplayState) {
        _resultState.value = result
        _isNavHidden.value = true
    }

    fun hideResult() {
        _resultState.value = null
        _isNavHidden.value = _currentScreen.value == Screen.EXEMPTION_INPUT
    }

    fun requestPinWidget() {
        val alreadyPinned = com.Nightjar.caliq5.widget.isAppWidgetPinned()
        com.Nightjar.caliq5.widget.pinAppWidget()
        if (alreadyPinned) {
            showAlert("تم الإضافة بنجاح!")
        } else {
            showAlert("تم طلب إضافة التطبيق المصغر. يرجى التأكيد في النافذة المنبثقة.")
        }
    }

    // ===== Loading =====
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // ===== Snackbar =====
    private val _snackbarMessage = MutableStateFlow<SnackbarMessage?>(null)
    val snackbarMessage: StateFlow<SnackbarMessage?> = _snackbarMessage.asStateFlow()

    private var alertJob: kotlinx.coroutines.Job? = null
    fun showAlert(message: String, isError: Boolean = false) {
        alertJob?.cancel()
        alertJob = viewModelScope.launch {
            _snackbarMessage.value = SnackbarMessage(message, isError)
            delay(3200)
            _snackbarMessage.value = null
        }
    }

    // ===== Exemption Screen State =====
    private val _selectedClass = MutableStateFlow("")
    val selectedClass: StateFlow<String> = _selectedClass.asStateFlow()

    fun selectClass(className: String) {
        _selectedClass.value = className
        savePreference("selectedClass", className)
        val subjects = ClassData.getSubjects(className)
        val entries = subjects.associate { subject ->
            subject.name to SubjectGradeEntry(
                name = subject.name,
                grade = null,
                isSkipped = false,
            )
        }
        _exemptionGrades.value = entries
        loadExemptionDrafts()
        loadCustomSubjectsDrafts()
    }

    // Grade entries per subject for the exemption screen
    private val _exemptionGrades = MutableStateFlow<Map<String, SubjectGradeEntry>>(emptyMap())
    val exemptionGrades: StateFlow<Map<String, SubjectGradeEntry>> = _exemptionGrades.asStateFlow()

    fun updateSubjectGrade(subjectName: String, grade: String) {
        val current = _exemptionGrades.value.toMutableMap()
        val existing = current[subjectName] ?: SubjectGradeEntry(
            name = subjectName, grade = null, isSkipped = false
        )
        current[subjectName] = existing.copy(grade = grade.parseGradeToDouble())
        _exemptionGrades.value = current
        saveExemptionDrafts()
    }

    fun updateSubjectDetailGrades(subjectName: String, term1: String, midYear: String, term2: String) {
        val t1 = term1.parseGradeToDouble()
        val mid = midYear.parseGradeToDouble()
        val t2 = term2.parseGradeToDouble()

        val current = _exemptionGrades.value.toMutableMap()
        val existing = current[subjectName] ?: SubjectGradeEntry(
            name = subjectName, grade = null, isSkipped = false
        )

        // Instant exact arithmetic mean if all 3 grades are present
        val calculatedGrade = if (t1 != null && mid != null && t2 != null) {
            (t1 + mid + t2) / 3.0
        } else if (term1.isEmpty() && midYear.isEmpty() && term2.isEmpty()) {
            existing.grade
        } else null

        current[subjectName] = existing.copy(
            grade = calculatedGrade,
            term1 = t1,
            midYear = mid,
            term2 = t2,
        )
        _exemptionGrades.value = current
        saveExemptionDrafts()
    }

    fun toggleSkipSubject(subjectName: String) {
        val current = _exemptionGrades.value.toMutableMap()
        val existing = current[subjectName] ?: SubjectGradeEntry(
            name = subjectName, grade = null, isSkipped = false
        )
        current[subjectName] = existing.copy(
            isSkipped = !existing.isSkipped,
            grade = if (!existing.isSkipped) null else existing.grade,
        )
        _exemptionGrades.value = current
        saveExemptionDrafts()
    }

    // Custom subjects (added by user)
    private val _customSubjects = MutableStateFlow<List<SubjectGradeEntry>>(emptyList())
    val customSubjects: StateFlow<List<SubjectGradeEntry>> = _customSubjects.asStateFlow()

    fun addCustomSubject() {
        val nextNumber = _customSubjects.value.size + 1
        val newSubject = SubjectGradeEntry(
            name = "المادة $nextNumber",
            grade = null,
            isSkipped = false,
            id = "custom_" + kotlin.random.Random.nextLong().toString()
        )
        _customSubjects.value = _customSubjects.value + newSubject
        saveCustomSubjectsDrafts(immediate = true)
    }

    fun removeCustomSubject(index: Int) {
        _customSubjects.value = _customSubjects.value.toMutableList().also { it.removeAt(index) }
        saveCustomSubjectsDrafts(immediate = true)
    }

    fun updateCustomSubjectName(index: Int, name: String) {
        _customSubjects.value = _customSubjects.value.toMutableList().also {
            it[index] = it[index].copy(name = name)
        }
        saveCustomSubjectsDrafts()
    }

    fun updateCustomSubjectGrade(index: Int, grade: String) {
        _customSubjects.value = _customSubjects.value.toMutableList().also {
            it[index] = it[index].copy(grade = grade.parseGradeToDouble())
        }
        saveCustomSubjectsDrafts()
    }

    fun updateCustomSubjectDetailGrades(index: Int, t1: String, mid: String, t2: String) {
        val term1Val = t1.parseGradeToDouble()
        val midYearVal = mid.parseGradeToDouble()
        val term2Val = t2.parseGradeToDouble()
        
        val calculatedGrade = if (term1Val != null && midYearVal != null && term2Val != null) {
            (term1Val + midYearVal + term2Val) / 3.0
        } else if (t1.isEmpty() && mid.isEmpty() && t2.isEmpty()) {
            _customSubjects.value[index].grade
        } else null

        _customSubjects.value = _customSubjects.value.toMutableList().also {
            it[index] = it[index].copy(
                term1 = term1Val,
                midYear = midYearVal,
                term2 = term2Val,
                grade = calculatedGrade
            )
        }
        saveCustomSubjectsDrafts()
    }

    // ===== Main Calculations =====

    fun startGradeInput() {
        val cls = _selectedClass.value
        if (cls.isEmpty()) {
            showAlert("الرجاء اختيار الصف الدراسي من المربعات أعلاه.", isError = true)
            return
        }
        if (_exemptionGrades.value.isEmpty()) {
            val subjects = ClassData.getSubjects(cls)
            val entries = subjects.associate { subject ->
                subject.name to SubjectGradeEntry(
                    name = subject.name,
                    grade = null,
                    isSkipped = false,
                )
            }
            _exemptionGrades.value = entries
            loadExemptionDrafts()
            loadCustomSubjectsDrafts()
        }
        navigateTo(Screen.EXEMPTION_INPUT)
    }

    fun calculateExemptionResults() {
        viewModelScope.launch {
            val className = _selectedClass.value

            val hasEmptyCustom = _customSubjects.value.any { 
                it.name.trim().isEmpty() || (it.grade == null && it.term1 == null && it.midYear == null && it.term2 == null)
            }
            if (hasEmptyCustom) {
                showAlert("الرجاء إدخال اسم ودرجات لجميع المواد المخصصة أو قم بحذفها.", isError = true)
                _isLoading.value = false
                return@launch
            }

            val allEntries = withContext(Dispatchers.Default) {
                buildList {
                    // Standard subjects
                    ClassData.getSubjects(className).forEach { subject ->
                        val entry = _exemptionGrades.value[subject.name]
                            ?: SubjectGradeEntry(name = subject.name, grade = null, isSkipped = false)
                        add(entry)
                    }
                    // Custom subjects
                    _customSubjects.value.forEach { add(it) }
                }
            }

            val result = withContext(Dispatchers.Default) {
                GradeCalculator.calculateGeneralExemption(allEntries)
            }
            _isLoading.value = false

            result.fold(
                onSuccess = { data ->
                    val finalData = data.copy(className = className)
                    viewModelScope.launch(Dispatchers.IO) {
                        saveHistory(
                            HistoryItem(
                                id = "h_${System.currentTimeMillis()}",
                                type = ResultType.EXEMPTION,
                                title = "نتيجة الاعفاء العام والفردي للصف $className",
                                date = getCurrentDateString(),
                                data = Json.encodeToString(finalData),
                            )
                        )
                    }
                    showResult(
                        ResultDisplayState.ExemptionResult(
                            title = "نتيجة الاعفاء العام والفردي للصف $className",
                            data = finalData,
                        )
                    )
                },
                onFailure = { e ->
                    showAlert(e.message ?: "حدث خطأ في الحساب", isError = true)
                }
            )
        }
    }

    // Annual Average State
    private val _annualSubjectName = MutableStateFlow("")
    val annualSubjectName: StateFlow<String> = _annualSubjectName.asStateFlow()
    private val _annualTerm1 = MutableStateFlow("")
    val annualTerm1: StateFlow<String> = _annualTerm1.asStateFlow()
    private val _annualMidYear = MutableStateFlow("")
    val annualMidYear: StateFlow<String> = _annualMidYear.asStateFlow()
    private val _annualTerm2 = MutableStateFlow("")
    val annualTerm2: StateFlow<String> = _annualTerm2.asStateFlow()

    fun updateAnnualField(field: String, value: String) {
        when (field) {
            "name" -> _annualSubjectName.value = value
            "term1" -> _annualTerm1.value = value
            "midYear" -> _annualMidYear.value = value
            "term2" -> _annualTerm2.value = value
        }
        savePreference("annual_$field", value)
    }

    fun calculateAnnualAverage() {
        viewModelScope.launch {
            val name = _annualSubjectName.value.trim().ifEmpty { "مادة غير مسماة" }
            val t1 = _annualTerm1.value.parseGradeToDouble()
            val mid = _annualMidYear.value.parseGradeToDouble()
            val t2 = _annualTerm2.value.parseGradeToDouble()

            if (t1 == null || mid == null || t2 == null ||
                t1 < 0.0 || t1 > 100.0 || mid < 0.0 || mid > 100.0 || t2 < 0.0 || t2 > 100.0) {
                showAlert("الرجاء إدخال درجات صحيحة (0-100).", isError = true)
                return@launch
            }

            val result = withContext(Dispatchers.Default) {
                GradeCalculator.calculateAnnualAverage(name, t1, mid, t2)
            }
            viewModelScope.launch(Dispatchers.IO) {
                saveHistory(
                    HistoryItem(
                        id = "h_${System.currentTimeMillis()}",
                        type = ResultType.ANNUAL,
                        title = "نتيجة معدل السعي السنوي لمادة $name",
                        date = getCurrentDateString(),
                        data = Json.encodeToString(result),
                    )
                )
            }
            showResult(
                ResultDisplayState.AnnualResult(
                    title = "نتيجة معدل السعي السنوي لمادة $name",
                    data = result,
                )
            )
        }
    }

    // Monthly Average State
    private val _monthlySubjectName = MutableStateFlow("")
    val monthlySubjectName: StateFlow<String> = _monthlySubjectName.asStateFlow()
    private var nextMonthlyGradeId = 2
    private val _monthlyGrades = MutableStateFlow<List<MonthlyGrade>>(listOf(MonthlyGrade(0, ""), MonthlyGrade(1, "")))
    val monthlyGrades: StateFlow<List<MonthlyGrade>> = _monthlyGrades.asStateFlow()

    fun updateMonthlyField(field: String, value: String) {
        when (field) {
            "name" -> _monthlySubjectName.value = value
        }
        savePreference("monthly_$field", value)
    }

    fun addMonthGrade() {
        _monthlyGrades.value = _monthlyGrades.value + MonthlyGrade(nextMonthlyGradeId++, "")
        saveMonthlyGradesDrafts(immediate = true)
    }

    fun removeMonthGrade(index: Int) {
        if (_monthlyGrades.value.size > 1) {
            _monthlyGrades.value = _monthlyGrades.value.toMutableList().also { it.removeAt(index) }
            saveMonthlyGradesDrafts(immediate = true)
        }
    }

    fun updateMonthGrade(index: Int, value: String) {
        _monthlyGrades.value = _monthlyGrades.value.toMutableList().also {
            if (index < it.size) it[index] = it[index].copy(grade = value)
        }
        saveMonthlyGradesDrafts()
    }

    fun calculateMonthlyAverage() {
        viewModelScope.launch {
            val name = _monthlySubjectName.value.trim().ifEmpty { "مادة غير مسماة" }
            val grades = _monthlyGrades.value.mapNotNull { it.grade.parseGradeToDouble() }

            if (grades.size < 2) {
                showAlert("عذراً، يجب إدخال درجتين على الأقل لإجراء حساب المعدل الفصلي.", isError = true)
                return@launch
            }

            val result = withContext(Dispatchers.Default) {
                GradeCalculator.calculateMonthlyAverage(name, grades)
            }
            viewModelScope.launch(Dispatchers.IO) {
                saveHistory(
                    HistoryItem(
                        id = "h_${System.currentTimeMillis()}",
                        type = ResultType.MONTHLY,
                        title = "نتيجة المعدل الفصلي لمادة $name",
                        date = getCurrentDateString(),
                        data = Json.encodeToString(result),
                    )
                )
            }
            showResult(
                ResultDisplayState.MonthlyResult(
                    title = "نتيجة المعدل الفصلي لمادة $name",
                    data = result,
                )
            )
        }
    }

    // Term 2 State
    private val _term2SubjectName = MutableStateFlow("")
    val term2SubjectName: StateFlow<String> = _term2SubjectName.asStateFlow()
    private val _term2Term1 = MutableStateFlow("")
    val term2Term1: StateFlow<String> = _term2Term1.asStateFlow()
    private val _term2MidYear = MutableStateFlow("")
    val term2MidYear: StateFlow<String> = _term2MidYear.asStateFlow()

    fun updateTerm2Field(field: String, value: String) {
        when (field) {
            "name" -> _term2SubjectName.value = value
            "term1" -> _term2Term1.value = value
            "midYear" -> _term2MidYear.value = value
        }
        savePreference("term2_$field", value)
    }

    fun calculateTerm2() {
        viewModelScope.launch {
            val name = _term2SubjectName.value.trim().ifEmpty { "مادة غير مسماة" }
            val t1 = _term2Term1.value.parseGradeToDouble()
            val mid = _term2MidYear.value.parseGradeToDouble()

            if (t1 == null || mid == null || t1 < 0.0 || t1 > 100.0 || mid < 0.0 || mid > 100.0) {
                showAlert("الرجاء إدخال درجات صحيحة (0-100).", isError = true)
                return@launch
            }

            val result = withContext(Dispatchers.Default) {
                GradeCalculator.calculateTerm2Required(name, t1, mid)
            }
            viewModelScope.launch(Dispatchers.IO) {
                saveHistory(
                    HistoryItem(
                        id = "h_${System.currentTimeMillis()}",
                        type = ResultType.TERM2,
                        title = "نتيجة درجة الفصل الثاني لمادة $name",
                        date = getCurrentDateString(),
                        data = Json.encodeToString(result),
                    )
                )
            }
            showResult(
                ResultDisplayState.Term2Result(
                    title = "نتيجة درجة الفصل الثاني لمادة $name",
                    data = result,
                )
            )
        }
    }

    // ===== History / Statistics =====
    private val _history = MutableStateFlow<List<HistoryItem>>(emptyList())
    val history: StateFlow<List<HistoryItem>> = _history.asStateFlow()

    private fun saveHistory(item: HistoryItem) {
        val current = _history.value.toMutableList()
        current.add(0, item)
        _history.value = current.take(50)
        // Persist to DataStore (platform-specific, done via repository)
        viewModelScope.launch { DataRepository.saveHistory(current.take(50)) }
    }

    fun deleteHistoryItem(id: String) {
        _history.value = _history.value.filter { it.id != id }
        viewModelScope.launch { DataRepository.saveHistory(_history.value) }
    }

    fun showHistoryItemResult(item: HistoryItem) {
        try {
            val result = when (item.type) {
                ResultType.EXEMPTION -> {
                    val data = Json.decodeFromString<ExemptionResultData>(item.data)
                    ResultDisplayState.ExemptionResult(item.title, data)
                }
                ResultType.ANNUAL -> {
                    val data = Json.decodeFromString<AnnualResultData>(item.data)
                    ResultDisplayState.AnnualResult(item.title, data)
                }
                ResultType.MONTHLY -> {
                    val data = Json.decodeFromString<MonthlyResultData>(item.data)
                    ResultDisplayState.MonthlyResult(item.title, data)
                }
                ResultType.TERM2 -> {
                    val data = Json.decodeFromString<Term2ResultData>(item.data)
                    ResultDisplayState.Term2Result(item.title, data)
                }
            }
            showResult(result)
        } catch (e: Exception) {
            showAlert("تعذر فتح النتيجة المحفوظة", isError = true)
        }
    }

    // ===== My Grades Pro =====
    private val _students = MutableStateFlow<List<StudentRecord>>(emptyList())
    val students: StateFlow<List<StudentRecord>> = _students.asStateFlow()

    fun saveStudent(student: StudentRecord) {
        val current = _students.value.toMutableList()
        val index = current.indexOfFirst { it.id == student.id }
        if (index >= 0) current[index] = student else current.add(student)
        _students.value = current
        viewModelScope.launch { DataRepository.saveStudents(current) }
    }

    fun deleteStudent(id: String) {
        _students.value = _students.value.filter { it.id != id }
        viewModelScope.launch { DataRepository.saveStudents(_students.value) }
    }

    private val _showNotificationDialog = MutableStateFlow(false)
    val showNotificationDialog: StateFlow<Boolean> = _showNotificationDialog.asStateFlow()

    fun dismissNotificationDialog() {
        _showNotificationDialog.value = false
    }

    fun exportToPdf(result: ResultDisplayState) {
        viewModelScope.launch {
            showAlert("جارٍ إنشاء ملف PDF...", isError = false)
            withContext(Dispatchers.IO) {
                com.Nightjar.caliq5.export.PdfExporter.export(result)
            }
            showAlert("تم تنزيل الملف على هاتفك")
            // Show the elegant dialog for notification permission only if not granted
            if (!com.Nightjar.caliq5.utils.isNotificationPermissionGranted()) {
                _showNotificationDialog.value = true
            }
        }
    }

    // ===== AutoSave / Drafts =====
    private var saveJob: kotlinx.coroutines.Job? = null
    private fun saveExemptionDrafts(immediate: Boolean = false) {
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            if (!immediate) delay(500)
            DataRepository.saveExemptionDrafts(
                _selectedClass.value,
                _exemptionGrades.value,
            )
        }
    }

    private fun loadExemptionDrafts() {
        viewModelScope.launch {
            val drafts = DataRepository.loadExemptionDrafts(_selectedClass.value)
            if (drafts.isNotEmpty()) {
                _exemptionGrades.value = _exemptionGrades.value.toMutableMap().also {
                    drafts.forEach { (k, v) -> if (it.containsKey(k)) it[k] = v }
                }
            }
        }
    }

    private var customSaveJob: kotlinx.coroutines.Job? = null
    private fun saveCustomSubjectsDrafts(immediate: Boolean = false) {
        customSaveJob?.cancel()
        customSaveJob = viewModelScope.launch {
            if (!immediate) delay(500)
            DataRepository.saveCustomSubjectsDrafts(
                _selectedClass.value,
                _customSubjects.value,
            )
        }
    }

    private fun loadCustomSubjectsDrafts() {
        viewModelScope.launch {
            val drafts = DataRepository.loadCustomSubjectsDrafts(_selectedClass.value)
            _customSubjects.value = drafts
        }
    }

    private var monthlySaveJob: kotlinx.coroutines.Job? = null
    private fun saveMonthlyGradesDrafts(immediate: Boolean = false) {
        monthlySaveJob?.cancel()
        monthlySaveJob = viewModelScope.launch {
            if (!immediate) delay(500)
            DataRepository.saveMonthlyGrades(_monthlyGrades.value)
        }
    }

    private val prefSaveJobs = mutableMapOf<String, kotlinx.coroutines.Job>()
    private fun savePreference(key: String, value: String, immediate: Boolean = false) {
        prefSaveJobs[key]?.cancel()
        prefSaveJobs[key] = viewModelScope.launch(Dispatchers.IO) {
            if (!immediate) delay(500)
            DataRepository.savePreference(key, value)
        }
    }

    // ===== Initialization =====
    init {
        viewModelScope.launch(Dispatchers.IO) {
            // Load history
            _history.value = DataRepository.loadHistory()
            // Load students
            _students.value = DataRepository.loadStudents()
            // Load saved class selection
            val savedClass = DataRepository.loadPreference("selectedClass") ?: ""
            if (savedClass.isNotEmpty()) {
                _selectedClass.value = savedClass
                val subjects = ClassData.getSubjects(savedClass)
                val entries = subjects.associate { subject ->
                    subject.name to SubjectGradeEntry(
                        name = subject.name,
                        grade = null,
                        isSkipped = false,
                    )
                }
                _exemptionGrades.value = entries
                loadExemptionDrafts()
                loadCustomSubjectsDrafts()
            }
            
            // Load appearance and user drafts without changing screen on cold start
            // Screen restoration will only trigger if restored from system process death

            // Load appearance
            _settings.value = DataRepository.loadSettings()

            // Load annual screen drafts
            _annualSubjectName.value = DataRepository.loadPreference("annual_name") ?: ""
            _annualTerm1.value = DataRepository.loadPreference("annual_term1") ?: ""
            _annualMidYear.value = DataRepository.loadPreference("annual_midYear") ?: ""
            _annualTerm2.value = DataRepository.loadPreference("annual_term2") ?: ""

            // Load monthly screen drafts
            _monthlySubjectName.value = DataRepository.loadPreference("monthly_name") ?: ""
            val loadedMonthlyGrades = DataRepository.loadMonthlyGrades()
            if (loadedMonthlyGrades.isNotEmpty()) {
                _monthlyGrades.value = loadedMonthlyGrades
                nextMonthlyGradeId = (loadedMonthlyGrades.maxOfOrNull { it.id } ?: 1) + 1
            }

            // Load term 2 screen drafts
            _term2SubjectName.value = DataRepository.loadPreference("term2_name") ?: ""
            _term2Term1.value = DataRepository.loadPreference("term2_term1") ?: ""
            _term2MidYear.value = DataRepository.loadPreference("term2_midYear") ?: ""
        }
    }

    private fun getCurrentDateString(): String {
        val instant = kotlinx.datetime.Clock.System.now()
        val localDateTime = instant.toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
        
        val dayOfWeekAr = when (localDateTime.dayOfWeek) {
            kotlinx.datetime.DayOfWeek.MONDAY -> "الاثنين"
            kotlinx.datetime.DayOfWeek.TUESDAY -> "الثلاثاء"
            kotlinx.datetime.DayOfWeek.WEDNESDAY -> "الأربعاء"
            kotlinx.datetime.DayOfWeek.THURSDAY -> "الخميس"
            kotlinx.datetime.DayOfWeek.FRIDAY -> "الجمعة"
            kotlinx.datetime.DayOfWeek.SATURDAY -> "السبت"
            kotlinx.datetime.DayOfWeek.SUNDAY -> "الأحد"
            else -> ""
        }
        
        val year = localDateTime.year
        val month = localDateTime.monthNumber
        val day = localDateTime.dayOfMonth
        
        val hour24 = localDateTime.hour
        val amPm = if (hour24 >= 12) "مساءاً" else "صباحاً"
        val hour12 = if (hour24 % 12 == 0) 12 else hour24 % 12
        
        val minute = localDateTime.minute.toString().padStart(2, '0')
        val second = localDateTime.second.toString().padStart(2, '0')
        
        return "$dayOfWeekAr $day-$month-$year | $hour12:$minute:$second $amPm"
    }
}
