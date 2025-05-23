package com.example.workoutbuddyapplication.screens

import android.os.SystemClock
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import com.example.workoutbuddyapplication.models.ExerciseDevice
import com.example.workoutbuddyapplication.navigation.Screen
import com.example.workoutbuddyapplication.models.Exercise as ExerciseModel
import com.example.workoutbuddyapplication.models.ExerciseDTO
import com.example.workoutbuddyapplication.data.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

data class ExerciseSet(
    val reps: Int,
    val weight: Double,
    val completed: Boolean = false,
    val restTime: Int = 60 // Rest time in seconds
)

data class Exercise(
    val name: String,
    var sets: List<ExerciseSet>,
    val muscleGroup: String = "Algemeen",
    val notes: String = "",
    val device: ExerciseDevice? = null,
    val caloriesPerRep: Int = 1 // Default to 1 calorie per rep
)

data class AvailableExercise(
    val name: String,
    val muscleGroup: String,
    val equipment: String,
    val caloriesPerRep: Int = 1 // Default to 1 calorie per rep
)

@Composable
fun StrengthWorkoutScreen(navController: NavController) {
    var isRunning by remember { mutableStateOf(true) }
    var elapsedTime by remember { mutableLongStateOf(0L) }
    var calories by remember { mutableIntStateOf(0) }
    var exerciseCalories by remember { mutableIntStateOf(0) } // Track exercise calories separately
    var currentExerciseForDevice by remember { mutableStateOf<Exercise?>(null) }
    var showExerciseSelector by remember { mutableStateOf(false) }

    // For tracking rest timer between sets
    var activeRestTimerExercise by remember { mutableStateOf<String?>(null) }
    var activeRestTimerSetIndex by remember { mutableIntStateOf(-1) }
    var restTimeRemaining by remember { mutableIntStateOf(0) }
    var timerActive by remember { mutableStateOf(false) }

    // Exercise tracking
    val exercises = remember { mutableStateListOf<Exercise>() }
    
    // Available exercises from Supabase
    var availableExercises by remember { mutableStateOf<List<AvailableExercise>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    
    val coroutineScope = rememberCoroutineScope()
    
    // Function to add calories from an exercise set
    val addCaloriesFromSet = { reps: Int, caloriesPerRep: Int ->
        exerciseCalories += reps * caloriesPerRep
    }

    // Fetch exercises from Supabase
    LaunchedEffect(key1 = true) {
        coroutineScope.launch {
            try {
                // Standard approach
                val exerciseDTOs = SupabaseClient.client.postgrest
                    .from("exercises")
                    .select()
                    .decodeList<ExerciseDTO>()
                
                // Convert DTOs to AvailableExercise objects
                availableExercises = exerciseDTOs.map { dto ->
                    AvailableExercise(
                        name = dto.name,
                        muscleGroup = dto.primary_muscles.firstOrNull() ?: "Algemeen",
                        equipment = dto.equipment ?: "Bodyweight",
                        caloriesPerRep = dto.calories ?: 1
                    )
                }
                isLoading = false
                error = null
            } catch (e: Exception) {
                // Fall back to hardcoded exercises
                availableExercises = listOf(
                    AvailableExercise(
                        name = "Bench Press",
                        muscleGroup = "Borst",
                        equipment = "Barbell",
                        caloriesPerRep = 1
                    ),
                    AvailableExercise(
                        name = "Deadlift",
                        muscleGroup = "Rug",
                        equipment = "Barbell",
                        caloriesPerRep = 1
                    ),
                    AvailableExercise(
                        name = "Squat",
                        muscleGroup = "Benen",
                        equipment = "Barbell",
                        caloriesPerRep = 1
                    ),
                    AvailableExercise(
                        name = "Shoulder Press",
                        muscleGroup = "Schouders",
                        equipment = "Dumbbell",
                        caloriesPerRep = 1
                    ),
                    AvailableExercise(
                        name = "Bicep Curls",
                        muscleGroup = "Armen",
                        equipment = "Dumbbell",
                        caloriesPerRep = 1
                    ),
                    AvailableExercise(
                        name = "Lat Pulldown",
                        muscleGroup = "Rug",
                        equipment = "Cable Machine",
                        caloriesPerRep = 1
                    )
                )
                isLoading = false
                error = null
                println("Using hardcoded exercises due to error: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    // Rest timer effect - only run when timer is actually active
    LaunchedEffect(timerActive) {
        if (timerActive && restTimeRemaining > 0) {
            while (timerActive && restTimeRemaining > 0) {
                delay(1000)
                restTimeRemaining--

                if (restTimeRemaining <= 0) {
                    timerActive = false
                    activeRestTimerExercise = null
                    activeRestTimerSetIndex = -1
                }
            }
        }
    }

    // Timer effect (for calories)
    LaunchedEffect(isRunning) {
        val startTime = SystemClock.elapsedRealtime() - elapsedTime
        while (isRunning) {
            elapsedTime = SystemClock.elapsedRealtime() - startTime
            delay(1000)

            // Simulate calorie burn (about 5 calories per minute)
            if (isRunning) {
                // Calculate time-based calories only
                val timeCalories = (elapsedTime / 60000 * 5).toInt()
                // Set the total calories (time-based + exercise-based)
                calories = timeCalories + exerciseCalories
            }
        }
    }

    // Show exercise selector dialog
    if (showExerciseSelector) {
        ExerciseSelectorDialog(
            availableExercises = availableExercises,
            isLoading = isLoading,
            error = error,
            onDismiss = { showExerciseSelector = false },
            onExerciseSelected = { selectedExercise ->
                // Create a new exercise with just one set
                val newExercise = Exercise(
                    name = selectedExercise.name,
                    muscleGroup = selectedExercise.muscleGroup,
                    sets = listOf(
                        ExerciseSet(reps = 10, weight = 20.0)
                    ),
                    caloriesPerRep = selectedExercise.caloriesPerRep
                )
                exercises.add(newExercise)
                showExerciseSelector = false
            }
        )
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

            Spacer(modifier = Modifier.height(40.dp))
            
            // Add Exercises Button (Blue)
            Button(
                onClick = { showExerciseSelector = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(8.dp)
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
                shape = RoundedCornerShape(8.dp)
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
                        onStartRest = { duration, exerciseName, setIndex ->
                            activeRestTimerExercise = exerciseName
                            activeRestTimerSetIndex = setIndex
                            restTimeRemaining = duration
                            timerActive = true
                        },
                        onSetCompleted = { reps, wasCompleted -> 
                            // Only add calories when a set is newly completed (not when unmarking)
                            if (wasCompleted) {
                                addCaloriesFromSet(reps, exercise.caloriesPerRep)
                            }
                        },
                        activeRestTimer = timerActive && activeRestTimerExercise == exercise.name,
                        activeRestTimerSet = activeRestTimerSetIndex,
                        restTimeRemaining = restTimeRemaining,
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
    onStartRest: (Int, String, Int) -> Unit,
    onSetCompleted: (Int, Boolean) -> Unit,
    activeRestTimer: Boolean,
    activeRestTimerSet: Int,
    restTimeRemaining: Int,
    onDeleteExercise: (Exercise) -> Unit,
    onScanDevice: () -> Unit
) {
    var expanded by remember { mutableStateOf(true) } // Default to expanded
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showRestTimeDialog by remember { mutableStateOf(false) }
    var customRestTime by remember { mutableStateOf("") }
    
    // Create a mutable state list for the sets to allow adding new sets
    val exerciseSets = remember { mutableStateListOf<ExerciseSet>().apply { addAll(exercise.sets) } }
    
    // Store default rest time
    var defaultRestTimeSeconds by remember { mutableStateOf(120) } // Default 2 minutes
    
    // Function to add a new set
    val addNewSet = {
        // Clone the last set if available, otherwise create a default
        val newSet = if (exerciseSets.isNotEmpty()) {
            val lastSet = exerciseSets.last()
            ExerciseSet(
                reps = lastSet.reps,
                weight = lastSet.weight,
                completed = false,
                restTime = defaultRestTimeSeconds
            )
        } else {
            ExerciseSet(reps = 10, weight = 20.0, restTime = defaultRestTimeSeconds)
        }
        
        // Add the new set to our local list
        exerciseSets.add(newSet)
        
        // Update the exercise's sets in the parent state
        exercise.sets = exerciseSets.toList()
    }
    
    // Rest time dialog
    if (showRestTimeDialog) {
        AlertDialog(
            onDismissRequest = { showRestTimeDialog = false },
            title = { Text("Rust tijd instellen") },
            text = {
                Column {
                    Text("Stel de rusttijd in minuten in voor ${exercise.name}")
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = customRestTime,
                        onValueChange = { customRestTime = it },
                        placeholder = { Text("2:00", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { 
                        // Parse the input as minutes:seconds and convert to seconds
                        val seconds = try {
                            if (customRestTime.contains(":")) {
                                val parts = customRestTime.split(":")
                                val minutes = parts[0].toIntOrNull() ?: 2
                                val secs = parts[1].toIntOrNull() ?: 0
                                minutes * 60 + secs
                            } else {
                                // If just a number, assume it's minutes
                                (customRestTime.toFloatOrNull() ?: 2f) * 60f
                            }.toInt()
                        } catch (e: Exception) {
                            // Default to 2 minutes if parsing fails
                            120
                        }
                        
                        // Just update the default rest time without activating the timer
                        defaultRestTimeSeconds = seconds
                        
                        // Update all sets with the new rest time
                        for (i in exerciseSets.indices) {
                            exerciseSets[i] = exerciseSets[i].copy(restTime = seconds)
                        }
                        
                        // Update the exercise's sets in the parent state
                        exercise.sets = exerciseSets.toList()
                        
                        showRestTimeDialog = false 
                    }
                ) {
                    Text("Update rust tijd")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showRestTimeDialog = false }
                ) {
                    Text("Annuleren")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        // Exercise header with menu button
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
                        contentDescription = "Menu",
                        modifier = Modifier.size(18.dp)
                    )
                }
                
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Rust tijd") },
                        leadingIcon = { Icon(Icons.Default.Timer, contentDescription = null) },
                        onClick = { 
                            showRestTimeDialog = true
                            showMenu = false 
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Verwijderen") },
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
                    text = "Set",
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(0.5f),
                    fontSize = 12.sp
                )
                Text(
                    text = "Vorige",
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1.0f),
                    fontSize = 12.sp
                )
                Text(
                    text = "kg",
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(0.8f),
                    fontSize = 12.sp
                )
                Text(
                    text = "reps",
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(0.8f),
                    fontSize = 12.sp
                )
                // Empty space for finish button
                Spacer(modifier = Modifier.weight(0.4f))
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            Divider()
            
            // Sets
            exerciseSets.forEachIndexed { index, set ->
                var completed by remember { mutableStateOf(set.completed) }
                var reps by remember { mutableStateOf(set.reps.toString()) }
                var weight by remember { mutableStateOf(set.weight.toString()) }
                var showRestTimer by remember { mutableStateOf(false) }
                
                // Update the values in the set when they change
                LaunchedEffect(reps, weight, completed) {
                    exerciseSets[index] = set.copy(
                        reps = reps.toIntOrNull() ?: set.reps,
                        weight = weight.toDoubleOrNull() ?: set.weight,
                        completed = completed
                    )
                }
                
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Main set row
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
                        // Set number
                        Text(
                            text = "${index + 1}",
                            modifier = Modifier.weight(0.5f),
                            fontSize = 14.sp
                        )
                        
                        // Previous weight/reps
                        Text(
                            text = "${set.weight}×${set.reps}",
                            modifier = Modifier.weight(1.0f),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                        
                        // Weight input - fixed
                        OutlinedTextField(
                            value = weight,
                            onValueChange = { weight = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(0.8f),
                            singleLine = true,
                            textStyle = TextStyle(fontSize = 14.sp)
                        )
                        
                        // Reps input - fixed
                        OutlinedTextField(
                            value = reps,
                            onValueChange = { reps = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(0.8f),
                            singleLine = true,
                            textStyle = TextStyle(fontSize = 14.sp)
                        )
                        
                        // Finish button
                        IconButton(
                            onClick = { 
                                val wasCompleted = completed
                                completed = !completed
                                
                                // Update the actual set data
                                exerciseSets[index] = exerciseSets[index].copy(
                                    completed = completed
                                )
                                
                                // Only start rest timer when marking a set as completed (not when unmarking)
                                if (!wasCompleted && completed) {
                                    val restTime = exerciseSets[index].restTime
                                    onStartRest(restTime, exercise.name, index)
                                }

                                // Only add calories when a set is newly completed (not when unmarking)
                                if (wasCompleted && !completed) {
                                    // Temporarily commenting out to fix compilation error
                                    // addCaloriesFromSet(reps.toIntOrNull() ?: 0, exercise.caloriesPerRep)
                                }
                            },
                            modifier = Modifier
                                .weight(0.4f)
                                .size(36.dp)
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "Voltooid",
                                tint = if (completed) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    
                    // Show compact rest timer below the set if it's active
                    if (activeRestTimer && activeRestTimerSet == index) {
                        CompactRestTimer(restTimeRemaining)
                    }
                }
                
                if (index < exerciseSets.size - 1) {
                    Divider(modifier = Modifier.padding(vertical = 2.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Add set button
            OutlinedButton(
                onClick = { addNewSet() },
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 6.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Set toevoegen",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Set toevoegen", fontSize = 14.sp)
            }
        }
        
        Divider(modifier = Modifier.padding(top = 8.dp))
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
fun CompactRestTimer(remainingSeconds: Int) {
    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val formattedTime = String.format("%d:%02d", minutes, seconds)
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Timer,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "RUST TIJD: $formattedTime",
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
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

@Composable
fun ExerciseSelectorDialog(
    availableExercises: List<AvailableExercise>,
    isLoading: Boolean,
    error: String?,
    onDismiss: () -> Unit,
    onExerciseSelected: (AvailableExercise) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    
    val filteredExercises = remember(searchQuery, availableExercises) {
        if (searchQuery.isBlank()) {
            availableExercises
        } else {
            availableExercises.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.muscleGroup.contains(searchQuery, ignoreCase = true) ||
                it.equipment.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Oefeningen",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Sluiten")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Search field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Zoek oefeningen...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Zoeken") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Exercise list
                if (isLoading) {
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (error != null) {
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f)
                    ) {
                        items(filteredExercises) { exercise ->
                            ExerciseItem(
                                exercise = exercise,
                                onExerciseClick = { onExerciseSelected(exercise) }
                            )
                            Divider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExerciseItem(
    exercise: AvailableExercise,
    onExerciseClick: () -> Unit
) {
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
                contentDescription = "Toevoegen",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
