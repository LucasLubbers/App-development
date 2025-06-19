package com.example.workoutbuddyapplication.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun WorkoutTypeDistribution(running: Int, cycling: Int, strength: Int, other: Int) {
    Column {
        WorkoutTypeBar("Hardlopen", running, MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(8.dp))
        WorkoutTypeBar("Fietsen", cycling, MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(8.dp))
        WorkoutTypeBar("Krachttraining", strength, MaterialTheme.colorScheme.secondary)
        Spacer(modifier = Modifier.height(8.dp))
        WorkoutTypeBar("Overig", other, MaterialTheme.colorScheme.surfaceVariant)
    }
}