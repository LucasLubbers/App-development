package com.example.workoutbuddyapplication.services

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SensorService(context: Context) {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val stepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

    private val _steps = MutableStateFlow(0)
    val steps: StateFlow<Int> = _steps.asStateFlow()

    private val _acceleration = MutableStateFlow(0f)
    val acceleration: StateFlow<Float> = _acceleration.asStateFlow()

    private val _isTracking = MutableStateFlow(false)
    val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()

    private var initialSteps: Int? = null

    private val accelerometerListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]

                // Calculate the magnitude of acceleration
                val acceleration = Math.sqrt((x * x + y * y + z * z).toDouble()).toFloat()
                _acceleration.value = acceleration
            }
        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
            // Not needed for this implementation
        }
    }

    private val stepCounterListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            if (event.sensor.type == Sensor.TYPE_STEP_COUNTER) {
                val totalSteps = event.values[0].toInt()

                if (initialSteps == null) {
                    initialSteps = totalSteps
                }

                initialSteps?.let {
                    _steps.value = totalSteps - it
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
            // Not needed for this implementation
        }
    }

    fun startTracking() {
        if (!_isTracking.value) {
            registerSensors()
            _isTracking.value = true
        }
    }

    fun stopTracking() {
        if (_isTracking.value) {
            unregisterSensors()
            _isTracking.value = false
            initialSteps = null
        }
    }

    private fun registerSensors() {
        accelerometer?.let {
            sensorManager.registerListener(
                accelerometerListener,
                it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }

        stepCounter?.let {
            sensorManager.registerListener(
                stepCounterListener,
                it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    private fun unregisterSensors() {
        sensorManager.unregisterListener(accelerometerListener)
        sensorManager.unregisterListener(stepCounterListener)
    }
}
