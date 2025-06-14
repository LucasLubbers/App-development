package com.example.workoutbuddyapplication.ui.theme

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import com.example.workoutbuddyapplication.screens.getUserId
import com.example.workoutbuddyapplication.screens.updateUserLanguage
import com.example.workoutbuddyapplication.screens.updateUserUnitSystem
import com.example.workoutbuddyapplication.screens.fetchUserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import androidx.datastore.preferences.core.doublePreferencesKey

class UserPreferencesManager(private val context: Context) {
    
    companion object {
        private val LANGUAGE_KEY = stringPreferencesKey("user_language")
        private val UNIT_SYSTEM_KEY = stringPreferencesKey("user_unit_system")
        private val DEBUG_MODE_KEY = booleanPreferencesKey("debug_mode")
        private val WEIGHT_KEY = doublePreferencesKey("user_weight")

    }

    val selectedLanguage: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[LANGUAGE_KEY] ?: "nl"
    }

    val selectedUnitSystem: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[UNIT_SYSTEM_KEY] ?: "metric"
    }

    val debugMode: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[DEBUG_MODE_KEY] ?: false
    }

    suspend fun setLanguage(language: String) {
        // Update local storage
        context.dataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = language
        }
        
        // Update Supabase profile
        try {
            val userId = getUserId(context)
            if (userId != null) {
                updateUserLanguage(userId, language)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun setUnitSystem(unitSystem: String) {
        // Update local storage
        context.dataStore.edit { preferences ->
            preferences[UNIT_SYSTEM_KEY] = unitSystem
        }
        
        // Update Supabase profile
        try {
            val userId = getUserId(context)
            if (userId != null) {
                updateUserUnitSystem(userId, unitSystem)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun loadPreferencesFromProfile() {
        try {
            val userId = getUserId(context)
            if (userId != null) {
                val profile = fetchUserProfile(userId)
                profile?.let {
                    // Update local storage with profile values
                    context.dataStore.edit { preferences ->
                        preferences[LANGUAGE_KEY] = it.language ?: "nl"
                        preferences[UNIT_SYSTEM_KEY] = it.unitSystem ?: "metric"
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun setDebugMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DEBUG_MODE_KEY] = enabled
        }
    }

    suspend fun saveUserWeight(weight: Double) {
        context.dataStore.edit { prefs ->
            prefs[WEIGHT_KEY] = weight
        }
    }

    suspend fun getUserWeight(): Double {
        return context.dataStore.data
            .map { it[WEIGHT_KEY] ?: 70.0 }
            .first()
    }
} 