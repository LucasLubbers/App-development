package com.example.workoutbuddyapplication.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.workoutbuddyapplication.screens.Exercise
import com.example.workoutbuddyapplication.screens.ExerciseSet
import com.example.workoutbuddyapplication.ui.theme.UnitSystem
import com.example.workoutbuddyapplication.ui.theme.strings
import com.example.workoutbuddyapplication.utils.UnitConverter

@Composable
fun EnhancedExerciseCard(
    exercise: Exercise,
    unitSystem: UnitSystem,
    onStartRest: (Int, String, Int) -> Unit,
    onSetCompleted: (Int, Boolean) -> Unit,
    activeRestTimer: Boolean,
    activeRestTimerSet: Int,
    restTimeRemaining: Int,
    onDeleteExercise: (Exercise) -> Unit,
    onScanDevice: () -> Unit
) {
    val strings = strings()
    var expanded by remember { mutableStateOf(true) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showRestTimeDialog by remember { mutableStateOf(false) }
    var customRestTime by remember { mutableStateOf("") }
    var defaultRestTimeSeconds by remember { mutableStateOf(120) }

    // Rest time dialog
    if (showRestTimeDialog) {
        AlertDialog(
            onDismissRequest = { showRestTimeDialog = false },
            title = { Text(strings.setRestTime) },
            text = {
                Column {
                    Text("${strings.setRestTimeFor} ${exercise.name}")
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = customRestTime,
                        onValueChange = { customRestTime = it },
                        placeholder = {
                            Text(
                                "2:00",
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val seconds = try {
                            if (customRestTime.contains(":")) {
                                val parts = customRestTime.split(":")
                                val minutes = parts[0].toIntOrNull() ?: 2
                                val secs = parts[1].toIntOrNull() ?: 0
                                minutes * 60 + secs
                            } else {
                                (customRestTime.toFloatOrNull() ?: 2f) * 60f
                            }.toInt()
                        } catch (e: Exception) {
                            120
                        }
                        defaultRestTimeSeconds = seconds
                        // Update all sets' rest time
                        val updatedSets = exercise.sets.map { it.copy(restTime = seconds) }
                        exercise.sets = updatedSets
                        showRestTimeDialog = false
                    }
                ) {
                    Text(strings.updateRestTime)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showRestTimeDialog = false }
                ) {
                    Text(strings.cancel)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = exercise.name,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = strings.menu,
                        modifier = Modifier.size(18.dp)
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(strings.restTime) },
                        leadingIcon = { Icon(Icons.Default.Timer, contentDescription = null) },
                        onClick = {
                            showRestTimeDialog = true
                            showMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(strings.delete) },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                        onClick = {
                            showDeleteConfirmation = true
                            showMenu = false
                        }
                    )
                }
            }
        }

        if (expanded) {
            Spacer(modifier = Modifier.height(8.dp))
            // Headers row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = strings.set,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(0.5f),
                    fontSize = 12.sp
                )
                Text(
                    text = strings.previous,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1.0f),
                    fontSize = 12.sp
                )
                Text(
                    text = UnitConverter.getWeightUnit(unitSystem),
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(0.8f),
                    fontSize = 12.sp
                )
                Text(
                    text = strings.reps,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(0.8f),
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.weight(0.4f))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Divider()

            // Sets
            exercise.sets.forEachIndexed { index, set ->
                var completed by remember(set.completed) { mutableStateOf(set.completed) }
                var reps by remember(set.reps) { mutableStateOf(set.reps.toString()) }
                var weight by remember(set.weight, unitSystem) {
                    mutableStateOf(UnitConverter.weightFromKg(set.weight, unitSystem).toString())
                }

                // Update the values in the set when they change
                LaunchedEffect(reps, weight, completed) {
                    val weightInKg = UnitConverter.weightToKg(
                        weight.toDoubleOrNull() ?: set.weight,
                        unitSystem
                    )
                    val updatedSet = set.copy(
                        reps = reps.toIntOrNull() ?: set.reps,
                        weight = weightInKg,
                        completed = completed
                    )
                    val updatedSets = exercise.sets.toMutableList()
                    updatedSets[index] = updatedSet
                    exercise.sets = updatedSets
                }

                // Update weight display when unit system changes
                LaunchedEffect(unitSystem) {
                    weight = UnitConverter.weightFromKg(set.weight, unitSystem).toString()
                }

                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .background(
                                color = if (completed)
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                else
                                    Color.Transparent,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 4.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${index + 1}",
                            modifier = Modifier.weight(0.5f),
                            fontSize = 14.sp
                        )
                        Text(
                            text = "${
                                UnitConverter.formatWeight(
                                    set.weight,
                                    unitSystem
                                )
                            }×${set.reps}",
                            modifier = Modifier.weight(1.0f),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                        OutlinedTextField(
                            value = weight,
                            onValueChange = { weight = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(0.8f),
                            singleLine = true,
                            textStyle = TextStyle(fontSize = 14.sp)
                        )
                        OutlinedTextField(
                            value = reps,
                            onValueChange = { reps = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(0.8f),
                            singleLine = true,
                            textStyle = TextStyle(fontSize = 14.sp)
                        )
                        IconButton(
                            onClick = {
                                val wasCompleted = completed
                                completed = !completed
                                if (!wasCompleted && completed) {
                                    val restTime = set.restTime
                                    onStartRest(restTime, exercise.name, index)
                                }
                                onSetCompleted(reps.toIntOrNull() ?: set.reps, completed)
                            },
                            modifier = Modifier
                                .weight(0.4f)
                                .size(36.dp)
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = strings.completed,
                                tint = if (completed)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    if (activeRestTimer && activeRestTimerSet == index) {
                        CompactRestTimer(restTimeRemaining)
                    }
                }
                if (index < exercise.sets.size - 1) {
                    Divider(modifier = Modifier.padding(vertical = 2.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = {
                    val newSet = if (exercise.sets.isNotEmpty()) {
                        val lastSet = exercise.sets.last()
                        ExerciseSet(
                            reps = lastSet.reps,
                            weight = lastSet.weight,
                            completed = false,
                            restTime = defaultRestTimeSeconds
                        )
                    } else {
                        ExerciseSet(reps = 10, weight = 20.0, restTime = defaultRestTimeSeconds)
                    }
                    val updatedSets = exercise.sets.toMutableList()
                    updatedSets.add(newSet)
                    exercise.sets = updatedSets
                },
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 6.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = strings.addSet,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(strings.addSet, fontSize = 14.sp)
            }
        }
        Divider(modifier = Modifier.padding(top = 8.dp))
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text(strings.deleteExercise) },
            text = { Text(String.format(strings.deleteExerciseConfirm, exercise.name)) },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteExercise(exercise)
                        showDeleteConfirmation = false
                    }
                ) {
                    Text(strings.delete)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirmation = false }
                ) {
                    Text(strings.cancel)
                }
            }
        )
    }
}