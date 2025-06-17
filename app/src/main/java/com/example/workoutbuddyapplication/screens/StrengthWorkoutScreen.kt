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
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import com.example.workoutbuddyapplication.models.ExerciseDTO
import com.example.workoutbuddyapplication.data.SupabaseClient
import com.example.workoutbuddyapplication.ui.theme.UserPreferencesManager
import com.example.workoutbuddyapplication.ui.theme.UnitSystem
import com.example.workoutbuddyapplication.ui.theme.toUnitSystem
import com.example.workoutbuddyapplication.utils.UnitConverter
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.example.workoutbuddyapplication.ui.theme.strings
import com.example.workoutbuddyapplication.ui.theme.StringResources
import androidx.compose.material.icons.filled.Mic
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.compose.runtime.DisposableEffect
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.example.workoutbuddyapplication.models.Workout
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun StrengthWorkoutScreen(navController: NavController) {
    val strings = strings()
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

    // Add state for saving workout and error
    var isSaving by remember { mutableStateOf(false) }
    var saveError by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // Function to add calories from an exercise set
    val addCaloriesFromSet = { reps: Int, caloriesPerRep: Int ->
        exerciseCalories += reps * caloriesPerRep
    }

    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasAudioPermission = granted
    }

    var voiceCommandsEnabled by remember { mutableStateOf(false) }
    var showStopWordDialog by remember { mutableStateOf(false) }
    var customStopWord by remember { mutableStateOf("stop") }
    var tempStopWord by remember { mutableStateOf(customStopWord) }
    val isStopWordValid = tempStopWord.trim().isNotEmpty() && tempStopWord.all { it.isLetter() || it.isWhitespace() }

    LaunchedEffect(voiceCommandsEnabled) {
        if (voiceCommandsEnabled && !hasAudioPermission) {
            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
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
                val defaultWeight = if (unitSystem == UnitSystem.IMPERIAL) 45.0 else 20.0 // 45 lbs ≈ 20 kg
                val newExercise = Exercise(
                    name = selectedExercise.name,
                    muscleGroup = selectedExercise.muscleGroup,
                    sets = listOf(
                        ExerciseSet(reps = 10, weight = defaultWeight)
                    ),
                    caloriesPerRep = selectedExercise.caloriesPerRep
                )
                exercises.add(newExercise)
                showExerciseSelector = false
            }
        )
    }

    // Show preset menu dialog
    if (showPresetMenu) {
        PresetMenuDialog(
            availableExercises = availableExercises,
            unitSystem = unitSystem,
            onDismiss = { showPresetMenu = false },
            onPresetSelected = { presetExercises ->
                // Add the selected preset exercises
                exercises.addAll(presetExercises)
                showPresetMenu = false
            }
        )
    }

    Scaffold(
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
                        contentDescription = strings.strengthWorkoutTitle,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.padding(8.dp))

                Text(
                    text = strings.strengthWorkoutTitle,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.weight(1f))

                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Voice Commands",
                    tint = if (voiceCommandsEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Switch(
                    checked = voiceCommandsEnabled,
                    onCheckedChange = { checked ->
                        if (checked) {
                            showStopWordDialog = true
                        } else {
                            voiceCommandsEnabled = false
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Main stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatCard(
                    title = strings.time,
                    value = formatTime(elapsedTime),
                    icon = Icons.Default.Timer,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.padding(4.dp))

                StatCard(
                    title = strings.calories,
                    value = "$calories kcal",
                    icon = Icons.Default.LocalFireDepartment,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Preset toevoegen Button (Green)
            Button(
                onClick = { showPresetMenu = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50) // Green color
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = strings.addPreset,
                    fontSize = 18.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

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
                    text = strings.addExercises,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Stop Workout Button
            Button(
                onClick = {
                    isSaving = true
                    saveError = null
                    val durationMinutes = (elapsedTime / 60000).toInt()
                    val dateString = java.time.LocalDate.now().toString()
                    coroutineScope.launch {
                        try {
                            val user = SupabaseClient.client.auth.currentUserOrNull()
                            if (user == null) {
                                saveError = "Gebruiker niet ingelogd"
                                isSaving = false
                                return@launch
                            }
                            val workout = Workout(
                                type = "STRENGTH",
                                date = dateString,
                                duration = durationMinutes,
                                distance = null,
                                notes = null,
                                profileId = user.id
                            )
                            SupabaseClient.client.postgrest.from("workouts").insert(workout)
                            isSaving = false
                            navController.navigate(
                                Screen.WorkoutCompleted.createRoute(
                                    duration = formatTime(elapsedTime),
                                    distance = "0.00 km",
                                    calories = calories,
                                )
                            )
                        } catch (e: Exception) {
                            saveError = e.message
                            isSaving = false
                        }
                    }
                },
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
                    text = strings.stopWorkout,
                    fontSize = 18.sp
                )
            }

            if (isSaving) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            if (saveError != null) {
                Text(
                    text = "Fout bij opslaan: $saveError",
                    color = MaterialTheme.colorScheme.error
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
                        unitSystem = unitSystem,
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

    if (showStopWordDialog) {
        AlertDialog(
            onDismissRequest = { showStopWordDialog = false },
            title = { Text("Stel stopwoord in") },
            text = {
                OutlinedTextField(
                    value = tempStopWord,
                    onValueChange = { tempStopWord = it },
                    label = { Text("Stopwoord") }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        customStopWord = tempStopWord.trim().ifEmpty { "stop" }
                        voiceCommandsEnabled = true
                        showStopWordDialog = false
                    },
                    enabled = isStopWordValid
                ) { Text("Bevestigen") }
            },
            dismissButton = {
                TextButton(onClick = { showStopWordDialog = false }) { Text("Annuleren") }
            }
        )
    }

    if (voiceCommandsEnabled && hasAudioPermission) {
        VoiceCommandListener(
            enabled = true,
            stopWord = customStopWord,
            onFinishSet = {
                val exercise = exercises.firstOrNull { ex -> ex.sets.any { !it.completed } }
                val setIndex = exercise?.sets?.indexOfFirst { !it.completed } ?: -1
                if (exercise != null && setIndex != -1) {
                    val updatedSets = exercise.sets.toMutableList()
                    updatedSets[setIndex] = updatedSets[setIndex].copy(completed = true)
                    val exerciseIndex = exercises.indexOf(exercise)
                    exercises[exerciseIndex] = exercise.copy(sets = updatedSets)
                    val restTime = updatedSets[setIndex].restTime
                    activeRestTimerExercise = exercise.name
                    activeRestTimerSetIndex = setIndex
                    restTimeRemaining = restTime
                    timerActive = true
                }
            }
        )
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
                            text = "${UnitConverter.formatWeight(set.weight, unitSystem)}×${set.reps}",
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

@Composable
fun CompactRestTimer(remainingSeconds: Int) {
    val strings = strings()
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
            text = String.format(strings.restTimeFormat, formattedTime),
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
    val strings = strings()
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
                        text = strings.exercises,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = strings.close)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Search field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(strings.searchExercises) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = strings.search) },
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

@Composable
fun PresetMenuDialog(
    availableExercises: List<AvailableExercise>,
    unitSystem: UnitSystem,
    onDismiss: () -> Unit,
    onPresetSelected: (List<Exercise>) -> Unit
) {
    val strings = strings()
    val presets = remember(availableExercises, unitSystem) {
        createWorkoutPresets(availableExercises, unitSystem, strings)
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
                .height(400.dp),
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
                        text = strings.workoutPresets,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = strings.close)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Preset list
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    items(presets) { preset ->
                        PresetItem(
                            presetName = preset.name,
                            exerciseCount = preset.exercises.size,
                            onPresetClick = { onPresetSelected(preset.exercises) }
                        )
                        if (preset != presets.last()) {
                            Divider()
                        }
                    }
                }
            }
        }
    }
}

data class WorkoutPreset(
    val name: String,
    val exercises: List<Exercise>
)

// Function to create dynamic presets based on available exercises
fun createWorkoutPresets(
    availableExercises: List<AvailableExercise>,
    unitSystem: UnitSystem,
    strings: StringResources
): List<WorkoutPreset> {
    val presets = mutableListOf<WorkoutPreset>()

    val exercisesByMuscle = availableExercises.groupBy { it.muscleGroup.lowercase() }

    val lightWeight = if (unitSystem == UnitSystem.IMPERIAL) 33.0 else 15.0
    val mediumWeight = if (unitSystem == UnitSystem.IMPERIAL) 44.0 else 20.0
    val heavyWeight = if (unitSystem == UnitSystem.IMPERIAL) 88.0 else 40.0

    val pushMuscles = listOf("borst", "chest", "schouders", "shoulders", "armen", "arms", "triceps")
    val pushExercises = exercisesByMuscle.filterKeys { muscle ->
        pushMuscles.any { pushMuscle -> muscle.contains(pushMuscle, ignoreCase = true) }
    }.values.flatten().take(4)

    if (pushExercises.isNotEmpty()) {
        presets.add(WorkoutPreset(
            name = strings.pushWorkout,
            exercises = pushExercises.map { exercise ->
                Exercise(
                    name = exercise.name,
                    muscleGroup = exercise.muscleGroup,
                    sets = listOf(
                        ExerciseSet(reps = 10, weight = mediumWeight),
                        ExerciseSet(reps = 10, weight = mediumWeight),
                        ExerciseSet(reps = 10, weight = mediumWeight)
                    ),
                    caloriesPerRep = exercise.caloriesPerRep
                )
            }
        ))
    }

    val pullMuscles = listOf("rug", "back", "biceps", "bicep")
    val pullExercises = exercisesByMuscle.filterKeys { muscle ->
        pullMuscles.any { pullMuscle -> muscle.contains(pullMuscle, ignoreCase = true) }
    }.values.flatten().take(4)

    if (pullExercises.isNotEmpty()) {
        presets.add(WorkoutPreset(
            name = strings.pullWorkout,
            exercises = pullExercises.map { exercise ->
                Exercise(
                    name = exercise.name,
                    muscleGroup = exercise.muscleGroup,
                    sets = listOf(
                        ExerciseSet(reps = 12, weight = lightWeight),
                        ExerciseSet(reps = 12, weight = lightWeight),
                        ExerciseSet(reps = 12, weight = lightWeight)
                    ),
                    caloriesPerRep = exercise.caloriesPerRep
                )
            }
        ))
    }

    val legMuscles = listOf("benen", "legs", "leg", "quadriceps", "hamstring", "calves")
    val legExercises = exercisesByMuscle.filterKeys { muscle ->
        legMuscles.any { legMuscle -> muscle.contains(legMuscle, ignoreCase = true) }
    }.values.flatten().take(4)

    if (legExercises.isNotEmpty()) {
        presets.add(WorkoutPreset(
            name = strings.legWorkout,
            exercises = legExercises.map { exercise ->
                Exercise(
                    name = exercise.name,
                    muscleGroup = exercise.muscleGroup,
                    sets = listOf(
                        ExerciseSet(reps = 12, weight = heavyWeight),
                        ExerciseSet(reps = 12, weight = heavyWeight),
                        ExerciseSet(reps = 10, weight = heavyWeight)
                    ),
                    caloriesPerRep = exercise.caloriesPerRep
                )
            }
        ))
    }

    if (availableExercises.size >= 5) {
        val fullBodyExercises = availableExercises.shuffled().take(5)
        presets.add(WorkoutPreset(
            name = strings.fullBodyWorkout,
            exercises = fullBodyExercises.map { exercise ->
                Exercise(
                    name = exercise.name,
                    muscleGroup = exercise.muscleGroup,
                    sets = listOf(
                        ExerciseSet(reps = 10, weight = mediumWeight),
                        ExerciseSet(reps = 10, weight = mediumWeight)
                    ),
                    caloriesPerRep = exercise.caloriesPerRep
                )
            }
        ))
    }

    return presets
}

@Composable
fun PresetItem(
    presetName: String,
    exerciseCount: Int,
    onPresetClick: () -> Unit
) {
    val strings = strings()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onPresetClick)
            .padding(vertical = 16.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.FitnessCenter,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = presetName,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = String.format(strings.exercisesCountFormat, exerciseCount),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Icon(
            Icons.Default.Add,
            contentDescription = strings.add,
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun VoiceCommandListener(
    enabled: Boolean,
    stopWord: String,
    onFinishSet: () -> Unit
) {
    val context = LocalContext.current
    val speechRecognizer = remember { SpeechRecognizer.createSpeechRecognizer(context) }
    val intent = remember {
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        }
    }

    DisposableEffect(enabled, stopWord) {
        if (enabled) {
            val listener = object : RecognitionListener {
                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (matches?.any { it.contains(stopWord, ignoreCase = true) } == true) {
                        onFinishSet()
                    }
                    speechRecognizer.startListening(intent)
                }
                override fun onError(error: Int) { speechRecognizer.startListening(intent) }
                override fun onReadyForSpeech(params: Bundle?) {}
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {}
                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            }
            speechRecognizer.setRecognitionListener(listener)
            speechRecognizer.startListening(intent)
        }
        onDispose { speechRecognizer.destroy() }
    }
}