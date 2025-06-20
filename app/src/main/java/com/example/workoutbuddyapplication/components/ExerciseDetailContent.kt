package com.example.workoutbuddyapplication.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.workoutbuddyapplication.models.Exercise

@Composable
fun ExerciseDetailContent(exercise: Exercise) {
    Column(
        modifier = Modifier
            .padding(20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = exercise.name,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Divider(modifier = Modifier.padding(vertical = 8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.FitnessCenter, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Level: ", fontWeight = FontWeight.SemiBold)
            Text(exercise.level)
        }
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.FitnessCenter, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Kracht: ", fontWeight = FontWeight.SemiBold)
            Text(exercise.force)
        }
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.FitnessCenter, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Mechaniek: ", fontWeight = FontWeight.SemiBold)
            Text(exercise.mechanic)
        }
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.FitnessCenter, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Apparatuur: ", fontWeight = FontWeight.SemiBold)
            Text(exercise.equipment ?: "No equipment needed")
        }
        Spacer(Modifier.height(8.dp))
        Divider(modifier = Modifier.padding(vertical = 8.dp))

        Text("Primaire Spieren:", fontWeight = FontWeight.SemiBold)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            exercise.primaryMuscles.forEach {
                AssistChip(onClick = {}, label = { Text(it) })
            }
        }
        if (exercise.secondaryMuscles.isNotEmpty()) {
            Text("Secundaire Spieren:", fontWeight = FontWeight.SemiBold)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                exercise.secondaryMuscles.forEach {
                    AssistChip(onClick = {}, label = { Text(it) })
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text("Categorie: ${exercise.category}", fontWeight = FontWeight.SemiBold)
        Divider(modifier = Modifier.padding(vertical = 8.dp))

        Text("Instructies:", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(Modifier.height(4.dp))
        exercise.instructions.forEachIndexed { idx, instr ->
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                Text("${idx + 1}.", fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(8.dp))
                Text(instr)
            }
        }
    }
}