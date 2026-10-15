package com.Nightjar.caliq5.logic

import com.Nightjar.caliq5.data.*
import com.Nightjar.caliq5.ui.components.parseGradeToDouble
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt

/**
 * Grade calculation engine - direct Kotlin translation of caliq5.html JavaScript functions.
 * All functions are pure (no side effects) for testability.
 */
object GradeCalculator {

    /**
     * Translated from applyExemptionRounding(num) in caliq5.html.
     *
     * Rules:
     * - If decimal part >= 0.6 AND integer part is 74 → rounds to 75
     * - If decimal part >= 0.6 AND integer part is 89 → rounds to 90
     * - Otherwise: no rounding (return original)
     */
    fun applyExemptionRounding(num: Double): ExemptionRoundingResult {
        val raw = Math.round(num * 100).toDouble() / 100.0  // 2 decimal precision
        val floorVal = floor(raw).toInt()
        val decimalPart = ((raw - floorVal) * 10).roundToInt()

        var finalGrade = raw
        var isRounded = false

        if (decimalPart >= 6) {
            when (floorVal) {
                74 -> { finalGrade = 75.0; isRounded = true }
                89 -> { finalGrade = 90.0; isRounded = true }
            }
        }

        return ExemptionRoundingResult(
            rounded = finalGrade,
            original = raw,
            isRounded = isRounded,
        )
    }

    /**
     * Translated from simpleCustomRound(num) in caliq5.html.
     * Rounds up if decimal >= 0.6, otherwise rounds down.
     */
    fun simpleCustomRound(num: Double): Int {
        val raw = Math.round(num * 100).toDouble() / 100.0
        val decimalPart = ((raw - floor(raw)) * 10).roundToInt()
        return if (decimalPart >= 6) ceil(raw).toInt() else floor(raw).toInt()
    }

    /**
     * Main general exemption calculation.
     * Translated from performGeneralExemptionCalculation() in caliq5.html.
     */
    fun calculateGeneralExemption(
        entries: List<SubjectGradeEntry>,
    ): Result<ExemptionResultData> {
        var total = 0.0
        var count = 0
        val subjectsBelow75 = mutableListOf<GradeDisplayItem>()
        val individualExemptions = mutableListOf<GradeDisplayItem>()
        val allGradesForDisplay = mutableListOf<GradeDisplayItem>()

        for (entry in entries) {
            if (entry.isSkipped) {
                allGradesForDisplay.add(
                    GradeDisplayItem(
                        name = entry.name,
                        grade = null,
                        original = null,
                        isRounded = false,
                        isSkipped = true,
                    )
                )
                continue
            }

            val grade = entry.grade

            if (grade == null) {
                if (entry.term1 != null || entry.midYear != null || entry.term2 != null) {
                    return Result.failure(Exception("لا يجوز تطبيق معادلات على إدخال مستطيل واحد أو اثنين. الرجاء إدخال جميع الدرجات الثلاث (الفصل الأول، نصف السنة، الفصل الثاني) للمادة: ${entry.name} أو تركها فارغة."))
                }
                return Result.failure(Exception("يرجى إدخال درجات صحيحة (0-100) للمادة: ${entry.name}، أو اختيار \"لا تشملني\"."))
            }

            if (grade < 0 || grade > 100) {
                return Result.failure(Exception("يرجى إدخال درجات صحيحة (0-100) للمادة: ${entry.name}، أو اختيار \"لا تشملني\"."))
            }

            val exemptionData = applyExemptionRounding(grade)
            val finalGradeForCheck = exemptionData.rounded

            if (finalGradeForCheck < 75) {
                subjectsBelow75.add(
                    GradeDisplayItem(
                        name = entry.name,
                        grade = finalGradeForCheck,
                        original = exemptionData.original,
                        isRounded = exemptionData.isRounded,
                    )
                )
            }
            if (finalGradeForCheck >= 90) {
                individualExemptions.add(
                    GradeDisplayItem(
                        name = entry.name,
                        grade = finalGradeForCheck,
                        original = exemptionData.original,
                        isRounded = exemptionData.isRounded,
                    )
                )
            }

            total += grade
            count++
            allGradesForDisplay.add(
                GradeDisplayItem(
                    name = entry.name,
                    grade = finalGradeForCheck,
                    original = exemptionData.original,
                    isRounded = exemptionData.isRounded,
                )
            )
        }

        if (count == 0) {
            return Result.failure(Exception("لم يتم إدخال أي درجة صحيحة."))
        }

        val average = total / count

        // Determine exemption status (matches JS logic exactly)
        val exemptionStatus: ExemptionStatus
        val isExempt: Boolean
        val isGeneralExempt: Boolean

        val indivCount = individualExemptions.size
        val indivWord = when {
            indivCount == 1 -> "مادة واحدة"
            indivCount == 2 -> "مادتان"
            indivCount in 3..10 -> "مواد دراسية"
            else -> "مادة دراسية"
        }

        if (subjectsBelow75.isNotEmpty() || average < 85) {
            val primaryStatus = when {
                subjectsBelow75.isNotEmpty() -> "لم تحقق شرط الإعفاء العام لوجود مادة بسعي أقل من 75%."
                else -> "لم تحقق شرط الإعفاء العام لأن معدلك أقل من 85%."
            }

            if (individualExemptions.isNotEmpty()) {
                exemptionStatus = ExemptionStatus(
                    primary = primaryStatus,
                    secondary = "ولكن تهانينا! 🎉 حصلت على إعفاء فردي في $indivCount $indivWord.",
                    isGeneralExempt = false,
                )
                isExempt = true
                isGeneralExempt = false
            } else {
                exemptionStatus = ExemptionStatus(
                    primary = "$primaryStatus حظاً أوفر في الامتحانات.",
                    secondary = "",
                    isGeneralExempt = false,
                )
                isExempt = false
                isGeneralExempt = false
            }
        } else {
            exemptionStatus = ExemptionStatus(
                primary = "مبروك✨🎉🎊 الطالب معفى إعفاء عام في جميع المواد الدراسية لهذا العام ونجحت",
                secondary = "",
                isGeneralExempt = true,
            )
            isExempt = true
            isGeneralExempt = true
        }

        return Result.success(
            ExemptionResultData(
                finalAverage = average,
                isGeneralExempt = isGeneralExempt,
                isExempt = isExempt,
                exemptionStatus = exemptionStatus,
                grades = allGradesForDisplay,
                individualExemptions = individualExemptions,
                className = "",
            )
        )
    }

    /**
     * Calculates annual average (السعي السنوي) for a single subject.
     * Translated from calculateAnnualAverageResult() in caliq5.html.
     */
    fun calculateAnnualAverage(
        subjectName: String,
        term1: Double,
        midYear: Double,
        term2: Double,
    ): AnnualResultData {
        val average = (term1 + midYear + term2) / 3.0
        val rounded = simpleCustomRound(average)
        return AnnualResultData(
            subjectName = subjectName,
            finalAverage = rounded,
            realAverage = average,
            grades = listOf(
                TermGrade("الفصل الأول", term1),
                TermGrade("نصف السنة", midYear),
                TermGrade("الفصل الثاني", term2),
            )
        )
    }

    /**
     * Calculates monthly (Faṣlī) average for a subject.
     * Translated from calculateMonthlyAverage() in caliq5.html.
     */
    fun calculateMonthlyAverage(
        subjectName: String,
        monthGrades: List<Double>,
    ): MonthlyResultData {
        val total = monthGrades.sum()
        val count = monthGrades.size
        val average = total / count
        val rounded = simpleCustomRound(average)
        return MonthlyResultData(
            subjectName = subjectName,
            finalAverage = rounded,
            realAverage = average,
            grades = monthGrades.mapIndexed { i, g -> TermGrade("الشهر ${i + 1}", g) }
        )
    }

    /**
     * Calculates required Term 2 grade to achieve exemption.
     * Translated from calculateTerm2Result() in caliq5.html.
     *
     * Formula: requiredFor75 = (75 * 3) - term1 - midYear
     * Formula: requiredFor90 = (90 * 3) - term1 - midYear
     */
    fun calculateTerm2Required(
        subjName: String,
        term1: Double,
        midYear: Double,
    ): Term2ResultData {
        val requiredFor75 = (75.0 * 3) - term1 - midYear
        val requiredFor90 = (90.0 * 3) - term1 - midYear
        return Term2ResultData(
            requiredFor75 = requiredFor75,
            requiredFor90 = requiredFor90,
            subjName = subjName,
            grades = listOf(
                TermGrade("الفصل الأول", term1),
                TermGrade("نصف السنة", midYear),
            )
        )
    }

    /**
     * Checks if a student record qualifies for general exemption.
     * Translated from isStudentGenerallyExempt() in caliq5.html.
     */
    fun isStudentGenerallyExempt(
        subjects: Map<String, com.Nightjar.caliq5.data.SubjectGradeRecord>
    ): Boolean {
        var total = 0.0
        var count = 0
        var hasBelow75 = false

        subjects.values.forEach { vals ->
            if (vals.t1Val == "SKIPPED") return@forEach
            val t1Val = when (vals.t1Mode) {
                com.Nightjar.caliq5.data.TermMode.TERM -> vals.t1Val.parseGradeToDouble()
                com.Nightjar.caliq5.data.TermMode.MONTHLY -> {
                    val validMonths = vals.t1Months.mapNotNull { it.parseGradeToDouble() }
                    if (validMonths.isEmpty()) null
                    else simpleCustomRound(validMonths.sum() / validMonths.size).toDouble()
                }
            }
            val midVal = vals.midVal.parseGradeToDouble()
            val t2Val = when (vals.t2Mode) {
                com.Nightjar.caliq5.data.TermMode.TERM -> vals.t2Val.parseGradeToDouble()
                com.Nightjar.caliq5.data.TermMode.MONTHLY -> {
                    val validMonths = vals.t2Months.mapNotNull { it.parseGradeToDouble() }
                    if (validMonths.isEmpty()) null
                    else simpleCustomRound(validMonths.sum() / validMonths.size).toDouble()
                }
            }

            if (t1Val != null && midVal != null && t2Val != null) {
                val annualRaw = (t1Val + midVal + t2Val) / 3.0
                val rounded = applyExemptionRounding(annualRaw).rounded
                total += rounded
                count++
                if (rounded < 75) hasBelow75 = true
            }
        }

        val avg = if (count > 0) total / count else 0.0
        return avg >= 85 && !hasBelow75 && count > 0
    }
}
