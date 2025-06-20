package com.example.workoutbuddyapplication.models

data class Goal(
    val id: Int? = null,
    val profileId: String,
    val title: String,
    val workoutType: WorkoutType,
    val goalType: GoalType,
    val target: Double,
    val unit: String,
    val startDate: String? = null,
    val endDate: String? = null,
    val createdAt: String? = null,
    val description: String = "",
    val current: Double = 0.0
)

enum class GoalType(val displayName: String) {
    COUNT("Aantal"),
    DISTANCE("Afstand"),
    TIME("Tijd"),
    OTHER("Anders");

    companion object {
        fun fromString(value: String): GoalType =
            values().firstOrNull { it.name.equals(value, true) || it.displayName.equals(value, true) } ?: OTHER
    }
}