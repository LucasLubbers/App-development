package com.example.workoutbuddyapplication.models

import kotlinx.serialization.Serializable

@Serializable
data class ExerciseDTO(
    val id: String? = null,
    val name: String,
    val force: String? = null,
    val level: String? = null,
    val mechanic: String? = null,
    val equipment: String? = null,
    val primary_muscles: List<String>,
    val secondary_muscles: List<String>? = null,
    val category: String,
    val instructions: List<String>,
    val created_at: String? = null,
    val updated_at: String? = null
) 