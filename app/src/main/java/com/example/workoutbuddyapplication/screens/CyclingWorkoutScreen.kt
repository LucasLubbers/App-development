package com.example.workoutbuddyapplication.screens

import KeepScreenOn
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Whatshot
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.workoutbuddyapplication.components.CartoMapStyle
import com.example.workoutbuddyapplication.components.OpenStreetMapView
import com.example.workoutbuddyapplication.components.StatCard
import com.example.workoutbuddyapplication.navigation.Screen
import com.example.workoutbuddyapplication.ui.theme.UnitSystem
import com.example.workoutbuddyapplication.ui.theme.UserPreferencesManager
import com.example.workoutbuddyapplication.ui.theme.toUnitSystem
import com.example.workoutbuddyapplication.utils.UnitConverter
import com.example.workoutbuddyapplication.viewmodel.CyclingWorkoutViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CyclingWorkoutScreen(navController: NavController) {
    KeepScreenOn()
    val context = LocalContext.current
    val preferencesManager = remember { UserPreferencesManager(context) }
    val selectedUnitSystem by preferencesManager.selectedUnitSystem.collectAsState(initial = "metric")
    val unitSystem = selectedUnitSystem.toUnitSystem()
    val debugMode by preferencesManager.debugMode.collectAsState(initial = false)

    val viewModel: CyclingWorkoutViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return CyclingWorkoutViewModel(context, preferencesManager, debugMode) as T
            }
        }
    )

    val session = viewModel.session

    var showGoalDialog by remember { mutableStateOf(false) }
    var targetDistanceInput by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) }

    val isSaving by viewModel.isSaving.collectAsState()
    val saveError by viewModel.saveError.collectAsState()
    val workoutNotes by viewModel.workoutNotes.collectAsState()
    val targetDistance by viewModel.targetDistance.collectAsState()

    val isActive by session.isActive.collectAsState()
    val distance by session.distance.collectAsState()
    val elapsedTime by session.elapsedTime.collectAsState()
    val calories by session.calories.collectAsState()
    val speed by session.speed.collectAsState()
    val routePoints by session.routePoints.collectAsState()

    var showNotesDialog by remember { mutableStateOf(false) }

    val speedData = remember { mutableStateListOf<Float>() }
    LaunchedEffect(speed) { speedData.add(speed.toFloat()) }
    LaunchedEffect(Unit) { session.start() }

    var hasLocationPermission by remember { mutableStateOf(false) }
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasLocationPermission = isGranted
    }
    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
    }

    if (showGoalDialog) {
        AlertDialog(
            onDismissRequest = { showGoalDialog = false },
            title = { Text("Set your goal") },
            text = {
                Column {
                    Text("Distance (${UnitConverter.getDistanceUnit(unitSystem)})")
                    OutlinedTextField(
                        value = targetDistanceInput,
                        onValueChange = { targetDistanceInput = it },
                        label = { Text("Target distance") },
                        keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val distanceInKm = UnitConverter.distanceToKm(
                            targetDistanceInput.toDoubleOrNull() ?: 5.0,
                            unitSystem
                        )
                        viewModel.setTargetDistance(distanceInKm)
                        showGoalDialog = false
                    }
                ) { Text("Confirm") }
            },
            dismissButton = {
                TextButton(onClick = { showGoalDialog = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                FloatingActionButton(
                    onClick = {
                        if (isActive) session.pause() else session.start()
                    },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Icon(
                        if (isActive) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isActive) "Pause" else "Resume"
                    )
                }
                FloatingActionButton(
                    onClick = { showNotesDialog = true },
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ) {
                    Icon(Icons.Default.Stop, contentDescription = "Stop")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
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
                        Icons.Default.DirectionsBike,
                        contentDescription = "Cycling",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.padding(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Cycling",
                        fontSize = 24.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                    if (targetDistance > 0) {
                        Text(
                            text = "Goal: ${UnitConverter.formatDistance(targetDistance, unitSystem)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Button(onClick = { showGoalDialog = true }) { Text("Set Goal") }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatCard(
                    title = "Time",
                    value = formatTime(elapsedTime),
                    icon = Icons.Default.Timer,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.padding(4.dp))
                StatCard(
                    title = "Distance",
                    value = UnitConverter.formatDistance(distance, unitSystem),
                    icon = Icons.Default.LocationOn,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatCard(
                    title = "Speed",
                    value = if (speed > 0) {
                        val speedUnit = if (unitSystem == UnitSystem.IMPERIAL) "mi/h" else "km/h"
                        String.format("%.1f %s", speed, speedUnit)
                    } else "--:--",
                    icon = Icons.Default.Speed,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.padding(4.dp))
                StatCard(
                    title = "Calories",
                    value = "$calories kcal",
                    icon = Icons.Outlined.Whatshot,
                    modifier = Modifier.weight(1f)
                )
            }

            if (targetDistance > 0) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Progress to goal",
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { (distance / targetDistance).coerceIn(0.0, 1.0).toFloat() },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${UnitConverter.formatDistance(distance, unitSystem)}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "${UnitConverter.formatDistance(targetDistance, unitSystem)}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Route") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Speed") }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    when (selectedTab) {
                        0 -> {
                            if (hasLocationPermission) {
                                OpenStreetMapView(
                                    modifier = Modifier.fillMaxSize(),
                                    currentLocation = routePoints.lastOrNull(),
                                    routePoints = routePoints,
                                    mapStyle = CartoMapStyle.POSITRON
                                )
                            } else {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text("Location permission required for map")
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = {
                                            locationPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
                                        }
                                    ) { Text("Grant access") }
                                }
                            }
                        }
                        1 -> {
                            val dataSize = speedData.size
                            if (dataSize > 0) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val maxSpeed =
                                        if (unitSystem == UnitSystem.IMPERIAL) 12f else 20f
                                    val minSpeed = 0f
                                    val width = size.width
                                    val height = size.height
                                    val xStep = width / (dataSize.coerceAtLeast(2) - 1)
                                    val speedUnit =
                                        if (unitSystem == UnitSystem.IMPERIAL) "mi/h" else "km/h"

                                    for (s in 0..maxSpeed.toInt() step 2) {
                                        val y =
                                            height - (s - minSpeed) / (maxSpeed - minSpeed) * height
                                        drawLine(
                                            color = Color.LightGray,
                                            start = androidx.compose.ui.geometry.Offset(0f, y),
                                            end = androidx.compose.ui.geometry.Offset(width, y),
                                            strokeWidth = 1f
                                        )
                                        drawContext.canvas.nativeCanvas.drawText(
                                            "$s $speedUnit",
                                            10f,
                                            y - 5,
                                            android.graphics.Paint().apply {
                                                color = android.graphics.Color.GRAY
                                                textSize = 30f
                                            }
                                        )
                                    }

                                    if (dataSize > 1) {
                                        val path = Path()
                                        val startX = 0f
                                        val startY = height - (speedData[0].coerceIn(
                                            minSpeed,
                                            maxSpeed
                                        ) - minSpeed) / (maxSpeed - minSpeed) * height
                                        path.moveTo(startX, startY)

                                        for (i in 1 until dataSize) {
                                            val x = i * xStep
                                            val y = height - (speedData[i].coerceIn(
                                                minSpeed,
                                                maxSpeed
                                            ) - minSpeed) / (maxSpeed - minSpeed) * height
                                            path.lineTo(x, y)
                                        }

                                        drawPath(
                                            path = path,
                                            color = Color.Blue,
                                            style = Stroke(width = 3f)
                                        )
                                    }
                                }
                            } else {
                                Text("No speed data available yet")
                            }
                        }
                    }
                }
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
                                viewModel.saveWorkout(
                                    elapsedTime = elapsedTime,
                                    distance = distance,
                                    calories = calories,
                                    unitSystem = unitSystem
                                ) { duration, formattedDistance, calories, notes ->
                                    navController.navigate(
                                        Screen.WorkoutCompleted.createRoute(
                                            duration = duration,
                                            distance = formattedDistance,
                                            calories = calories,
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
                    text = "Error saving: $saveError",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}