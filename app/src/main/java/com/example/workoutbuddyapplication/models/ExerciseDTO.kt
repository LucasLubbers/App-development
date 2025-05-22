package com.example.workoutbuddyapplication.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

// Tell the serializer to ignore unknown properties
@kotlinx.serialization.SerialName("exercises")
@Serializable
data class ExerciseDTO(
    val id: String? = null,
    val name: String = "",
    val force: String? = null,
    val level: String? = null,
    val mechanic: String? = null,
    val equipment: String? = null,
    val primary_muscles: List<String> = listOf(),
    val secondary_muscles: List<String>? = null,
    val category: String = "",
    val instructions: List<String> = listOf(),
    val created_at: String? = null,
    val updated_at: String? = null,
    
    // Add the new fields explicitly so they're not unknown
    @JsonNames("QRcode-link", "QRcode_link")
    val qrcode_link: String? = null,
    
    val calories: Int? = null
) 