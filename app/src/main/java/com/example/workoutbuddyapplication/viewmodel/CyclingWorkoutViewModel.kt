package com.example.workoutbuddyapplication.viewmodel

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.workoutbuddyapplication.data.SupabaseClient
import com.example.workoutbuddyapplication.models.Workout
import com.example.workoutbuddyapplication.ui.theme.UserPreferencesManager
import com.example.workoutbuddyapplication.workout.CyclingSession
import com.example.workoutbuddyapplication.ui.theme.UnitSystem
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CyclingWorkoutViewModel(
    context: Context,
    preferencesManager: UserPreferencesManager,
    debugMode: Boolean
) : ViewModel() {
    val session = CyclingSession(context, preferencesManager, debugMode)

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving

    private val _saveError = MutableStateFlow<String?>(null)
    val saveError: StateFlow<String?> = _saveError

    private val _workoutNotes = MutableStateFlow("")
    val workoutNotes: StateFlow<String> = _workoutNotes

    private val _targetDistance = MutableStateFlow(0.0)
    val targetDistance: StateFlow<Double> = _targetDistance

    fun setWorkoutNotes(notes: String) {
        _workoutNotes.value = notes
    }

    fun setTargetDistance(target: Double) {
        session.setTargetDistance(target)
        _targetDistance.value = target
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun saveWorkout(
        elapsedTime: Long,
        distance: Double,
        calories: Int,
        unitSystem: UnitSystem,
        onSuccess: (String, String, Int, String) -> Unit
    ) {
        viewModelScope.launch {
            _isSaving.value = true
            _saveError.value = null
            try {
                val user = SupabaseClient.client.auth.currentUserOrNull()
                if (user == null) {
                    _saveError.value = "User not logged in"
                    _isSaving.value = false
                    return@launch
                }
                val durationMinutes = (elapsedTime / 60000).toInt()
                val dateString = java.time.LocalDate.now().toString()
                val workout = Workout(
                    type = "CYCLING",
                    date = dateString,
                    duration = durationMinutes,
                    distance = distance,
                    notes = _workoutNotes.value,
                    profileId = user.id
                )
                SupabaseClient.client.postgrest.from("workouts").insert(workout)
                _isSaving.value = false
                session.stop()
                onSuccess(
                    com.example.workoutbuddyapplication.screens.formatTime(elapsedTime),
                    com.example.workoutbuddyapplication.utils.UnitConverter.formatDistance(
                        distance,
                        unitSystem
                    ),
                    calories,
                    _workoutNotes.value
                )
            } catch (e: Exception) {
                _saveError.value = e.message
                _isSaving.value = false
            }
        }
    }
}