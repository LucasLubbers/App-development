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
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.workoutbuddyapplication.components.CartoMapStyle
import com.example.workoutbuddyapplication.components.OpenStreetMapView
import com.example.workoutbuddyapplication.components.StatCard
import com.example.workoutbuddyapplication.data.SupabaseClient
import com.example.workoutbuddyapplication.models.Workout
import com.example.workoutbuddyapplication.navigation.Screen
import com.example.workoutbuddyapplication.ui.theme.UnitSystem
import com.example.workoutbuddyapplication.ui.theme.UserPreferencesManager
import com.example.workoutbuddyapplication.ui.theme.toUnitSystem
import com.example.workoutbuddyapplication.utils.UnitConverter
import com.example.workoutbuddyapplication.workout.RunningSession
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RunningWorkoutScreen(navController: NavController) {
    KeepScreenOn()
    val context = LocalContext.current
    val preferencesManager = remember { UserPreferencesManager(context) }
    val selectedUnitSystem by preferencesManager.selectedUnitSystem.collectAsState(initial = "metric")
    val unitSystem = selectedUnitSystem.toUnitSystem()
    val debugMode by preferencesManager.debugMode.collectAsState(initial = false)

    val session = remember { RunningSession(context, preferencesManager, debugMode) }

    var showGoalDialog by remember { mutableStateOf(false) }
    var targetDistanceInput by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) }
    var isSaving by remember { mutableStateOf(false) }
    var saveError by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasLocationPermission = isGranted
    }
    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            locationPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    val isActive by session.isActive.collectAsState()
    val distance by session.distance.collectAsState()
    val elapsedTime by session.elapsedTime.collectAsState()
    val calories by session.calories.collectAsState()
    val speed by session.speed.collectAsState()
    val routePoints by session.routePoints.collectAsState()

    var targetDistance by remember { mutableStateOf(0.0) }

    val speedData = remember { mutableStateListOf<Float>() }
    LaunchedEffect(speed) {
        speedData.add(speed.toFloat())
    }

    LaunchedEffect(Unit) { session.start() }

    if (showGoalDialog) {
        AlertDialog(
            onDismissRequest = { showGoalDialog = false },
            title = { Text("Stel je doel in") },
            text = {
                Column {
                    Text("Afstand (km)")
                    OutlinedTextField(
                        value = targetDistanceInput,
                        onValueChange = { targetDistanceInput = it },
                        label = { Text("Doel afstand (${UnitConverter.getDistanceUnit(unitSystem)})") },
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
                        session.setTargetDistance(distanceInKm)
                        targetDistance = distanceInKm
                        showGoalDialog = false
                    }
                ) { Text("Bevestigen") }
            },
            dismissButton = {
                TextButton(onClick = { showGoalDialog = false }) { Text("Annuleren") }
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
                        contentDescription = if (isActive) "Pauzeren" else "Hervatten"
                    )
                }
                FloatingActionButton(
                    onClick = {
                        isSaving = true
                        saveError = null
                        val durationMinutes = (elapsedTime / 60000).toInt()
                        val distanceKm = distance
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
                                    type = "RUNNING",
                                    date = dateString,
                                    duration = durationMinutes,
                                    distance = distanceKm,
                                    notes = null,
                                    profileId = user.id
                                )
                                SupabaseClient.client.postgrest.from("workouts").insert(workout)
                                isSaving = false
                                navController.navigate(
                                    Screen.WorkoutCompleted.createRoute(
                                        duration = formatTime(elapsedTime),
                                        distance = UnitConverter.formatDistance(
                                            distance,
                                            unitSystem
                                        ),
                                        calories = calories,
                                    )
                                )
                            } catch (e: Exception) {
                                saveError = e.message
                                isSaving = false
                            }
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ) {
                    Icon(Icons.Default.Stop, contentDescription = "Stoppen")
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
                        Icons.Default.DirectionsRun,
                        contentDescription = "Hardlopen",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.padding(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Hardlopen",
                        fontSize = 24.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                    if (targetDistance > 0) {
                        Text(
                            text = "Doel: ${
                                UnitConverter.formatDistance(
                                    targetDistance,
                                    unitSystem
                                )
                            }",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Button(onClick = { showGoalDialog = true }) { Text("Doel instellen") }
            }

            Spacer(modifier = Modifier.height(24.dp))

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
                    title = "Afstand",
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
                    title = "Tempo",
                    value = if (speed > 0) {
                        val speedUnit = if (unitSystem == UnitSystem.IMPERIAL) "mi/h" else "km/h"
                        String.format("%.1f %s", speed, speedUnit)
                    } else "--:--",
                    icon = Icons.Default.Speed,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.padding(4.dp))
                StatCard(
                    title = "Calorieën",
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
                            text = "Voortgang naar doel",
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
                                text = "${
                                    UnitConverter.formatDistance(
                                        targetDistance,
                                        unitSystem
                                    )
                                }",
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
                    text = { Text("Tempo") }
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
                                    Text("Locatie toegang nodig voor kaart")
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = {
                                            locationPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
                                        }
                                    ) { Text("Toegang verlenen") }
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
                                Text("Nog geen tempogegevens beschikbaar")
                            }
                        }
                    }
                }
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
        }
    }
}

fun formatTime(timeInMillis: Long): String {
    val hours = (timeInMillis / (1000 * 60 * 60)) % 24
    val minutes = (timeInMillis / (1000 * 60)) % 60
    val seconds = (timeInMillis / 1000) % 60
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}