package com.example.workoutbuddyapplication.screens

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.workoutbuddyapplication.components.*
import com.example.workoutbuddyapplication.models.*
import com.example.workoutbuddyapplication.navigation.Screen
import com.example.workoutbuddyapplication.ui.theme.*
import com.example.workoutbuddyapplication.viewmodel.StrengthWorkoutViewModel
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class ExerciseSet(
    val reps: Int,
    val weight: Double,
    val completed: Boolean = false,
    val restTime: Int = 60
)

data class Exercise(
    val name: String,
    var sets: List<ExerciseSet>,
    val muscleGroup: String = "Algemeen",
    val notes: String = "",
    val device: ExerciseDevice? = null,
)

data class AvailableExercise(
    val name: String,
    val muscleGroup: String,
    val equipment: String,
)

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun StrengthWorkoutScreen(
    navController: NavController,
    viewModel: StrengthWorkoutViewModel = viewModel()
) {
    val strings = strings()
    val context = LocalContext.current
    val preferencesManager = remember { UserPreferencesManager(context) }
    val selectedUnitSystem by preferencesManager.selectedUnitSystem.collectAsState(initial = "metric")
    val unitSystem = selectedUnitSystem.toUnitSystem()

    val session = viewModel.session
    val elapsedTime by session.elapsedTime.collectAsState()

    var currentExerciseForDevice by remember { mutableStateOf<Exercise?>(null) }
    var showExerciseSelector by remember { mutableStateOf(false) }
    var showPresetMenu by remember { mutableStateOf(false) }

    var activeRestTimerExercise by remember { mutableStateOf<String?>(null) }
    var activeRestTimerSetIndex by remember { mutableIntStateOf(-1) }
    var restTimeRemaining by remember { mutableIntStateOf(0) }
    var timerActive by remember { mutableStateOf(false) }

    val exercises = remember { mutableStateListOf<Exercise>() }

    var availableExercises by remember { mutableStateOf<List<AvailableExercise>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    val isSaving by viewModel.isSaving.collectAsState()
    val saveError by viewModel.saveError.collectAsState()
    val workoutNotes by viewModel.workoutNotes.collectAsState()

    val coroutineScope = rememberCoroutineScope()

    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.RECORD_AUDIO
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
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
    val isStopWordValid =
        tempStopWord.trim().isNotEmpty() && tempStopWord.all { it.isLetter() || it.isWhitespace() }

    var showQRScannerDialog by remember { mutableStateOf(false) }
    var scannedQrValue by remember { mutableStateOf<String?>(null) }
    var showExerciseDetailDialog by remember { mutableStateOf(false) }
    var selectedExerciseName by remember { mutableStateOf<String?>(null) }

    var showNotesDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { session.start() }

    LaunchedEffect(voiceCommandsEnabled) {
        if (voiceCommandsEnabled && !hasAudioPermission) {
            audioPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
        }
    }

    LaunchedEffect(key1 = true) {
        coroutineScope.launch {
            try {
                val exerciseDTOs =
                    com.example.workoutbuddyapplication.data.SupabaseClient.client.postgrest
                        .from("exercises")
                        .select()
                        .decodeList<ExerciseDTO>()

                availableExercises = exerciseDTOs.map { dto ->
                    AvailableExercise(
                        name = dto.name,
                        muscleGroup = dto.primary_muscles.firstOrNull() ?: "Algemeen",
                        equipment = dto.equipment ?: "Bodyweight",
                    )
                }
                isLoading = false
                error = null
            } catch (e: Exception) {
                availableExercises = listOf(
                    AvailableExercise(
                        name = "Bench Press",
                        muscleGroup = "Borst",
                        equipment = "Barbell"
                    ),
                    AvailableExercise(
                        name = "Deadlift",
                        muscleGroup = "Rug",
                        equipment = "Barbell"
                    ),
                    AvailableExercise(
                        name = "Squat",
                        muscleGroup = "Benen",
                        equipment = "Barbell"
                    ),
                    AvailableExercise(
                        name = "Shoulder Press",
                        muscleGroup = "Schouders",
                        equipment = "Dumbbell"
                    ),
                    AvailableExercise(
                        name = "Bicep Curls",
                        muscleGroup = "Armen",
                        equipment = "Dumbbell"
                    ),
                    AvailableExercise(
                        name = "Lat Pulldown",
                        muscleGroup = "Rug",
                        equipment = "Cable Machine"
                    )
                )
                isLoading = false
                error = null
            }
        }
    }

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

    if (showExerciseSelector) {
        ExerciseSelectorDialog(
            availableExercises = availableExercises,
            isLoading = isLoading,
            error = error,
            onDismiss = { showExerciseSelector = false },
            onExerciseSelected = { selectedExercise ->
                val defaultWeight = if (unitSystem == UnitSystem.IMPERIAL) 45.0 else 20.0
                val newExercise = Exercise(
                    name = selectedExercise.name,
                    muscleGroup = selectedExercise.muscleGroup,
                    sets = listOf(
                        ExerciseSet(reps = 10, weight = defaultWeight)
                    )
                )
                exercises.add(newExercise)
                showExerciseSelector = false
            }
        )
    }

    if (showPresetMenu) {
        PresetMenuDialog(
            availableExercises = availableExercises,
            unitSystem = unitSystem,
            onDismiss = { showPresetMenu = false },
            onPresetSelected = { presetExercises ->
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

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(101.dp)
                        .clickable { showQRScannerDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(vertical = 16.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Scan Machine",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Scan Machine",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = { showPresetMenu = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
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

            Button(
                onClick = { showNotesDialog = true },
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

            if (showNotesDialog) {
                AlertDialog(
                    onDismissRequest = { showNotesDialog = false },
                    title = { Text("Add Notes") },
                    text = {
                        OutlinedTextField(
                            value = workoutNotes,
                            onValueChange = { viewModel.setWorkoutNotes(it) },
                            label = { Text("Notes") }
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showNotesDialog = false
                                viewModel.saveWorkout(elapsedTime) { duration, notes ->
                                    navController.navigate(
                                        Screen.StrengthWorkoutCompleted.createRoute(
                                            duration = formatTime(elapsedTime),
                                            notes = notes
                                        )
                                    )
                                }
                            }
                        ) { Text("Save") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showNotesDialog = false }) { Text("Cancel") }
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

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
                        onSetCompleted = { reps, wasCompleted -> },
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

    if (showQRScannerDialog) {
        Dialog(onDismissRequest = { showQRScannerDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp),
                    contentAlignment = Alignment.Center
                ) {
                    QRScanner(
                        onQrCodeScanned = { qrValue ->
                            scannedQrValue = qrValue
                            showQRScannerDialog = false
                        },
                        onClose = { showQRScannerDialog = false }
                    )
                }
            }
        }
    }

    if (scannedQrValue != null) {
        var machine by remember { mutableStateOf<Machine?>(null) }
        var exerciseName by remember { mutableStateOf<String?>(null) }
        var isLoading by remember { mutableStateOf(true) }

        LaunchedEffect(scannedQrValue) {
            isLoading = true
            try {
                machine = com.example.workoutbuddyapplication.data.SupabaseClient.client.postgrest
                    .from("machines?id=eq.${scannedQrValue!!.toLong()}")
                    .select(columns = io.github.jan.supabase.postgrest.query.Columns.list("*"))
                    .decodeSingle<Machine>()

                val exerciseResult =
                    com.example.workoutbuddyapplication.data.SupabaseClient.client.postgrest
                        .from("exercises?id=eq.${machine!!.exercise}")
                        .select(columns = io.github.jan.supabase.postgrest.query.Columns.list("name"))
                        .decodeSingle<Map<String, String>>()
                exerciseName = exerciseResult["name"].toString()
            } catch (e: Exception) {
                machine = null
                exerciseName = null
            }
            isLoading = false
        }

        AlertDialog(
            onDismissRequest = { scannedQrValue = null },
            title = { Text(machine?.name ?: "Machine") },
            text = {
                if (isLoading) {
                    CircularProgressIndicator()
                } else if (machine != null && exerciseName != null) {
                    Column {
                        machine!!.image?.let { imageUrl ->
                            androidx.compose.foundation.Image(
                                painter = rememberAsyncImagePainter(imageUrl),
                                contentDescription = machine!!.name,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                            )
                            Spacer(Modifier.height(16.dp))
                        }
                        Text(
                            text = "Exercise:",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                .clickable {
                                    selectedExerciseName = exerciseName
                                    showExerciseDetailDialog = true
                                }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = exerciseName!!,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                } else {
                    Text("Failed to load machine or exercise info.")
                }
            },
            confirmButton = {
                Button(onClick = { scannedQrValue = null }) {
                    Text("OK")
                }
            }
        )
    }

    if (showExerciseDetailDialog && selectedExerciseName != null) {
        var exercise by remember {
            mutableStateOf<com.example.workoutbuddyapplication.models.Exercise?>(
                null
            )
        }
        var isLoading by remember { mutableStateOf(true) }
        var error by remember { mutableStateOf<String?>(null) }

        LaunchedEffect(selectedExerciseName) {
            isLoading = true
            error = null
            try {
                exercise = fetchExerciseByName(selectedExerciseName!!)
            } catch (e: Exception) {
                error = "Could not load exercise."
            }
            isLoading = false
        }

        Dialog(
            onDismissRequest = { showExerciseDetailDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.background
            ) {
                Box(Modifier.fillMaxSize()) {
                    when {
                        isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                        error != null -> Text(
                            error!!,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.align(Alignment.Center)
                        )

                        exercise != null -> ExerciseDetailContent(exercise!!)
                    }
                    IconButton(
                        onClick = { showExerciseDetailDialog = false },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
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