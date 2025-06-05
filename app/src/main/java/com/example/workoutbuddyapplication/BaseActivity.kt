package com.example.workoutbuddyapplication

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.example.workoutbuddyapplication.ui.theme.UserPreferencesManager
import com.example.workoutbuddyapplication.util.LocaleHelper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

open class BaseActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        val preferencesManager = UserPreferencesManager(newBase)
        var language = "en" // Default language
        
        // Get the saved language synchronously
        runCatching {
            language = kotlinx.coroutines.runBlocking {
                preferencesManager.selectedLanguage.first()
            }
        }
        
        val context = LocaleHelper.setLocale(newBase, language)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize language from preferences
        lifecycleScope.launch {
            val preferencesManager = UserPreferencesManager(this@BaseActivity)
            val language = preferencesManager.selectedLanguage.first()
            LocaleHelper.setLocale(this@BaseActivity, language)
        }
    }
} 