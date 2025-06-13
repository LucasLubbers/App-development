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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.workoutbuddyapplication.navigation.Screen
import com.example.workoutbuddyapplication.ui.theme.strings
import com.example.workoutbuddyapplication.screens.formatTime
import com.example.workoutbuddyapplication.screens.StatCard
import kotlinx.coroutines.delay
import com.example.workoutbuddyapplication.services.NotificationService
import androidx.compose.ui.platform.LocalContext

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
    val strings = strings()
    var isRunning by remember { mutableStateOf(true) }
    var elapsedTime by remember { mutableLongStateOf(0L) }
    var calories by remember { mutableIntStateOf(0) }

    val yogaRoutines = remember {
        mutableStateListOf(
            YogaRoutine(
                name = strings.morningFlow,
                description = strings.morningFlowDescription,
                poses = listOf(
                    YogaPose(
                        name = strings.mountainPose,
                        duration = 60,
                        description = strings.mountainPoseDescription,
                        benefits = "Improves posture, balance, and body awareness"
                    ),
                    YogaPose(
                        name = strings.downwardDog,
                        duration = 90,
                        description = "Forms an inverted V with your body, hands and feet on the ground.",
                        benefits = "Stretches the back, strengthens arms and shoulders"
                    ),
                    YogaPose(
                        name = strings.warriorOne,
                        duration = 60,
                        description = "Lunge with one leg forward, arms stretched upwards.",
                        difficulty = strings.intermediate,
                        benefits = "Strengthens legs, improves balance and focus"
                    ),
                    YogaPose(
                        name = strings.warriorTwo,
                        duration = 60,
                        description = "Lunge with one leg forward, arms stretched out to the sides.",
                        difficulty = strings.intermediate,
                        benefits = "Opens hips, strengthens legs"
                    ),
                    YogaPose(
                        name = strings.childsPose,
                        duration = 120,
                        description = "Kneel with your forehead on the ground, arms alongside your body or stretched out.",
                        benefits = "Relaxes the back, calms the mind"
                    )
                ),
                duration = 15,
                level = strings.beginner
            ),
            YogaRoutine(
                name = strings.powerAndBalance,
                description = strings.powerAndBalanceDescription,
                poses = listOf(
                    YogaPose(
                        name = strings.treePose,
                        duration = 45,
                        description = "Stand on one leg, place the sole of the other foot against your inner thigh.",
                        difficulty = strings.intermediate,
                        benefits = "Improves balance and concentration"
                    ),
                    YogaPose(
                        name = strings.chairPose,
                        duration = 60,
                        description = "Bend your knees as if sitting in a chair, arms up.",
                        benefits = "Strengthens legs and core"
                    ),
                    YogaPose(
                        name = strings.plankPose,
                        duration = 45,
                        description = "Keep your body in a straight line, supported on hands and toes.",
                        difficulty = strings.intermediate,
                        benefits = "Strengthens core, arms and shoulders",
                        breathingPattern = strings.deepBreathing
                    ),
                    YogaPose(
                        name = strings.sidePlankPose,
                        duration = 30,
                        description = "From plank, turn to one side, supporting on one hand and the side of your feet.",
                        difficulty = strings.advanced,
                        benefits = "Strengthens arms, shoulders and core"
                    ),
                    YogaPose(
                        name = strings.eaglePose,
                        duration = 45,
                        description = "Cross your arms and legs in front of each other in a twisted position.",
                        difficulty = strings.intermediate,
                        benefits = "Improves balance, concentration and coordination"
                    )
                ),
                duration = 20,
                level = strings.intermediate
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
    var newRoutineLevel by remember { mutableStateOf(strings.beginner) }

    var newPoseName by remember { mutableStateOf("") }
    var newPoseDuration by remember { mutableIntStateOf(60) }
    var newPoseDescription by remember { mutableStateOf("") }
    var newPoseDifficulty by remember { mutableStateOf(strings.beginner) }
    var newPoseBenefits by remember { mutableStateOf("") }

    val context = LocalContext.current
    var hasSentTimeNotification by remember { mutableStateOf(false) }

    LaunchedEffect(isRunning) {
        val startTime = SystemClock.elapsedRealtime() - elapsedTime
        while (isRunning) {
            elapsedTime = SystemClock.elapsedRealtime() - startTime
            delay(1000)
            if (isRunning) {
                calories = (elapsedTime / 60000 * 3).toInt()
            }
        }
        if (!hasSentTimeNotification && elapsedTime > 3_600_000L) {
            NotificationService.createNotificationChannel(context)
            NotificationService.sendWorkoutTimeNotification(
                context,
                "Let op de tijd!",
                "Je bent al langer dan 1 uur bezig met je workout"
            )
            hasSentTimeNotification = true
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
                        contentDescription = if (isRunning) strings.pause else strings.resume
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
                    Icon(Icons.Default.Stop, contentDescription = strings.stopWorkout)
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
                    },
                    onAddNew = {
                        showRoutineSelector = false
                        showAddRoutine = true
                    }
                )
            }

            if (showAddRoutine) {
                AddOrEditRoutineDialog(
                    onDismiss = { showAddRoutine = false },
                    onSave = { newRoutine ->
                        yogaRoutines.add(newRoutine)
                        selectedRoutineIndex = yogaRoutines.size - 1
                        currentPoseIndex = 0
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
    val strings = strings()
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.SelfImprovement, contentDescription = strings.yoga, tint = MaterialTheme.colorScheme.onPrimaryContainer)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = routine.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(text = routine.description, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun WorkoutStats(elapsedTime: Long, calories: Int) {
    val strings = strings()
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        StatCard(title = strings.time, value = formatTime(elapsedTime), icon = Icons.Default.Timer, modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.width(8.dp))
        StatCard(title = strings.calories, value = "$calories kcal", icon = Icons.Default.LocalFireDepartment, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun CurrentPoseCard(pose: YogaPose, timeRemaining: Int) {
    val strings = strings()
    Text(text = strings.currentPose, style = MaterialTheme.typography.headlineSmall)
    Spacer(modifier = Modifier.height(8.dp))
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = pose.name, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(progress = { (pose.duration - timeRemaining).toFloat() / pose.duration }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "${formatTime(timeRemaining * 1000L)} ${strings.time} remaining", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = pose.description, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = String.format(strings.poseDifficulty, pose.difficulty), style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            if (pose.benefits.isNotEmpty()) {
                Text(text = String.format(strings.poseBenefits, pose.benefits), style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
            }
            Text(text = String.format(strings.poseBreathing, pose.breathingPattern), style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun NextPosePreview(pose: YogaPose) {
    val strings = strings()
    Text(text = strings.nextPoses, style = MaterialTheme.typography.headlineSmall)
    Spacer(modifier = Modifier.height(8.dp))
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = pose.name, fontWeight = FontWeight.Bold)
                Text(text = formatTime(pose.duration * 1000L), style = MaterialTheme.typography.bodySmall)
            }
            Icon(Icons.Default.ArrowForward, contentDescription = null)
        }
    }
}

@Composable
private fun ManagementButtons(onSelectRoutine: () -> Unit, onAddRoutine: () -> Unit) {
    val strings = strings()
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(onClick = onSelectRoutine, modifier = Modifier.weight(1f)) { Text(strings.chooseYogaRoutine) }
        OutlinedButton(onClick = onAddRoutine, modifier = Modifier.weight(1f)) { Text(strings.newYogaRoutine) }
    }
}

@Composable
private fun RoutineSelectorDialog(routines: List<YogaRoutine>, onDismiss: () -> Unit, onSelect: (Int) -> Unit, onAddNew: () -> Unit) {
    val strings = strings()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.chooseYogaRoutine) },
        text = {
            LazyColumn {
                items(routines.size) { index ->
                    val routine = routines[index]
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(routine.name, fontWeight = FontWeight.Medium)
                            Text("${routine.duration} min | ${routine.level}", style = MaterialTheme.typography.bodySmall)
                        }
                        Button(onClick = { onSelect(index) }) { Text(strings.select) }
                    }
                }
                item {
                    OutlinedButton(onClick = onAddNew, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(strings.newRoutine)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(strings.close) } }
    )
}

@Composable
private fun AddOrEditRoutineDialog(onDismiss: () -> Unit, onSave: (YogaRoutine) -> Unit, routineToEdit: YogaRoutine? = null) {
    val strings = strings()
    var name by remember { mutableStateOf(routineToEdit?.name ?: "") }
    var description by remember { mutableStateOf(routineToEdit?.description ?: "") }
    var level by remember { mutableStateOf(routineToEdit?.level ?: strings.beginner) }
    val poses = remember { mutableStateListOf<YogaPose>().apply { routineToEdit?.poses?.let { addAll(it) } } }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (routineToEdit == null) strings.newYogaRoutine else strings.editRoutine) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(strings.routineName) })
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text(strings.routineDescription) })
            }
        },
        confirmButton = {
            Button(onClick = {
                val newRoutine = YogaRoutine(name, description, poses, poses.sumOf { it.duration } / 60, level)
                onSave(newRoutine)
            }) { Text(strings.save) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(strings.cancel) } }
    )
}
