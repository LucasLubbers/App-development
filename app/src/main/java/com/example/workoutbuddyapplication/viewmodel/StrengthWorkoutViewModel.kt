package com.example.workoutbuddyapplication.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.workoutbuddyapplication.data.SupabaseClient
import com.example.workoutbuddyapplication.models.Workout
import com.example.workoutbuddyapplication.workout.StrengthSession
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

class StrengthWorkoutViewModel : ViewModel() {
    val session = StrengthSession()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving

    private val _saveError = MutableStateFlow<String?>(null)
    val saveError: StateFlow<String?> = _saveError

    private val _workoutNotes = MutableStateFlow("")
    val workoutNotes: StateFlow<String> = _workoutNotes

    fun setWorkoutNotes(notes: String) {
        _workoutNotes.value = notes
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun saveWorkout(
        elapsedTime: Long,
        onSuccess: (String, String) -> Unit
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
                val dateString = LocalDate.now().toString()
                val workout = Workout(
                    type = "STRENGTH",
                    date = dateString,
                    duration = durationMinutes,
                    distance = null,
                    notes = _workoutNotes.value,
                    profileId = user.id
                )
                SupabaseClient.client.postgrest.from("workouts").insert(workout)
                _isSaving.value = false
                session.stop()
                onSuccess(durationMinutes.toString(), _workoutNotes.value)
            } catch (e: Exception) {
                _saveError.value = e.message
                _isSaving.value = false
            }
        }
    }
}