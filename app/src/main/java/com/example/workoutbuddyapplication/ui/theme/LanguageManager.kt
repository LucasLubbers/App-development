package com.example.workoutbuddyapplication.ui.theme

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.languageDataStore by preferencesDataStore(name = "language_settings")

class LanguageManager(private val context: Context) {
    private val LANGUAGE_KEY = stringPreferencesKey("selected_language")

    val selectedLanguage: Flow<String> = context.languageDataStore.data.map { preferences ->
        preferences[LANGUAGE_KEY] ?: "nl" // Default to Dutch
    }

    suspend fun setLanguage(language: String) {
        context.languageDataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = language
        }
    }
} 