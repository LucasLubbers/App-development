package com.example.workoutbuddyapplication.ui.theme

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_preferences")

enum class UnitSystem {
    METRIC, IMPERIAL
}

class ThemeManager(private val context: Context) {
    companion object {
        val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
        val IMPERIAL_UNITS_KEY = booleanPreferencesKey("imperial_units")
    }

    val isDarkMode: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[DARK_MODE_KEY] ?: false
        }

    val isImperialUnits: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[IMPERIAL_UNITS_KEY] ?: false
        }

    val unitSystem: Flow<UnitSystem> = isImperialUnits.map { isImperial ->
        if (isImperial) UnitSystem.IMPERIAL else UnitSystem.METRIC
    }

    suspend fun setDarkMode(isDark: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DARK_MODE_KEY] = isDark
        }
    }

    suspend fun setImperialUnits(useImperial: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IMPERIAL_UNITS_KEY] = useImperial
        }
    }
} 