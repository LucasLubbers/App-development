package com.example.workoutbuddyapplication.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.workoutbuddyapplication.models.Workout
import com.example.workoutbuddyapplication.data.WorkoutRepository
import com.example.workoutbuddyapplication.utils.StatsCalculator
import com.example.workoutbuddyapplication.utils.StatsResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

class StatsViewModel(
    private val repository: WorkoutRepository
) : ViewModel() {
    private val _workouts = MutableStateFlow<List<Workout>>(emptyList())
    val workouts: StateFlow<List<Workout>> = _workouts

    private val _stats = MutableStateFlow<StatsResult?>(null)
    val stats: StateFlow<StatsResult?> = _stats

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    @RequiresApi(Build.VERSION_CODES.O)
    fun loadStats() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val data = repository.getAllWorkoutsForStats()
                _workouts.value = data
                _stats.value = StatsCalculator.calculate(data, LocalDate.now())
            } catch (e: Exception) {
                _error.value = "Failed to load stats: ${e.message}"
            }
            _isLoading.value = false
        }
    }
}