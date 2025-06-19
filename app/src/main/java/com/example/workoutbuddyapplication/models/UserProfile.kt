package com.example.workoutbuddyapplication.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val name: String?,
    val email: String,
    @SerialName("picture")
    val pictureUrl: String?,
    val language: String? = "nl",
    @SerialName("unit_system")
    val unitSystem: String? = "metric"
)