package com.example.workoutbuddyapplication.models

data class ExerciseDevice(
    val name: String,
    val id: String,
    val type: String,
    val settings: Map<String, String> = emptyMap()
)