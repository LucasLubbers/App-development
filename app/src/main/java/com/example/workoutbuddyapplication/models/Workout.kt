package com.example.workoutbuddyapplication.models

import java.time.LocalDate

data class Workout(
    val id: Int,
    val type: WorkoutType,
    val date: LocalDate,
    val duration: Int, // in minutes
    val distance: Double?, // in kilometers
    val notes: String?
)

enum class WorkoutType(val displayName: String) {
    RUNNING("Hardlopen"),
    CYCLING("Fietsen"),
    STRENGTH("Krachttraining"),
    YOGA("Yoga"),
    OTHER("Overig")
}