package com.example.workoutbuddyapplication.workout

import android.content.Context
import android.os.SystemClock
import com.example.workoutbuddyapplication.ui.theme.UserPreferencesManager
import com.example.workoutbuddyapplication.utils.LatLng
import com.example.workoutbuddyapplication.utils.LocationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

class CyclingSession(
    context: Context,
    private val preferencesManager: UserPreferencesManager,
    private val debugMode: Boolean = false
) : WorkoutSession {
    override val isActive = MutableStateFlow(false)
    override val distance = MutableStateFlow(0.0)
    override val elapsedTime = MutableStateFlow(0L)
    override val calories = MutableStateFlow(0)
    override val speed = MutableStateFlow(0.0)
    override val routePoints = MutableStateFlow<List<LatLng>>(emptyList())
    private var targetDistance: Double = 0.0

    private var currentLocation: LatLng? = null
    private var totalDistanceFromGPS = 0.0
    private var caloriesAccum = 0.0
    private var timerJob: Job? = null
    private var locationJob: Job? = null
    private val locationManager = LocationManager(context)
    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    override fun start() {
        if (isActive.value) return
        isActive.value = true
        startTimer()
        startLocationUpdates()
    }

    override fun pause() {
        isActive.value = false
        timerJob?.cancel()
        locationJob?.cancel()
        locationManager.stopLocationUpdates()
    }

    override fun stop() {
        pause()
    }

    override fun setTargetDistance(target: Double) {
        targetDistance = target
    }

    private fun startTimer() {
        val startTime = SystemClock.elapsedRealtime() - elapsedTime.value
        timerJob = coroutineScope.launch {
            val userWeight = preferencesManager.getUserWeight()
            while (isActive.value) {
                elapsedTime.value = SystemClock.elapsedRealtime() - startTime
                delay(1000)

                if (debugMode && currentLocation != null) {
                    distance.value += 0.0008f
                    val lastLocation = routePoints.value.lastOrNull() ?: currentLocation!!
                    val angle = Math.random() * 2 * Math.PI
                    val movementDistance = 0.00001 + Math.random() * 0.00001
                    val newLat = lastLocation.latitude + (cos(angle) * movementDistance)
                    val newLng = lastLocation.longitude + (sin(angle) * movementDistance)
                    val amsterdamLat = 52.3676
                    val amsterdamLng = 4.9041
                    val maxDistance = 0.01
                    val finalLat = if (kotlin.math.abs(newLat - amsterdamLat) > maxDistance) {
                        amsterdamLat + (if (newLat > amsterdamLat) maxDistance else -maxDistance)
                    } else newLat
                    val finalLng = if (kotlin.math.abs(newLng - amsterdamLng) > maxDistance) {
                        amsterdamLng + (if (newLng > amsterdamLng) maxDistance else -maxDistance)
                    } else newLng
                    val newLocation = LatLng(finalLat, finalLng)
                    currentLocation = newLocation
                    routePoints.value = routePoints.value + newLocation

                    val minutesElapsed = elapsedTime.value / 60000.0
                    val expectedCalories = (minutesElapsed * (12 + Math.random() * 3)).toInt()
                    if (calories.value < expectedCalories) {
                        calories.value = expectedCalories
                    }
                }

                if (distance.value > 0 && elapsedTime.value > 0) {
                    val hours = elapsedTime.value / 3_600_000.0
                    speed.value = distance.value / hours
                } else {
                    speed.value = 0.0
                }

                val met = when {
                    speed.value < 4 -> 0.0
                    speed.value < 8 -> 4.0
                    speed.value < 16 -> 6.8
                    speed.value < 20 -> 8.0
                    speed.value < 22 -> 10.0
                    speed.value < 25 -> 12.0
                    else -> 15.8
                }
                if (met > 0) {
                    val caloriesPerSecond = met * userWeight / 3600.0
                    caloriesAccum += caloriesPerSecond
                    calories.value = caloriesAccum.toInt()
                }
            }
        }
    }

    private fun startLocationUpdates() {
        if (debugMode) {
            val mockLocation = LatLng(52.3676, 4.9041)
            currentLocation = mockLocation
            routePoints.value = listOf(mockLocation)
            return
        }
        locationManager.startLocationUpdates()
        locationJob = coroutineScope.launch {
            locationManager.locationUpdates.collect { newLocation ->
                currentLocation = newLocation
                if (routePoints.value.isNotEmpty()) {
                    val lastPoint = routePoints.value.last()
                    val distanceToAdd =
                        LocationManager.calculateDistance(lastPoint, newLocation) / 1000.0
                    totalDistanceFromGPS += distanceToAdd
                    distance.value = totalDistanceFromGPS
                }
                routePoints.value = routePoints.value + newLocation
            }
        }
    }
}