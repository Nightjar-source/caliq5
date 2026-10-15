package com.Nightjar.caliq5.viewmodel

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.Nightjar.caliq5.CaliqApplication
import com.Nightjar.caliq5.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "caliq_prefs")

/**
 * Android implementation of DataRepository using DataStore.
 * Provides persistent storage for drafts, history, and student records.
 * All I/O and JSON serialization operations are dispatched to Dispatchers.IO for 100% background execution.
 */
actual object DataRepository {

    private val context: Context get() = CaliqApplication.appContext
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private val HISTORY_KEY = stringPreferencesKey("history_v1")
    private val STUDENTS_KEY = stringPreferencesKey("students_v1")
    private val EXEMPTION_DRAFTS_PREFIX = "exemption_draft_"

    actual suspend fun saveHistory(items: List<HistoryItem>): Unit = withContext(Dispatchers.IO) {
        val payload = json.encodeToString(items)
        context.dataStore.edit { prefs ->
            prefs[HISTORY_KEY] = payload
        }
        Unit
    }

    actual suspend fun loadHistory(): List<HistoryItem> = withContext(Dispatchers.IO) {
        try {
            val prefs = context.dataStore.data.first()
            val raw = prefs[HISTORY_KEY] ?: return@withContext emptyList()
            json.decodeFromString(raw)
        } catch (e: Exception) {
            emptyList()
        }
    }

    actual suspend fun saveStudents(students: List<StudentRecord>): Unit = withContext(Dispatchers.IO) {
        val payload = json.encodeToString(students)
        context.dataStore.edit { prefs ->
            prefs[STUDENTS_KEY] = payload
        }
        Unit
    }

    actual suspend fun loadStudents(): List<StudentRecord> = withContext(Dispatchers.IO) {
        try {
            val prefs = context.dataStore.data.first()
            val raw = prefs[STUDENTS_KEY] ?: return@withContext emptyList()
            json.decodeFromString(raw)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private val CUSTOM_SUBJECTS_DRAFTS_PREFIX = "custom_subjects_draft_"
    private val MONTHLY_GRADES_KEY = stringPreferencesKey("monthly_grades_draft_v1")

    actual suspend fun saveExemptionDrafts(
        className: String,
        grades: Map<String, SubjectGradeEntry>,
    ): Unit = withContext(Dispatchers.IO) {
        val key = stringPreferencesKey("$EXEMPTION_DRAFTS_PREFIX$className")
        val payload = json.encodeToString(grades)
        context.dataStore.edit { prefs ->
            prefs[key] = payload
        }
        Unit
    }

    actual suspend fun loadExemptionDrafts(className: String): Map<String, SubjectGradeEntry> = withContext(Dispatchers.IO) {
        try {
            val key = stringPreferencesKey("$EXEMPTION_DRAFTS_PREFIX$className")
            val prefs = context.dataStore.data.first()
            val raw = prefs[key] ?: return@withContext emptyMap()
            json.decodeFromString(raw)
        } catch (e: Exception) {
            emptyMap()
        }
    }

    actual suspend fun saveCustomSubjectsDrafts(
        className: String,
        subjects: List<SubjectGradeEntry>,
    ): Unit = withContext(Dispatchers.IO) {
        val key = stringPreferencesKey("$CUSTOM_SUBJECTS_DRAFTS_PREFIX$className")
        val payload = json.encodeToString(subjects)
        context.dataStore.edit { prefs ->
            prefs[key] = payload
        }
        Unit
    }

    actual suspend fun loadCustomSubjectsDrafts(className: String): List<SubjectGradeEntry> = withContext(Dispatchers.IO) {
        try {
            val key = stringPreferencesKey("$CUSTOM_SUBJECTS_DRAFTS_PREFIX$className")
            val prefs = context.dataStore.data.first()
            val raw = prefs[key] ?: return@withContext emptyList()
            json.decodeFromString(raw)
        } catch (e: Exception) {
            emptyList()
        }
    }

    actual suspend fun saveMonthlyGrades(grades: List<MonthlyGrade>): Unit = withContext(Dispatchers.IO) {
        val payload = json.encodeToString(grades)
        context.dataStore.edit { prefs ->
            prefs[MONTHLY_GRADES_KEY] = payload
        }
        Unit
    }

    actual suspend fun loadMonthlyGrades(): List<MonthlyGrade> = withContext(Dispatchers.IO) {
        try {
            val prefs = context.dataStore.data.first()
            val raw = prefs[MONTHLY_GRADES_KEY] ?: return@withContext listOf(MonthlyGrade(0, ""), MonthlyGrade(1, ""))
            json.decodeFromString(raw)
        } catch (e: Exception) {
            listOf(MonthlyGrade(0, ""), MonthlyGrade(1, ""))
        }
    }

    actual suspend fun savePreference(key: String, value: String): Unit = withContext(Dispatchers.IO) {
        val prefKey = stringPreferencesKey(key)
        context.dataStore.edit { prefs ->
            prefs[prefKey] = value
        }
        Unit
    }

    actual suspend fun loadPreference(key: String): String? = withContext(Dispatchers.IO) {
        try {
            val prefKey = stringPreferencesKey(key)
            val prefs = context.dataStore.data.first()
            prefs[prefKey]
        } catch (e: Exception) {
            null
        }
    }

    private val SETTINGS_KEY = stringPreferencesKey("settings_v1")

    actual suspend fun saveSettings(settings: AppSettings): Unit = withContext(Dispatchers.IO) {
        val payload = json.encodeToString(settings)
        context.dataStore.edit { prefs ->
            prefs[SETTINGS_KEY] = payload
        }
        Unit
    }

    actual suspend fun loadSettings(): AppSettings = withContext(Dispatchers.IO) {
        try {
            val prefs = context.dataStore.data.first()
            val raw = prefs[SETTINGS_KEY] ?: return@withContext AppSettings()
            json.decodeFromString(raw)
        } catch (e: Exception) {
            AppSettings()
        }
    }
}
