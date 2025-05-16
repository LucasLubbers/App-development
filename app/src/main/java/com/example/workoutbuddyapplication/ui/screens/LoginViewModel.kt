package com.example.workoutbuddyapplication.ui.screens

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class LoginViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun updateEmail(email: String) {
        _uiState.update { it.copy(email = email) }
    }

    fun updatePassword(password: String) {
        _uiState.update { it.copy(password = password) }
    }

    fun login(): Boolean {
        val currentState = _uiState.value

        if (currentState.email.isBlank() || currentState.password.isBlank()) {
            return false
        }

        // Here you would typically call your authentication service
        // For now, we'll just return true if fields are not empty

        return true
    }

    fun resetPassword() {
        // Implement password reset logic
        // This would typically navigate to a password reset screen
        // or trigger a password reset email
    }

    fun navigateToRegister() {
        // Implement navigation to registration screen
        // This would typically use a navigation controller
    }
}