package com.example.workoutbuddyapplication.workout

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.example.workoutbuddyapplication.utils.LatLng
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicBoolean

class StrengthSession : WorkoutSession {
    private val _isActive = MutableStateFlow(false)
    override val isActive: StateFlow<Boolean> = _isActive

    private val _distance = MutableStateFlow(0.0)
    override val distance: StateFlow<Double> = _distance

    private val _elapsedTime = MutableStateFlow(0L)
    override val elapsedTime: StateFlow<Long> = _elapsedTime

    private val _calories = MutableStateFlow(0)
    override val calories: StateFlow<Int> = _calories

    private val _speed = MutableStateFlow(0.0)
    override val speed: StateFlow<Double> = _speed

    private val _routePoints = MutableStateFlow<List<LatLng>>(emptyList())
    override val routePoints: StateFlow<List<LatLng>> = _routePoints

    private var sessionJob: Job? = null
    private var startTime: Long = 0L
    private var pausedTime: Long = 0L
    private var totalPausedDuration: Long = 0L
    private val isPaused = AtomicBoolean(false)

    override fun start() {
        if (_isActive.value) return
        _isActive.value = true
        isPaused.set(false)
        startTime = System.currentTimeMillis() - _elapsedTime.value
        sessionJob = CoroutineScope(Dispatchers.Default).launch {
            while (_isActive.value && !isPaused.get()) {
                _elapsedTime.value = System.currentTimeMillis() - startTime - totalPausedDuration
                delay(1000)
            }
        }
    }

    override fun pause() {
        if (!_isActive.value || isPaused.get()) return
        isPaused.set(true)
        pausedTime = System.currentTimeMillis()
        sessionJob?.cancel()
    }

    override fun stop() {
        _isActive.value = false
        sessionJob?.cancel()
        sessionJob = null
        isPaused.set(false)
        totalPausedDuration = 0L
        pausedTime = 0L
    }

    override fun setTargetDistance(target: Double) {
        // Not used for strength workouts
    }

    fun addExerciseCalories(caloriesToAdd: Int) {
        _calories.value += caloriesToAdd
    }
}