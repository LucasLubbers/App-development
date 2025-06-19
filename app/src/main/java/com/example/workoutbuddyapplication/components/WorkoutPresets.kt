package com.example.workoutbuddyapplication.components

import com.example.workoutbuddyapplication.screens.AvailableExercise
import com.example.workoutbuddyapplication.screens.Exercise
import com.example.workoutbuddyapplication.screens.ExerciseSet
import com.example.workoutbuddyapplication.ui.theme.StringResources
import com.example.workoutbuddyapplication.ui.theme.UnitSystem

data class WorkoutPreset(
    val name: String,
    val exercises: List<Exercise>
)

fun createWorkoutPresets(
    availableExercises: List<AvailableExercise>,
    unitSystem: UnitSystem,
    strings: StringResources
): List<WorkoutPreset> {
    // Example presets, adjust as needed
    val defaultWeight = if (unitSystem == UnitSystem.IMPERIAL) 45.0 else 20.0
    return listOf(
        WorkoutPreset(
            name = "Full Body",
            exercises = listOf(
                Exercise("Bench Press", listOf(ExerciseSet(10, defaultWeight)), "Chest"),
                Exercise("Squat", listOf(ExerciseSet(10, defaultWeight)), "Legs"),
                Exercise("Deadlift", listOf(ExerciseSet(10, defaultWeight)), "Back")
            )
        ),
        WorkoutPreset(
            name = "Push Day",
            exercises = listOf(
                Exercise("Bench Press", listOf(ExerciseSet(10, defaultWeight)), "Chest"),
                Exercise("Shoulder Press", listOf(ExerciseSet(10, defaultWeight)), "Shoulders")
            )
        )
    )
}