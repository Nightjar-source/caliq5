package com.Nightjar.caliq5.viewmodel

import com.Nightjar.caliq5.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

actual object DataRepository {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    private val storageDir: File by lazy {
        File(System.getProperty("user.home"), ".caliq5").apply { mkdirs() }
    }

    actual var cachedSettings: AppSettings? = null

    private suspend inline fun <reified T> writeJson(fileName: String, value: T) {
        withContext(Dispatchers.IO) {
            runCatching {
                File(storageDir, fileName).writeText(json.encodeToString(value), Charsets.UTF_8)
            }
        }
    }

    private suspend inline fun <reified T> readJson(fileName: String, defaultValue: T): T {
        return withContext(Dispatchers.IO) {
            runCatching {
                val file = File(storageDir, fileName)
                if (file.exists()) json.decodeFromString<T>(file.readText(Charsets.UTF_8)) else defaultValue
            }.getOrDefault(defaultValue)
        }
    }

    actual suspend fun saveHistory(items: List<HistoryItem>) = writeJson("history.json", items)
    actual suspend fun loadHistory(): List<HistoryItem> = readJson("history.json", emptyList())

    actual suspend fun saveStudents(students: List<StudentRecord>) = writeJson("students.json", students)
    actual suspend fun loadStudents(): List<StudentRecord> = readJson("students.json", emptyList())

    actual suspend fun saveExemptionDrafts(className: String, grades: Map<String, SubjectGradeEntry>) =
        writeJson("exemption_draft_${className.hashCode()}.json", grades)

    actual suspend fun loadExemptionDrafts(className: String): Map<String, SubjectGradeEntry> =
        readJson("exemption_draft_${className.hashCode()}.json", emptyMap())

    actual suspend fun saveCustomSubjectsDrafts(className: String, subjects: List<SubjectGradeEntry>) =
        writeJson("custom_subjects_${className.hashCode()}.json", subjects)

    actual suspend fun loadCustomSubjectsDrafts(className: String): List<SubjectGradeEntry> =
        readJson("custom_subjects_${className.hashCode()}.json", emptyList())

    actual suspend fun saveMonthlyGrades(grades: List<MonthlyGrade>) = writeJson("monthly.json", grades)
    actual suspend fun loadMonthlyGrades(): List<MonthlyGrade> = readJson("monthly.json", emptyList())

    actual suspend fun savePreference(key: String, value: String) =
        writeJson("pref_${key.hashCode()}.json", value)

    actual suspend fun loadPreference(key: String): String? =
        withContext(Dispatchers.IO) {
            runCatching {
                val file = File(storageDir, "pref_${key.hashCode()}.json")
                if (file.exists()) json.decodeFromString<String>(file.readText(Charsets.UTF_8)) else null
            }.getOrNull()
        }

    actual suspend fun saveSettings(settings: AppSettings) {
        cachedSettings = settings
        writeJson("settings.json", settings)
    }

    actual suspend fun loadSettings(): AppSettings {
        val loaded = readJson("settings.json", AppSettings())
        cachedSettings = loaded
        return loaded
    }
}
