package com.Nightjar.caliq5.viewmodel

import com.Nightjar.caliq5.data.*

/**
 * Platform-agnostic repository interface for persistence.
 * Actual implementation is in androidMain using DataStore.
 */
expect object DataRepository {
    suspend fun saveHistory(items: List<HistoryItem>)
    suspend fun loadHistory(): List<HistoryItem>

    suspend fun saveStudents(students: List<StudentRecord>)
    suspend fun loadStudents(): List<StudentRecord>

    suspend fun saveExemptionDrafts(className: String, grades: Map<String, SubjectGradeEntry>)
    suspend fun loadExemptionDrafts(className: String): Map<String, SubjectGradeEntry>

    suspend fun saveCustomSubjectsDrafts(className: String, subjects: List<SubjectGradeEntry>)
    suspend fun loadCustomSubjectsDrafts(className: String): List<SubjectGradeEntry>

    suspend fun saveMonthlyGrades(grades: List<MonthlyGrade>)
    suspend fun loadMonthlyGrades(): List<MonthlyGrade>

    suspend fun savePreference(key: String, value: String)
    suspend fun loadPreference(key: String): String?

    var cachedSettings: AppSettings?

    suspend fun saveSettings(settings: AppSettings)
    suspend fun loadSettings(): AppSettings
}
