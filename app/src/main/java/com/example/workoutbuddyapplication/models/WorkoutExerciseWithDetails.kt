package com.example.workoutbuddyapplication.models

data class WorkoutExerciseWithDetails(
    val exercise: Exercise,
    val sets: Int?,
    val reps: Int?,
    val weight: Double?,
    val notes: String?,
    val restTime: Int?
)