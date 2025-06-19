package com.example.workoutbuddyapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.workoutbuddyapplication.data.WorkoutRepository
import com.example.workoutbuddyapplication.models.Workout
import com.example.workoutbuddyapplication.models.WorkoutExerciseWithDetails
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class WorkoutDetailViewModel(
    private val repository: WorkoutRepository
) : ViewModel() {
    private val _workout = MutableStateFlow<Workout?>(null)
    val workout: StateFlow<Workout?> = _workout

    private val _exercises = MutableStateFlow<List<WorkoutExerciseWithDetails>>(emptyList())
    val exercises: StateFlow<List<WorkoutExerciseWithDetails>> = _exercises

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadWorkoutDetails(workoutId: Int, errorMsg: String) {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                _workout.value = repository.getWorkoutById(workoutId)
                _exercises.value = repository.getExercisesForWorkout(workoutId)
            } catch (e: Exception) {
                _error.value = errorMsg
            }
            _isLoading.value = false
        }
    }
}