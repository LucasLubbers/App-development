package com.example.workoutbuddyapplication.models

data class WorkoutExerciseWithDetails(
    val exercise: Exercise,
    val sets: Int?,
    val weight: Double?,
    val notes: String?,
    val restTime: Int?
)