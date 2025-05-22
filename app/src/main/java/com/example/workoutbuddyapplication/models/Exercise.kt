package com.example.workoutbuddyapplication.models

data class Exercise(
    val id: String? = null,
    val name: String,
    val force: String? = null,
    val level: String,
    val mechanic: String? = null,
    val equipment: String? = null,
    val primaryMuscles: List<String>,
    val secondaryMuscles: List<String>? = emptyList(),
    val category: String,
    val instructions: List<String>
) 