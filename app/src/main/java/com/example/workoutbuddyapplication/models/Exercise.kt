package com.example.workoutbuddyapplication.models

data class Exercise(
    val name: String,
    val level: String,
    val equipment: String?,
    val primaryMuscles: List<String>,
    val category: String,
    val instructions: List<String>
) 