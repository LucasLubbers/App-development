package com.example.workoutbuddyapplication.utils

import android.util.Patterns

class EmailValidator {
    fun isValid(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}