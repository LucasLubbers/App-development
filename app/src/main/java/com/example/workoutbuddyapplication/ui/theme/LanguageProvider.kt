package com.example.workoutbuddyapplication.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
fun LanguageProvider(
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val preferencesManager = remember { UserPreferencesManager(context) }
    val selectedLanguage by preferencesManager.selectedLanguage.collectAsState(initial = "nl")

    // Load preferences from Supabase profile when the app starts
    LaunchedEffect(Unit) {
        preferencesManager.loadPreferencesFromProfile()
    }

    androidx.compose.runtime.CompositionLocalProvider(
        LocalStringResources provides if (selectedLanguage == "nl") dutchStrings else englishStrings
    ) {
        content()
    }
} 