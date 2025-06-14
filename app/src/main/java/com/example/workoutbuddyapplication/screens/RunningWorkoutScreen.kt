package com.example.workoutbuddyapplication.screens

import android.content.Context
import android.os.SystemClock
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.workoutbuddyapplication.navigation.Screen
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.workoutbuddyapplication.ui.theme.UnitSystem
import com.example.workoutbuddyapplication.utils.UnitConverter
import androidx.compose.runtime.collectAsState
import com.example.workoutbuddyapplication.ui.theme.UserPreferencesManager
import com.example.workoutbuddyapplication.ui.theme.toUnitSystem
import com.example.workoutbuddyapplication.utils.LocationManager
import com.example.workoutbuddyapplication.utils.LatLng
import com.example.workoutbuddyapplication.components.OpenStreetMapView
import com.example.workoutbuddyapplication.components.CartoMapStyle
import androidx.compose.runtime.mutableStateListOf
import kotlinx.coroutines.launch
import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.core.content.ContextCompat
import androidx.compose.material.icons.outlined.Whatshot

@Composable
fun RunningWorkoutScreen(navController: NavController) {
    val context = LocalContext.current
    val preferencesManager = remember { UserPreferencesManager(context) }
    val selectedUnitSystem by preferencesManager.selectedUnitSystem.collectAsState(initial = "metric")
    val unitSystem = selectedUnitSystem.toUnitSystem()
    val debugMode by preferencesManager.debugMode.collectAsState(initial = false)
    val speedData = remember { mutableListOf<Float>() }


    // Permission handling
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasLocationPermission = isGranted
    }

    // Request permission on first load
    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    var isRunning by remember { mutableStateOf(true) }
    var distance by remember { mutableStateOf(0.0) }
    var pace by remember { mutableStateOf(0.0) }
    var elapsedTime by remember { mutableLongStateOf(0L) }
    var targetDistance by remember { mutableStateOf(0.0) }
    var targetTime by remember { mutableIntStateOf(0) }
    var showGoalDialog by remember { mutableStateOf(false) }
    var targetDistanceInput by remember { mutableStateOf("") }
    var targetTimeInput by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf(0) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var speed by remember { mutableStateOf(0.0) }


    // Real location tracking variables
    var currentLocation by remember { mutableStateOf<LatLng?>(null) }
    val routePoints = remember { mutableStateListOf<LatLng>() }
    var totalDistanceFromGPS by remember { mutableStateOf(0.0) }

    // Location manager
    val locationManager = remember { LocationManager(context) }

    // Simulated route points for the map (keeping as fallback)
    val simulatedRoutePoints = remember { mutableListOf<Offset>() }

    // Simulated elevation data
    val elevationData = remember { mutableListOf<Float>() }

    // Simulated pace data
    val paceData = remember { mutableListOf<Float>() }


    // Location tracking effect
    LaunchedEffect(isRunning, hasLocationPermission, debugMode) {
        if (isRunning && hasLocationPermission && !debugMode) { // Only use real GPS if not in debug mode
            locationManager.startLocationUpdates()

            // Collect location updates
            launch {
                locationManager.locationUpdates.collect { newLocation ->
                    currentLocation = newLocation

                    if (routePoints.isNotEmpty()) {
                        val lastPoint = routePoints.last()
                        val distanceToAdd = LocationManager.calculateDistance(lastPoint, newLocation) / 1000.0 // Convert to km
                        totalDistanceFromGPS += distanceToAdd

                        // Update distance with real GPS data
                        distance = totalDistanceFromGPS
                    }

                    routePoints.add(newLocation)
                }
            }
        } else {
            locationManager.stopLocationUpdates()
        }
    }

    // Get initial location
    LaunchedEffect(hasLocationPermission, debugMode) {
        if (debugMode) {
            // Use mock location for testing (Amsterdam coordinates)
            val mockLocation = LatLng(52.3676, 4.9041)
            currentLocation = mockLocation
            routePoints.add(mockLocation)
        } else if (hasLocationPermission) {
            val initialLocation = locationManager.getCurrentLocation()
            initialLocation?.let {
                currentLocation = it
                routePoints.add(it)
            }
        }
    }

    // Timer effect
    LaunchedEffect(isRunning) {
        val startTime = SystemClock.elapsedRealtime() - elapsedTime
        val userWeight = preferencesManager.getUserWeight()
        while (isRunning) {
            elapsedTime = SystemClock.elapsedRealtime() - startTime
            delay(1000)

            // Use real GPS distance if available, otherwise simulate
            if (debugMode && currentLocation != null) {
                // Debug mode: Simulate movement around mock location (stay near Amsterdam)
                distance += 0.0008f

                val lastLocation = routePoints.lastOrNull() ?: currentLocation!!

                // Make smaller, more controlled movements (about 1-2 meters per second)
                val angle = Math.random() * 2 * Math.PI
                val movementDistance = 0.00001 + Math.random() * 0.00001 // Very small movement in degrees
                val newLat = lastLocation.latitude + (cos(angle) * movementDistance)
                val newLng = lastLocation.longitude + (sin(angle) * movementDistance)

                // Ensure we stay near Amsterdam (52.3676, 4.9041) - within ~1km radius
                val amsterdamLat = 52.3676
                val amsterdamLng = 4.9041
                val maxDistance = 0.01 // About 1km in degrees

                val finalLat = if (kotlin.math.abs(newLat - amsterdamLat) > maxDistance) {
                    amsterdamLat + (if (newLat > amsterdamLat) maxDistance else -maxDistance)
                } else newLat

                val finalLng = if (kotlin.math.abs(newLng - amsterdamLng) > maxDistance) {
                    amsterdamLng + (if (newLng > amsterdamLng) maxDistance else -maxDistance)
                } else newLng

                val newLocation = LatLng(finalLat, finalLng)
                currentLocation = newLocation
                routePoints.add(newLocation)

                // Simulate realistic calories for running (about 10-15 kcal per minute for average person)
                val minutesElapsed = elapsedTime / 60000.0
                val expectedCalories = (minutesElapsed * (12 + Math.random() * 3)).toInt() // 12-15 kcal/min
                if (calories < expectedCalories) {
                    calories = expectedCalories
                }
            } else if (!hasLocationPermission || (routePoints.isEmpty() && !debugMode)) {
                // Simulate distance increase (about 3 km/h) when no GPS
                distance += 0.0008f

                // Add simulated points for fallback (only if no real GPS data)
                if (simulatedRoutePoints.isEmpty()) {
                    simulatedRoutePoints.add(Offset(500f, 500f))
                } else {
                    val lastPoint = simulatedRoutePoints.last()
                    val angle = Math.random() * 2 * Math.PI
                    val newX = lastPoint.x + (cos(angle) * 10).toFloat()
                    val newY = lastPoint.y + (sin(angle) * 10).toFloat()
                    simulatedRoutePoints.add(Offset(newX, newY))
                }
            }

            // Calculate pace (min/km)
            if (distance > 0 && elapsedTime > 0) {
                val hours = elapsedTime / 3_600_000.0
                speed = distance / hours
            } else {
                speed = 0.0
            }

            val met = when {
                speed < 2.0 -> 0.0
                speed < 8 -> 7.0
                speed < 9 -> 8.3
                speed < 10 -> 9.0
                speed < 11 -> 9.8
                speed < 12 -> 10.5
                speed < 13 -> 11.0
                else -> 11.5
            }

            if (met > 0) {
                val caloriesPerSecond = met * userWeight / 3600.0
                calories += caloriesPerSecond.toInt()
            }

            speedData.add(speed.toFloat())

            // Simulate pace data
            paceData.add(pace.toFloat())

            // Simulate elevation (between 0-50m)
            val newElevation = (Math.random() * 50).toFloat()
            elevationData.add(newElevation)
        }
    }

    // Stop location updates when screen is disposed
    DisposableEffect(Unit) {
        onDispose {
            locationManager.stopLocationUpdates()
        }
    }

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
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Tijd (minuten)")
                    Slider(
                        value = targetTime.toFloat(),
                        onValueChange = { targetTime = it.toInt() },
                        valueRange = 5f..180f,
                    )
                    Text("${targetTime} minuten")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Convert input to km for storage
                        val distanceInKm = UnitConverter.distanceToKm(
                            targetDistanceInput.toDoubleOrNull() ?: 5.0,
                            unitSystem
                        )
                        targetDistance = distanceInKm
                        targetTime = targetTimeInput.toIntOrNull() ?: 30
                        showGoalDialog = false
                    }
                ) {
                    Text("Bevestigen")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showGoalDialog = false }
                ) {
                    Text("Annuleren")
                }
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FloatingActionButton(
                    onClick = { isRunning = !isRunning },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Icon(
                        if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isRunning) "Pauzeren" else "Hervatten"
                    )
                }

                FloatingActionButton(
                    onClick = {
                        val formattedDuration = formatTime(elapsedTime)
                        val formattedDistance = UnitConverter.formatDistance(distance, unitSystem)
                        navController.navigate(
                            Screen.WorkoutCompleted.createRoute(
                                duration = formattedDuration,
                                distance = formattedDistance,
                                calories = calories,
                            )
                        )
                    },
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ) {
                    Icon(
                        Icons.Default.Stop,
                        contentDescription = "Stoppen"
                    )
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
                        fontWeight = FontWeight.Bold
                    )

                    if (targetDistance > 0) {
                        Text(
                            text = "Doel: ${UnitConverter.formatDistance(targetDistance, unitSystem)} in ${targetTime} min",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Button(
                    onClick = { showGoalDialog = true }
                ) {
                    Text("Doel instellen")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Main stats in a 2x2 grid
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

            // Progress towards goal
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
                            fontWeight = FontWeight.Medium
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

            // Tabs for different data views
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

            // Content based on selected tab
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
                            // OpenStreetMap with real location tracking
                            if (hasLocationPermission) {
                                OpenStreetMapView(
                                    modifier = Modifier.fillMaxSize(),
                                    currentLocation = currentLocation,
                                    routePoints = routePoints.toList(),
                                    mapStyle = CartoMapStyle.POSITRON
                                )
                            } else {
                                // Show fallback or request permission
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text("Locatie toegang nodig voor kaart")
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = {
                                            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                                        }
                                    ) {
                                        Text("Toegang verlenen")
                                    }

                                    // Show simulated route as fallback
                                    if (simulatedRoutePoints.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text("Gesimuleerde route:")

                                        Canvas(modifier = Modifier.size(200.dp)) {
                                            if (simulatedRoutePoints.size > 1) {
                                                val path = Path()
                                                path.moveTo(simulatedRoutePoints[0].x, simulatedRoutePoints[0].y)

                                                for (i in 1 until simulatedRoutePoints.size) {
                                                    path.lineTo(simulatedRoutePoints[i].x, simulatedRoutePoints[i].y)
                                                }

                                                drawPath(
                                                    path = path,
                                                    color = Color.Blue,
                                                    style = Stroke(width = 5f)
                                                )

                                                // Draw start point
                                                drawCircle(
                                                    color = Color.Green,
                                                    radius = 10f,
                                                    center = simulatedRoutePoints.first()
                                                )

                                                // Draw current position
                                                drawCircle(
                                                    color = Color.Red,
                                                    radius = 10f,
                                                    center = simulatedRoutePoints.last()
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        1 -> {
                            // Observe speedData.size so Compose recomposes when new data is added
                            val dataSize = speedData.size
                            if (dataSize > 0) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val maxSpeed = if (unitSystem == UnitSystem.IMPERIAL) 12f else 20f
                                    val minSpeed = 0f
                                    val width = size.width
                                    val height = size.height
                                    val xStep = width / (dataSize.coerceAtLeast(2) - 1)

                                    // Draw horizontal grid lines
                                    for (s in 0..maxSpeed.toInt() step 2) {
                                        val y = height - (s - minSpeed) / (maxSpeed - minSpeed) * height
                                        drawLine(
                                            color = Color.LightGray,
                                            start = Offset(0f, y),
                                            end = Offset(width, y),
                                            strokeWidth = 1f
                                        )
                                        drawContext.canvas.nativeCanvas.drawText(
                                            "$s ${if (unitSystem == UnitSystem.IMPERIAL) "mi/h" else "km/h"}",
                                            10f,
                                            y - 5,
                                            androidx.compose.ui.graphics.Paint().asFrameworkPaint().apply {
                                                color = android.graphics.Color.GRAY
                                                textSize = 30f
                                            }
                                        )
                                    }

                                    // Draw speed line
                                    if (dataSize > 1) {
                                        val path = Path()
                                        val startX = 0f
                                        val startY = height - (speedData[0].coerceIn(minSpeed, maxSpeed) - minSpeed) / (maxSpeed - minSpeed) * height
                                        path.moveTo(startX, startY)

                                        for (i in 1 until dataSize) {
                                            val x = i * xStep
                                            val y = height - (speedData[i].coerceIn(minSpeed, maxSpeed) - minSpeed) / (maxSpeed - minSpeed) * height
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
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun SecondaryStatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall
            )

            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
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