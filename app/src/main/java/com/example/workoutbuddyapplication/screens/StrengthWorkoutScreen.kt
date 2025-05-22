package com.example.workoutbuddyapplication.screens

import android.os.SystemClock
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.workoutbuddyapplication.models.ExerciseDevice
import com.example.workoutbuddyapplication.navigation.Screen
import kotlinx.coroutines.delay

data class ExerciseSet(
    val reps: Int,
    val weight: Double,
    val completed: Boolean = false,
    val restTime: Int = 60 // Rest time in seconds
)

data class Exercise(
    val name: String,
    val sets: List<ExerciseSet>,
    val muscleGroup: String = "Algemeen",
    val notes: String = "",
    val device: ExerciseDevice? = null
)

@Composable
fun StrengthWorkoutScreen(navController: NavController) {
    var isRunning by remember { mutableStateOf(true) }
    var elapsedTime by remember { mutableLongStateOf(0L) }
    var calories by remember { mutableIntStateOf(0) }
    var restTimerActive by remember { mutableStateOf(false) }
    var restTimeRemaining by remember { mutableIntStateOf(0) }
    var currentRestingExercise by remember { mutableStateOf("") }
    var currentExerciseForDevice by remember { mutableStateOf<Exercise?>(null) }

    // Exercise tracking
    val exercises = remember { mutableStateListOf<Exercise>() }

    var newExerciseName by remember { mutableStateOf("") }
    var newExerciseMuscleGroup by remember { mutableStateOf("Algemeen") }
    var newExerciseNotes by remember { mutableStateOf("") }
    var showAddExercise by remember { mutableStateOf(false) }
    var showMuscleGroupDropdown by remember { mutableStateOf(false) }

    // Timer effect
    LaunchedEffect(isRunning) {
        val startTime = SystemClock.elapsedRealtime() - elapsedTime
        while (isRunning) {
            elapsedTime = SystemClock.elapsedRealtime() - startTime
            delay(1000)

            // Simulate calorie burn (about 5 calories per minute)
            if (isRunning) {
                calories = (elapsedTime / 60000 * 5).toInt()
            }
        }
    }

    // Rest timer effect
    LaunchedEffect(restTimerActive) {
        while (restTimerActive && restTimeRemaining > 0) {
            delay(1000)
            restTimeRemaining--

            if (restTimeRemaining <= 0) {
                restTimerActive = false
            }
        }
    }

    Scaffold(
        // Remove the floating action buttons
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.FitnessCenter,
                        contentDescription = "Krachttraining",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.padding(8.dp))

                Text(
                    text = "Krachttraining",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Main stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatCard(
                    title = "Tijd",
                    value = formatTime(elapsedTime),
                    icon = Icons.Default.Timer,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.padding(4.dp))

                StatCard(
                    title = "Calorieën",
                    value = "$calories kcal",
                    icon = Icons.Default.LocalFireDepartment,
                    modifier = Modifier.weight(1f)
                )
            }

            // Rest timer card (only show when active)
            if (restTimerActive) {
                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Rust tussen sets",
                            fontWeight = FontWeight.Medium
                        )

                        Text(
                            text = "$restTimeRemaining sec",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Volgende: $currentRestingExercise",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = { restTimerActive = false }
                        ) {
                            Text("Timer stoppen")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            // Add Exercises Button (Blue)
            Button(
                onClick = { /* Handle adding exercises */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Oefeningen toevoegen",
                    fontSize = 18.sp
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Stop Workout Button
            Button(
                onClick = { navController.navigate(Screen.WorkoutCompleted.route) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Stop workout",
                    fontSize = 18.sp
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            // Exercises
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(exercises) { exercise ->
                    EnhancedExerciseCard(
                        exercise = exercise,
                        onStartRest = { duration, exerciseName ->
                            restTimeRemaining = duration
                            currentRestingExercise = exerciseName
                            restTimerActive = true
                        },
                        onDeleteExercise = { exerciseToDelete ->
                            exercises.removeIf { it.name == exerciseToDelete.name }
                        },
                        onScanDevice = {
                            currentExerciseForDevice = exercise
                            navController.navigate(Screen.QRScanner.route)
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }

    // Handle device scanning result
    LaunchedEffect(navController.currentBackStackEntry) {
        val result = navController.currentBackStackEntry
            ?.savedStateHandle
            ?.get<ExerciseDevice>("scanned_device")

        if (result != null && currentExerciseForDevice != null) {
            val index = exercises.indexOfFirst { it.name == currentExerciseForDevice?.name }
            if (index != -1) {
                exercises[index] = exercises[index].copy(device = result)
                navController.currentBackStackEntry?.savedStateHandle?.remove<ExerciseDevice>("scanned_device")
                currentExerciseForDevice = null
            }
        }
    }
}

@Composable
fun EnhancedExerciseCard(
    exercise: Exercise,
    onStartRest: (Int, String) -> Unit,
    onDeleteExercise: (Exercise) -> Unit,
    onScanDevice: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Exercise header with expand/collapse
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = exercise.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )

                    Text(
                        text = exercise.muscleGroup,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )

                    if (exercise.device != null) {
                        Text(
                            text = "Apparaat: ${exercise.device.name}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }

                Row {
                    IconButton(onClick = { showDeleteConfirmation = true }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Verwijderen",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }

                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            if (expanded) Icons.Default.Check else Icons.Default.Add,
                            contentDescription = if (expanded) "Inklappen" else "Uitklappen"
                        )
                    }
                }
            }

            if (exercise.notes.isNotBlank()) {
                Text(
                    text = exercise.notes,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))

                // Device section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = onScanDevice
                            ) {
                                Icon(
                                    Icons.Default.QrCode,
                                    contentDescription = "Scan QR Code",
 tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        if (exercise.device != null) {
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Naam: ${exercise.device.name}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )

                                    Text(
                                        text = "ID: ${exercise.device.id}",
                                        style = MaterialTheme.typography.bodySmall
                                    )

                                    Text(
                                        text = "Type: ${exercise.device.type}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }

                                IconButton(
                                    onClick = { /* Remove device */ }
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Verwijderen",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = "Geen apparaat gekoppeld",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Divider()

                Spacer(modifier = Modifier.height(8.dp))

                // Header row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Set",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(0.5f)
                    )

                    Text(
                        text = "Reps",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(0.75f)
                    )

                    Text(
                        text = "Gewicht",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(0.75f)
                    )

                    Text(
                        text = "Rust",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(0.75f)
                    )

                    Text(
                        text = "Voltooid",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(0.75f)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Sets
                exercise.sets.forEachIndexed { index, set ->
                    var completed by remember { mutableStateOf(set.completed) }
                    var reps by remember { mutableStateOf(set.reps.toString()) }
                    var weight by remember { mutableStateOf(set.weight.toString()) }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${index + 1}",
                            modifier = Modifier.weight(0.5f)
                        )

                        OutlinedTextField(
                            value = reps,
                            onValueChange = { reps = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(0.75f),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = weight,
                            onValueChange = { weight = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(0.75f),
                            singleLine = true
                        )

                        IconButton(
                            onClick = { onStartRest(set.restTime, exercise.name) },
                            modifier = Modifier.weight(0.75f)
                        ) {
                            Icon(
                                Icons.Default.Timer,
                                contentDescription = "Start rust timer",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        Box(
                            modifier = Modifier.weight(0.75f),
                            contentAlignment = Alignment.Center
                        ) {
                            IconButton(
                                onClick = { completed = !completed }
                            ) {
                                if (completed) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = "Voltooid",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .background(
                                                color = MaterialTheme.colorScheme.surfaceVariant,
                                                shape = CircleShape
                                            )
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Add set button
                OutlinedButton(
                    onClick = { /* Add new set logic */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Set toevoegen")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Set toevoegen")
                }
            }
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Oefening verwijderen") },
            text = { Text("Weet je zeker dat je '${exercise.name}' wilt verwijderen?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteExercise(exercise)
                        showDeleteConfirmation = false
                    }
                ) {
                    Text("Verwijderen")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirmation = false }
                ) {
                    Text("Annuleren")
                }
            }
        )
    }
}

@Composable
fun FilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clip(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
