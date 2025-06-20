package com.example.workoutbuddyapplication.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ExerciseDetailLoading(modifier: Modifier = Modifier) {
    CircularProgressIndicator(modifier = modifier)
}