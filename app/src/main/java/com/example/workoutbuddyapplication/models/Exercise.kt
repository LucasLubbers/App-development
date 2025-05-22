package com.example.workoutbuddyapplication.models

data class Exercise(
    val name: String,
    val force: String = "",
    val level: String,
    val mechanic: String = "",
    val equipment: String?,
    val primaryMuscles: List<String>,
    val secondaryMuscles: List<String> = emptyList(),
    val instructions: List<String>,
    val category: String
)