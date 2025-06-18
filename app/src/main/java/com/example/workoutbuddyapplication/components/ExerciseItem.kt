package com.example.workoutbuddyapplication.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.workoutbuddyapplication.screens.AvailableExercise
import com.example.workoutbuddyapplication.ui.theme.strings

@Composable
fun ExerciseItem(
    exercise: AvailableExercise,
    onExerciseClick: () -> Unit
) {
    val strings = strings()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onExerciseClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = exercise.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = "${exercise.muscleGroup} • ${exercise.equipment}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        IconButton(onClick = onExerciseClick) {
            Icon(
                Icons.Default.Add,
                contentDescription = strings.add,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}