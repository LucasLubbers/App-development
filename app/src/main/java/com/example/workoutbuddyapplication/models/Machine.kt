package com.example.workoutbuddyapplication.models

import kotlinx.serialization.Serializable

@Serializable
data class Machine(
    val id: Long,
    val name: String,
    val exercise: String,
    val image: String? = null
)