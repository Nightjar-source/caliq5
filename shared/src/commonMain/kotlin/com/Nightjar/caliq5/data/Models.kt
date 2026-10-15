package com.Nightjar.caliq5.data

import kotlinx.serialization.Serializable

/**
 * Represents the exemption rounding result of a single grade.
 * Translated directly from applyExemptionRounding() in caliq5.html.
 */
@Serializable
data class ExemptionRoundingResult(
    val rounded: Double,
    val original: Double,
    val isRounded: Boolean,
)

/**
 * Grade entry for a single subject in exemption calculation screen.
 */
@Serializable
data class SubjectGradeEntry(
    val name: String,
    val grade: Double?,       // null if skipped
    val isSkipped: Boolean,
    // Detailed input mode (term1, midYear, term2)
    val term1: Double? = null,
    val midYear: Double? = null,
    val term2: Double? = null,
    val id: String = name, // Default to name for standard subjects, but custom subjects can set a unique ID
)

/**
 * Full result data for the general exemption calculation.
 * Returned by performGeneralExemptionCalculation() equivalent in Kotlin.
 */
@Serializable
data class ExemptionResultData(
    val finalAverage: Double,
    val isGeneralExempt: Boolean,
    val isExempt: Boolean,                     // general OR individual
    val exemptionStatus: ExemptionStatus,
    val grades: List<GradeDisplayItem>,
    val individualExemptions: List<GradeDisplayItem>,
    val className: String,
)

@Serializable
data class ExemptionStatus(
    val primary: String,
    val secondary: String,
    val isGeneralExempt: Boolean,
)

@Serializable
data class GradeDisplayItem(
    val name: String,
    val grade: Double?,           // null = skipped
    val original: Double?,
    val isRounded: Boolean,
    val isSkipped: Boolean = false,
)

/**
 * Result data for annual average calculation.
 */
@Serializable
data class AnnualResultData(
    val subjectName: String,
    val finalAverage: Int,
    val realAverage: Double,
    val grades: List<TermGrade>,
)

@Serializable
data class TermGrade(
    val name: String,
    val grade: Double,
)

/**
 * Result data for monthly (Faṣlī) calculation.
 */
@Serializable
data class MonthlyResultData(
    val subjectName: String,
    val finalAverage: Int,
    val realAverage: Double,
    val grades: List<TermGrade>,
)

/**
 * Result data for Term 2 calculation (predict required grade).
 */
@Serializable
data class Term2ResultData(
    val requiredFor75: Double,
    val requiredFor90: Double,
    val subjName: String,
    val grades: List<TermGrade>,
)

/**
 * Represents any saved result in history.
 */
@Serializable
data class HistoryItem(
    val id: String,
    val type: ResultType,
    val title: String,
    val date: String,
    val data: String,     // JSON-serialized result data
)

enum class ResultType {
    EXEMPTION,
    ANNUAL,
    MONTHLY,
    TERM2,
}

// ===== My Grades Pro Data Models =====

@Serializable
data class StudentRecord(
    val id: String,
    val name: String,
    val className: String,
    val subjects: Map<String, SubjectGradeRecord>,
)

@Serializable
data class SubjectGradeRecord(
    val t1Mode: TermMode = TermMode.TERM,
    val t1Val: String = "",
    val t1Months: List<String> = listOf("", ""),
    val midVal: String = "",
    val t2Mode: TermMode = TermMode.TERM,
    val t2Val: String = "",
    val t2Months: List<String> = listOf("", ""),
    val finalVal: String = "",
)

@Serializable
enum class TermMode {
    TERM, MONTHLY
}

@Serializable
data class AppSettings(
    val theme: String = "system",
    val pureBlackMode: Boolean = false,
    val pureWhiteMode: Boolean = false,
    val useMaterialYou: Boolean = false,
    val primaryColor: String = "#ec4899",
)
