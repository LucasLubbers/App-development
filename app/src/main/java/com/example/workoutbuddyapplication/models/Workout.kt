package com.example.workoutbuddyapplication.models

import java.time.LocalDate

data class Workout(
    val id: Int,
    val type: String, // Changed from WorkoutType to String
    val date: LocalDate,
    val duration: String,
    val distance: Double?,
    val notes: String?
)

enum class WorkoutType(val displayName: String) {
    RUNNING("Hardlopen"),
    CYCLING("Fietsen"),
    STRENGTH("Krachttraining"),
    YOGA("Yoga"),
    OTHER("Overig")
}