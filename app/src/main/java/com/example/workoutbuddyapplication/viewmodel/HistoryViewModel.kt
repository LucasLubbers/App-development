package com.example.workoutbuddyapplication.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.workoutbuddyapplication.models.Workout
import com.example.workoutbuddyapplication.models.WorkoutType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

class HistoryViewModel() : ViewModel() {
    private val _workouts = MutableStateFlow<List<Workout>>(emptyList())
    val workouts: StateFlow<List<Workout>> = _workouts

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _selectedType = MutableStateFlow<WorkoutType?>(null)
    val selectedType: StateFlow<WorkoutType?> = _selectedType

    private val _showCalendarView = MutableStateFlow(false)
    val showCalendarView: StateFlow<Boolean> = _showCalendarView

    @RequiresApi(Build.VERSION_CODES.O)
    private val _calendarMonth = MutableStateFlow(LocalDate.now().withDayOfMonth(1))

    @RequiresApi(Build.VERSION_CODES.O)
    val calendarMonth: StateFlow<LocalDate> = _calendarMonth

    fun fetchWorkouts(userId: String, fetcher: suspend (String) -> List<Workout>) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val result = fetcher(userId)
                if (result.isNotEmpty()) {
                    _workouts.value = result
                } else {
                    _error.value = "No workouts found or failed to fetch."
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
            _isLoading.value = false
        }
    }

    fun setSelectedType(type: WorkoutType?) {
        _selectedType.value = type
    }

    fun toggleCalendarView() {
        _showCalendarView.value = !_showCalendarView.value
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun setCalendarMonth(month: LocalDate) {
        _calendarMonth.value = month
    }
}