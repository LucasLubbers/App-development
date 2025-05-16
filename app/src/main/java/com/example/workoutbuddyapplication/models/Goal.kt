package com.example.workoutbuddyapplication.models

data class Goal(
    val id: Int,
    val title: String,
    val description: String,
    val target: Double,
    val current: Double,
    val unit: String
)
