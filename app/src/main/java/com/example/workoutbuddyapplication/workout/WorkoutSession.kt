package com.example.workoutbuddyapplication.workout

import com.example.workoutbuddyapplication.utils.LatLng
import kotlinx.coroutines.flow.StateFlow

interface WorkoutSession {
    val isActive: StateFlow<Boolean>
    val distance: StateFlow<Double>
    val elapsedTime: StateFlow<Long>
    val calories: StateFlow<Int>
    val speed: StateFlow<Double>
    val routePoints: StateFlow<List<LatLng>>
    fun start()
    fun pause()
    fun stop()
    fun setTargetDistance(target: Double)
}