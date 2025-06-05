package com.example.workoutbuddyapplication.screens

import android.os.SystemClock
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.workoutbuddyapplication.R
import com.example.workoutbuddyapplication.navigation.Screen
import com.example.workoutbuddyapplication.utils.formatTime
import com.example.workoutbuddyapplication.screens.StatCard
import kotlinx.coroutines.delay

data class YogaPose(
    val name: String,
    val duration: Int, // in seconds
    val description: String,
    val difficulty: String = "Beginner", // Beginner, Intermediate, Advanced
    val benefits: String = "",
    val breathingPattern: String = "Normale ademhaling"
)

data class YogaRoutine(
    val name: String,
    val description: String,
    val poses: List<YogaPose>,
    val duration: Int, // Total duration in minutes
    val level: String // Beginner, Intermediate, Advanced
)

@Composable
fun YogaWorkoutScreen(navController: NavController) {
    var isRunning by remember { mutableStateOf(true) }
    var elapsedTime by remember { mutableLongStateOf(0L) }
    var calories by remember { mutableIntStateOf(0) }

    val yogaRoutines = remember {
        mutableStateListOf(
            YogaRoutine(
                name = "Morning Flow",
                description = "Een zachte routine om de dag te beginnen en het lichaam wakker te maken.",
                poses = listOf(
                    YogaPose(
                        name = "Mountain Pose",
                        duration = 60,
                        description = "Sta rechtop, voeten bij elkaar, armen langs je zij.",
                        benefits = "Improves posture, balance, and body awareness"
                    ),
                    YogaPose(
                        name = "Downward-Facing Dog",
                        duration = 90,
                        description = "Forms an inverted V with your body, hands and feet on the ground.",
                        benefits = "Stretches the back, strengthens arms and shoulders"
                    ),
                    YogaPose(
                        name = "Warrior I",
                        duration = 60,
                        description = "Lunge with one leg forward, arms stretched upwards.",
                        difficulty = "Intermediate",
                        benefits = "Strengthens legs, improves balance and focus"
                    ),
                    YogaPose(
                        name = "Warrior II",
                        duration = 60,
                        description = "Lunge with one leg forward, arms stretched out to the sides.",
                        difficulty = "Intermediate",
                        benefits = "Opens hips, strengthens legs"
                    ),
                    YogaPose(
                        name = "Child's Pose",
                        duration = 120,
                        description = "Kneel with your forehead on the ground, arms alongside your body or stretched out.",
                        benefits = "Relaxes the back, calms the mind"
                    )
                ),
                duration = 15,
                level = "Beginner"
            ),
            YogaRoutine(
                name = "Power & Balance",
                description = "Een uitdagende routine gericht op kracht en evenwicht.",
                poses = listOf(
                    YogaPose(
                        name = "Tree Pose",
                        duration = 45,
                        description = "Stand on one leg, place the sole of the other foot against your inner thigh.",
                        difficulty = "Intermediate",
                        benefits = "Improves balance and concentration"
                    ),
                    YogaPose(
                        name = "Chair Pose",
                        duration = 60,
                        description = "Bend your knees as if sitting in a chair, arms up.",
                        benefits = "Strengthens legs and core"
                    ),
                    YogaPose(
                        name = "Plank Pose",
                        duration = 45,
                        description = "Keep your body in a straight line, supported on hands and toes.",
                        difficulty = "Intermediate",
                        benefits = "Strengthens core, arms and shoulders",
                        breathingPattern = "Diepe ademhaling"
                    ),
                    YogaPose(
                        name = "Side Plank Pose",
                        duration = 30,
                        description = "From plank, turn to one side, supporting on one hand and the side of your feet.",
                        difficulty = "Advanced",
                        benefits = "Strengthens arms, shoulders and core"
                    ),
                    YogaPose(
                        name = "Eagle Pose",
                        duration = 45,
                        description = "Cross your arms and legs in front of each other in a twisted position.",
                        difficulty = "Intermediate",
                        benefits = "Improves balance, concentration and coordination"
                    )
                ),
                duration = 20,
                level = "Intermediate"
            )
        )
    }

    var selectedRoutineIndex by remember { mutableIntStateOf(0) }
    var currentPoseIndex by remember { mutableIntStateOf(0) }
    var poseTimeRemaining by remember { mutableIntStateOf(yogaRoutines[selectedRoutineIndex].poses[0].duration) }

    var showAddRoutine by remember { mutableStateOf(false) }
    var showRoutineSelector by remember { mutableStateOf(false) }
    var showAddPose by remember { mutableStateOf(false) }

    var newRoutineName by remember { mutableStateOf("") }
    var newRoutineDescription by remember { mutableStateOf("") }
    var newRoutineLevel by remember { mutableStateOf("Beginner") }

    var newPoseName by remember { mutableStateOf("") }
    var newPoseDuration by remember { mutableIntStateOf(60) }
    var newPoseDescription by remember { mutableStateOf("") }
    var newPoseDifficulty by remember { mutableStateOf("Beginner") }
    var newPoseBenefits by remember { mutableStateOf("") }

    LaunchedEffect(isRunning) {
        val startTime = SystemClock.elapsedRealtime() - elapsedTime
        while (isRunning) {
            elapsedTime = SystemClock.elapsedRealtime() - startTime
            delay(1000)
            if (isRunning) {
                calories = (elapsedTime / 60000 * 3).toInt()
            }
        }
    }

    LaunchedEffect(isRunning, currentPoseIndex, selectedRoutineIndex) {
        if (yogaRoutines.isNotEmpty() && selectedRoutineIndex < yogaRoutines.size) {
        val currentRoutine = yogaRoutines[selectedRoutineIndex]
        if (currentPoseIndex < currentRoutine.poses.size) {
            poseTimeRemaining = currentRoutine.poses[currentPoseIndex].duration
            while (isRunning && poseTimeRemaining > 0) {
                delay(1000)
                poseTimeRemaining--
            }
            if (poseTimeRemaining <= 0 && currentPoseIndex < currentRoutine.poses.size - 1) {
                currentPoseIndex++
                }
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                FloatingActionButton(
                    onClick = { isRunning = !isRunning },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Icon(
                        if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isRunning) stringResource(R.string.pause) else stringResource(R.string.resume)
                    )
                }
                FloatingActionButton(
                    onClick = {
                        val formattedDuration = formatTime(elapsedTime)
                        navController.navigate(
                            Screen.WorkoutCompleted.createRoute(
                                duration = formattedDuration,
                                distance = "0.00 km",
                                calories = calories,
                                steps = 0
                            )
                        )
                    },
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ) {
                    Icon(Icons.Default.Stop, contentDescription = stringResource(R.string.stop_workout))
                }
            }
        }
    ) { paddingValues ->
        if (yogaRoutines.isEmpty()) {
            // Handle empty state
        } else {
            val currentRoutine = yogaRoutines[selectedRoutineIndex]
            val currentPose = currentRoutine.poses[currentPoseIndex]

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    // Header, Stats, Current Pose sections
                    YogaHeader(currentRoutine)
                    Spacer(modifier = Modifier.height(24.dp))
                    WorkoutStats(elapsedTime, calories)
                    Spacer(modifier = Modifier.height(24.dp))
                    CurrentPoseCard(currentPose, poseTimeRemaining)
                    Spacer(modifier = Modifier.height(24.dp))
                    if (currentPoseIndex < currentRoutine.poses.size - 1) {
                        NextPosePreview(currentRoutine.poses[currentPoseIndex + 1])
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                    ManagementButtons(
                        onSelectRoutine = { showRoutineSelector = true },
                        onAddRoutine = { showAddRoutine = true }
                    )
                }
            }

            if (showRoutineSelector) {
                RoutineSelectorDialog(
                    routines = yogaRoutines,
                    onDismiss = { showRoutineSelector = false },
                    onSelect = { index ->
                        selectedRoutineIndex = index
                        currentPoseIndex = 0
                        showRoutineSelector = false
                    }
                )
            }

            if (showAddRoutine) {
                AddRoutineDialog(
                    onDismiss = { showAddRoutine = false },
                    onAdd = { name, description, level ->
                        val newRoutine = YogaRoutine(
                            name = name,
                            description = description,
                            poses = emptyList(), // Start with an empty list of poses
                            duration = 0,
                            level = level
                        )
                        yogaRoutines.add(newRoutine)
                        showAddRoutine = false
                    }
                )
            }
        }
    }
}

// Helper Composables for YogaWorkoutScreen
@Composable
private fun YogaHeader(routine: YogaRoutine) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            Icons.Default.SelfImprovement,
            contentDescription = stringResource(R.string.yoga),
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = routine.name,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = routine.description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun WorkoutStats(elapsedTime: Long, calories: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        StatCard(
            title = stringResource(R.string.time),
            value = formatTime(elapsedTime),
            icon = Icons.Default.Timer
        )
        StatCard(
            title = stringResource(R.string.calories),
            value = "$calories kcal",
            icon = Icons.Default.LocalFireDepartment
        )
    }
}

@Composable
private fun CurrentPoseCard(pose: YogaPose, timeRemaining: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = pose.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = formatTime(timeRemaining * 1000L),
                fontSize = 48.sp,
                fontWeight = FontWeight.Light,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = pose.description,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${stringResource(R.string.difficulty)}: ${pose.difficulty}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${stringResource(R.string.breathing)}: ${pose.breathingPattern}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun NextPosePreview(pose: YogaPose) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = stringResource(R.string.next_pose),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "${stringResource(R.string.next)}: ${pose.name} (${formatTime(pose.duration * 1000L)})",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ManagementButtons(
    onSelectRoutine: () -> Unit,
    onAddRoutine: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Button(
            onClick = onSelectRoutine,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.select_other_routine))
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = onAddRoutine,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.create_new_routine))
        }
    }
}

@Composable
private fun RoutineSelectorDialog(routines: List<YogaRoutine>, onDismiss: () -> Unit, onSelect: (Int) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.choose_yoga_routine)) },
        text = {
            LazyColumn {
                items(routines.size) { index ->
                    val routine = routines[index]
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(routine.name, fontWeight = FontWeight.Medium)
                            Text("${routine.duration} min | ${routine.level}", style = MaterialTheme.typography.bodySmall)
                        }
                        Button(onClick = { onSelect(index) }) { Text(stringResource(R.string.select)) }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.close)) } }
    )
}

@Composable
private fun AddRoutineDialog(onDismiss: () -> Unit, onAdd: (String, String, String) -> Unit) {
    var newRoutineName by remember { mutableStateOf("") }
    var newRoutineDescription by remember { mutableStateOf("") }
    var newRoutineLevel by remember { mutableStateOf("Beginner") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.new_yoga_routine)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = newRoutineName, 
                    onValueChange = { newRoutineName = it }, 
                    label = { Text(stringResource(R.string.routine_name)) }
                )
                OutlinedTextField(
                    value = newRoutineDescription, 
                    onValueChange = { newRoutineDescription = it }, 
                    label = { Text(stringResource(R.string.routine_description)) }
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                onAdd(newRoutineName, newRoutineDescription, newRoutineLevel)
            }) { Text(stringResource(R.string.save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}
