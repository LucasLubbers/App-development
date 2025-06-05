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
import androidx.compose.material3.Checkbox
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
import androidx.compose.material3.Surface
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
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import com.example.workoutbuddyapplication.R
import com.example.workoutbuddyapplication.models.ExerciseDevice
import com.example.workoutbuddyapplication.navigation.Screen
import com.example.workoutbuddyapplication.models.Exercise as ExerciseModel
import com.example.workoutbuddyapplication.models.ExerciseDTO
import com.example.workoutbuddyapplication.data.SupabaseClient
import com.example.workoutbuddyapplication.ui.theme.UserPreferencesManager
import com.example.workoutbuddyapplication.ui.theme.UnitSystem
import com.example.workoutbuddyapplication.ui.theme.toUnitSystem
import com.example.workoutbuddyapplication.utils.UnitConverter
import com.example.workoutbuddyapplication.utils.formatTime
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import com.example.workoutbuddyapplication.screens.StatCard

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
    val context = LocalContext.current
    val preferencesManager = remember { UserPreferencesManager(context) }
    val selectedUnitSystem by preferencesManager.selectedUnitSystem.collectAsState(initial = "metric")
    val unitSystem = selectedUnitSystem.toUnitSystem()
    
    var isRunning by remember { mutableStateOf(true) }
    var elapsedTime by remember { mutableLongStateOf(0L) }
    var calories by remember { mutableIntStateOf(0) }
    var exerciseCalories by remember { mutableIntStateOf(0) } // Track exercise calories separately
    var currentExerciseForDevice by remember { mutableStateOf<Exercise?>(null) }
    var showExerciseSelector by remember { mutableStateOf(false) }
    var showPresetMenu by remember { mutableStateOf(false) }

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

            // Update total calories
            calories = ((elapsedTime / 60000) * 5).toInt() + exerciseCalories // 5 calories/min base rate
        }
    }

    // Main UI
    Scaffold(
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FloatingActionButton(
                    onClick = { showExerciseSelector = true },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_exercise))
                }

                FloatingActionButton(
                    onClick = { isRunning = !isRunning },
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(
                        if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isRunning) stringResource(R.string.pause) else stringResource(R.string.resume)
                    )
                }

                FloatingActionButton(
                    onClick = {
                        val formattedDuration = formatTime(elapsedTime)
                        val totalSets = exercises.sumOf { it.sets.size }
                        navController.navigate(
                            Screen.WorkoutCompleted.createRoute(
                                duration = formattedDuration,
                                distance = "$totalSets sets",
                                calories = calories,
                                steps = 0
                            )
                        )
                    },
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ) {
                    Icon(
                        Icons.Default.Stop,
                        contentDescription = stringResource(R.string.stop)
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
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
                        contentDescription = stringResource(R.string.strength_training),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.padding(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.strength_training),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Preset templates menu
                Box {
                    IconButton(onClick = { showPresetMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.presets))
                    }

                    DropdownMenu(
                        expanded = showPresetMenu,
                        onDismissRequest = { showPresetMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.push_workout)) },
                            onClick = {
                                exercises.clear()
                                exercises.addAll(getPushWorkout())
                                showPresetMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.pull_workout)) },
                            onClick = {
                                exercises.clear()
                                exercises.addAll(getPullWorkout())
                                showPresetMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.leg_workout)) },
                            onClick = {
                                exercises.clear()
                                exercises.addAll(getLegWorkout())
                                showPresetMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.full_body_workout)) },
                            onClick = {
                                exercises.clear()
                                exercises.addAll(getFullBodyWorkout())
                                showPresetMenu = false
                            }
                        )
                    }
                }
            }

            // Stat Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatCard(
                    title = stringResource(R.string.time),
                    value = formatTime(elapsedTime),
                    icon = Icons.Default.Timer,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.padding(4.dp))

                StatCard(
                    title = stringResource(R.string.calories),
                    value = "$calories kcal",
                    icon = Icons.Default.LocalFireDepartment,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Exercise List
            if (exercises.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(R.string.no_exercises_added), style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 80.dp) // Space for FABs
                ) {
                    items(items = exercises, key = { it.name }) { exercise ->
                        ExerciseCard(
                            exercise = exercise,
                            unitSystem = unitSystem,
                            onUpdateExercise = { updatedExercise ->
                                val index = exercises.indexOfFirst { it.name == updatedExercise.name }
                                if (index != -1) {
                                    exercises[index] = updatedExercise
                                }
                            },
                            onDeleteExercise = {
                                exercises.remove(exercise)
                            },
                            onAddSet = {
                                val currentSets = exercise.sets.toMutableList()
                                val lastSet = currentSets.lastOrNull()
                                currentSets.add(
                                    ExerciseSet(
                                        reps = lastSet?.reps ?: 10,
                                        weight = lastSet?.weight ?: 50.0
                                    )
                                )
                                exercise.sets = currentSets
                            },
                            onSetCompleted = { setIndex, completed ->
                                val updatedSets = exercise.sets.toMutableList()
                                val currentSet = updatedSets[setIndex]

                                if (completed && !currentSet.completed) {
                                    addCaloriesFromSet(currentSet.reps, exercise.caloriesPerRep)
                                }

                                updatedSets[setIndex] = currentSet.copy(completed = completed)
                                exercise.sets = updatedSets

                                // Start rest timer
                                if (completed) {
                                    activeRestTimerExercise = exercise.name
                                    activeRestTimerSetIndex = setIndex
                                    restTimeRemaining = currentSet.restTime
                                    timerActive = true
                                } else {
                                    // If un-checking a set, stop its timer
                                    if (activeRestTimerExercise == exercise.name && activeRestTimerSetIndex == setIndex) {
                                        timerActive = false
                                        activeRestTimerExercise = null
                                        activeRestTimerSetIndex = -1
                                    }
                                }
                            },
                            activeRestTimerExercise = activeRestTimerExercise,
                            activeRestTimerSetIndex = activeRestTimerSetIndex,
                            restTimeRemaining = restTimeRemaining
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }

    if (showExerciseSelector) {
        ExerciseSelectorDialog(
            availableExercises = availableExercises,
            onDismiss = { showExerciseSelector = false },
            onExerciseSelected = { selectedExercise ->
                exercises.add(
                    Exercise(
                        name = selectedExercise.name,
                        sets = listOf(ExerciseSet(reps = 10, weight = 50.0)),
                        muscleGroup = selectedExercise.muscleGroup,
                        caloriesPerRep = selectedExercise.caloriesPerRep
                    )
                )
                showExerciseSelector = false
            }
        )
    }
}

@Composable
fun ExerciseCard(
    exercise: Exercise,
    unitSystem: UnitSystem,
    onUpdateExercise: (Exercise) -> Unit,
    onDeleteExercise: () -> Unit,
    onAddSet: () -> Unit,
    onSetCompleted: (Int, Boolean) -> Unit,
    activeRestTimerExercise: String?,
    activeRestTimerSetIndex: Int,
    restTimeRemaining: Int
) {
    var expanded by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = exercise.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = exercise.muscleGroup,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = stringResource(R.string.expand)
                    )
                }
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(16.dp))
                
                // Header row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.set), modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.weight), modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.reps), modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.done), modifier = Modifier.weight(0.5f), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                }
                
                Divider(modifier = Modifier.padding(vertical = 4.dp))
                
                exercise.sets.forEachIndexed { index, set ->
                    SetRow(
                        set = set,
                        setIndex = index,
                        unitSystem = unitSystem,
                        onUpdateSet = { updatedSet ->
                            val updatedSets = exercise.sets.toMutableList()
                            updatedSets[index] = updatedSet
                            onUpdateExercise(exercise.copy(sets = updatedSets))
                        },
                        onDeleteSet = {
                            val updatedSets = exercise.sets.toMutableList()
                            updatedSets.removeAt(index)
                            onUpdateExercise(exercise.copy(sets = updatedSets))
                        },
                        onSetCompleted = { completed ->
                            onSetCompleted(index, completed)
                        }
                    )
                    
                    // Show rest timer if this set is active
                    if (activeRestTimerExercise == exercise.name && activeRestTimerSetIndex == index && restTimeRemaining > 0) {
                        RestTimerRow(restTimeRemaining)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onAddSet,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.add_set))
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = { showEditDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.edit_exercise))
                }
            }
        }
    }

    if (showEditDialog) {
        EditExerciseDialog(
            exercise = exercise,
            onDismiss = { showEditDialog = false },
            onSave = { updatedExercise ->
                onUpdateExercise(updatedExercise)
                showEditDialog = false
            },
            onDelete = {
                onDeleteExercise()
                showEditDialog = false
            }
        )
    }
}

@Composable
fun SetRow(
    set: ExerciseSet,
    setIndex: Int,
    unitSystem: UnitSystem,
    onUpdateSet: (ExerciseSet) -> Unit,
    onDeleteSet: () -> Unit,
    onSetCompleted: (Boolean) -> Unit
) {
    var reps by remember { mutableStateOf(set.reps.toString()) }
    var weight by remember { mutableStateOf(UnitConverter.toDisplayWeight(set.weight, unitSystem).toString()) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text((setIndex + 1).toString(), modifier = Modifier.weight(1f))

        OutlinedTextField(
            value = weight,
            onValueChange = {
                weight = it
                val metricWeight = UnitConverter.fromDisplayWeight(it.toDoubleOrNull() ?: 0.0, unitSystem)
                onUpdateSet(set.copy(weight = metricWeight))
            },
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 4.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            textStyle = TextStyle(fontSize = 14.sp)
        )

        OutlinedTextField(
            value = reps,
            onValueChange = {
                reps = it
                onUpdateSet(set.copy(reps = it.toIntOrNull() ?: 0))
            },
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 4.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            textStyle = TextStyle(fontSize = 14.sp)
        )

        Checkbox(
            checked = set.completed,
            onCheckedChange = onSetCompleted,
            modifier = Modifier.weight(0.5f)
        )
    }
}

@Composable
fun EditExerciseDialog(
    exercise: Exercise,
    onDismiss: () -> Unit,
    onSave: (Exercise) -> Unit,
    onDelete: () -> Unit
) {
    var notes by remember { mutableStateOf(exercise.notes) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.edit_exercise)) },
        text = {
            Column {
                Text(
                    text = exercise.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(stringResource(R.string.notes)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(exercise.copy(notes = notes)) }
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            Row {
                Button(
                    onClick = { showDeleteConfirm = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.delete))
                }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.cancel))
                }
            }
        }
    )

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.delete_exercise)) },
            text = { Text(stringResource(R.string.delete_exercise_confirm, exercise.name)) },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun ExerciseSelectorDialog(
    availableExercises: List<AvailableExercise>,
    onDismiss: () -> Unit,
    onExerciseSelected: (AvailableExercise) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    
    val filteredExercises = if (searchQuery.isBlank()) {
        availableExercises
    } else {
        availableExercises.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.muscleGroup.contains(searchQuery, ignoreCase = true)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.add_exercise),
                        style = MaterialTheme.typography.titleLarge
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.close))
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text(stringResource(R.string.search_exercises)) },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null)
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))
                
                if (filteredExercises.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(stringResource(R.string.no_exercises_found))
                    }
                } else {
                    LazyColumn {
                        items(filteredExercises) { exercise ->
                            ExerciseListItem(
                                exercise = exercise,
                                onClick = { onExerciseSelected(exercise) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExerciseListItem(
    exercise: AvailableExercise,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${exercise.muscleGroup} - ${exercise.equipment}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.Default.Add,
                contentDescription = stringResource(R.string.add)
            )
        }
    }
}

@Composable
fun RestTimerRow(
    timeRemaining: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp)
            .background(
                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                RoundedCornerShape(8.dp)
            )
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Timer, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "${stringResource(R.string.rest_time_remaining)}: ${formatTime(timeRemaining * 1000L)}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

// Preset Workouts
fun getPushWorkout(): List<Exercise> {
    return listOf(
        Exercise("Bench Press", listOf(ExerciseSet(8, 60.0), ExerciseSet(8, 60.0), ExerciseSet(8, 60.0)), "Borst"),
        Exercise("Overhead Press", listOf(ExerciseSet(10, 40.0), ExerciseSet(10, 40.0), ExerciseSet(10, 40.0)), "Schouders"),
        Exercise("Tricep Dips", listOf(ExerciseSet(12, 0.0), ExerciseSet(12, 0.0), ExerciseSet(12, 0.0)), "Armen")
    )
}

fun getPullWorkout(): List<Exercise> {
    return listOf(
        Exercise("Pull Ups", listOf(ExerciseSet(5, 0.0), ExerciseSet(5, 0.0), ExerciseSet(5, 0.0)), "Rug"),
        Exercise("Barbell Rows", listOf(ExerciseSet(8, 70.0), ExerciseSet(8, 70.0), ExerciseSet(8, 70.0)), "Rug"),
        Exercise("Bicep Curls", listOf(ExerciseSet(12, 15.0), ExerciseSet(12, 15.0), ExerciseSet(12, 15.0)), "Armen")
    )
}

fun getLegWorkout(): List<Exercise> {
    return listOf(
        Exercise("Squats", listOf(ExerciseSet(10, 80.0), ExerciseSet(10, 80.0), ExerciseSet(10, 80.0)), "Benen"),
        Exercise("Leg Press", listOf(ExerciseSet(10, 120.0), ExerciseSet(10, 120.0), ExerciseSet(10, 120.0)), "Benen"),
        Exercise("Calf Raises", listOf(ExerciseSet(15, 20.0), ExerciseSet(15, 20.0), ExerciseSet(15, 20.0)), "Benen")
    )
}

fun getFullBodyWorkout(): List<Exercise> {
    return listOf(
        Exercise("Squats", listOf(ExerciseSet(10, 80.0), ExerciseSet(10, 80.0), ExerciseSet(10, 80.0)), "Benen"),
        Exercise("Bench Press", listOf(ExerciseSet(8, 60.0), ExerciseSet(8, 60.0), ExerciseSet(8, 60.0)), "Borst"),
        Exercise("Barbell Rows", listOf(ExerciseSet(8, 70.0), ExerciseSet(8, 70.0), ExerciseSet(8, 70.0)), "Rug")
    )
}
