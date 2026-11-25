package com.Nightjar.caliq5.viewmodel

import com.Nightjar.caliq5.data.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private fun storageGet(key: String): String? = js("window.localStorage.getItem(key)")
private fun storageSet(key: String, value: String): Unit = js("window.localStorage.setItem(key, value)")

actual object DataRepository {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    actual var cachedSettings: AppSettings? = null

    private inline fun <reified T> writeJson(key: String, value: T) {
        runCatching {
            storageSet("caliq5_$key", json.encodeToString(value))
        }
    }

    private inline fun <reified T> readJson(key: String, defaultValue: T): T {
        return runCatching {
            val raw = storageGet("caliq5_$key")
            if (!raw.isNullOrBlank()) json.decodeFromString<T>(raw) else defaultValue
        }.getOrDefault(defaultValue)
    }

    actual suspend fun saveHistory(items: List<HistoryItem>) = writeJson("history", items)
    actual suspend fun loadHistory(): List<HistoryItem> = readJson("history", emptyList())

    actual suspend fun saveStudents(students: List<StudentRecord>) = writeJson("students", students)
    actual suspend fun loadStudents(): List<StudentRecord> = readJson("students", emptyList())

    actual suspend fun saveExemptionDrafts(className: String, grades: Map<String, SubjectGradeEntry>) =
        writeJson("exemption_draft_${className.hashCode()}", grades)

    actual suspend fun loadExemptionDrafts(className: String): Map<String, SubjectGradeEntry> =
        readJson("exemption_draft_${className.hashCode()}", emptyMap())

    actual suspend fun saveCustomSubjectsDrafts(className: String, subjects: List<SubjectGradeEntry>) =
        writeJson("custom_subjects_${className.hashCode()}", subjects)

    actual suspend fun loadCustomSubjectsDrafts(className: String): List<SubjectGradeEntry> =
        readJson("custom_subjects_${className.hashCode()}", emptyList())

    actual suspend fun saveMonthlyGrades(grades: List<MonthlyGrade>) = writeJson("monthly", grades)
    actual suspend fun loadMonthlyGrades(): List<MonthlyGrade> = readJson("monthly", emptyList())

    actual suspend fun savePreference(key: String, value: String) =
        writeJson("pref_${key.hashCode()}", value)

    actual suspend fun loadPreference(key: String): String? =
        runCatching {
            val raw = storageGet("caliq5_pref_${key.hashCode()}")
            if (!raw.isNullOrBlank()) json.decodeFromString<String>(raw) else null
        }.getOrNull()

    actual suspend fun saveSettings(settings: AppSettings) {
        cachedSettings = settings
        writeJson("settings", settings)
    }

    actual suspend fun loadSettings(): AppSettings {
        val loaded = readJson("settings", AppSettings())
        cachedSettings = loaded
        return loaded
    }
}
