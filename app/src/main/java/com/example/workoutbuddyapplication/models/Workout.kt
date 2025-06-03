package com.example.workoutbuddyapplication.models
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class Workout(
    val id: Int? = null,
    val type: String,
    val date: String,
    val duration: Int,
    val distance: Double? = null,
    val notes: String? = null,
    @SerialName("profile_id")
    val profileId: String,
) {
    val workoutTypeEnum: WorkoutType
        get() = WorkoutType.fromString(type)
}

enum class WorkoutType(val displayName: String, val icon: ImageVector) {
    RUNNING("Hardlopen", Icons.AutoMirrored.Filled.DirectionsRun),
    CYCLING("Fietsen", Icons.Filled.DirectionsBike),
    STRENGTH("Kracht", Icons.Filled.FitnessCenter),
    YOGA("Yoga", Icons.Filled.SelfImprovement),
    OTHER("Anders", Icons.Filled.QuestionMark);

    companion object {
        fun fromString(type: String): WorkoutType =
            values().firstOrNull { it.name.equals(type, ignoreCase = true) } ?: OTHER
    }
}