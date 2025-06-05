package com.example.workoutbuddyapplication.screens

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
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
import androidx.compose.material.icons.filled.Favorite
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import com.example.workoutbuddyapplication.ui.theme.ThemeManager
import com.example.workoutbuddyapplication.ui.theme.UnitSystem
import com.example.workoutbuddyapplication.utils.UnitConverter
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.collectAsState
import com.example.workoutbuddyapplication.ui.theme.UserPreferencesManager
import com.example.workoutbuddyapplication.ui.theme.toUnitSystem
import com.example.workoutbuddyapplication.utils.LocationManager
import com.example.workoutbuddyapplication.utils.LatLng
import com.example.workoutbuddyapplication.components.OpenStreetMapView
import com.example.workoutbuddyapplication.components.CartoMapStyle
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableStateListOf
import kotlinx.coroutines.launch
import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

@Composable
fun RunningWorkoutScreen(navController: NavController) {
    val context = LocalContext.current
    val preferencesManager = remember { UserPreferencesManager(context) }
    val selectedUnitSystem by preferencesManager.selectedUnitSystem.collectAsState(initial = "metric")
    val unitSystem = selectedUnitSystem.toUnitSystem()
    val debugMode by preferencesManager.debugMode.collectAsState(initial = false)
    val coroutineScope = rememberCoroutineScope()
    
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
    var heartRate by remember { mutableIntStateOf(75) }
    var elapsedTime by remember { mutableLongStateOf(0L) }
    var targetDistance by remember { mutableStateOf(0.0) }
    var targetTime by remember { mutableIntStateOf(0) }
    var showGoalDialog by remember { mutableStateOf(false) }
    var targetDistanceInput by remember { mutableStateOf("") }
    var targetTimeInput by remember { mutableStateOf("") }
    var steps by remember { mutableStateOf(0) }
    var calories by remember { mutableStateOf(0) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var useHeartRateZones by remember { mutableStateOf(false) }
    var targetHeartRateZone by remember { mutableIntStateOf(2) } // Zone 1-5

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

    // Simulated heart rate data
    val heartRateData = remember { mutableListOf<Int>() }

    // Simulated pace data
    val paceData = remember { mutableListOf<Float>() }

    // Accelerometer setup
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val accelerometer = remember { sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) }

    val sensorListener = remember {
        object : SensorEventListener {
            private var lastUpdate = 0L
            private var lastX = 0f
            private var lastY = 0f
            private var lastZ = 0f
            private val SHAKE_THRESHOLD = 600

            override fun onSensorChanged(event: SensorEvent) {
                val currentTime = System.currentTimeMillis()
                if ((currentTime - lastUpdate) > 100) {
                    val diffTime = currentTime - lastUpdate
                    lastUpdate = currentTime

                    val x = event.values[0]
                    val y = event.values[1]
                    val z = event.values[2]

                    val speed = Math.abs(x + y + z - lastX - lastY - lastZ) / diffTime * 10000

                    if (speed > SHAKE_THRESHOLD && isRunning) {
                        // Increment step count based on accelerometer
                        steps += 1

                        // Update calories (simple estimation)
                        calories = (steps * 0.04).toInt()
                    }

                    lastX = x
                    lastY = y
                    lastZ = z
                }
            }

            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
                // Not needed for this demo
            }
        }
    }

    // Register sensor listener
    DisposableEffect(Unit) {
        sensorManager.registerListener(
            sensorListener,
            accelerometer,
            SensorManager.SENSOR_DELAY_NORMAL
        )

        onDispose {
            sensorManager.unregisterListener(sensorListener)
        }
    }

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
                
                // Simulate realistic steps for running (about 160-180 steps per minute)
                val stepsPerSecond = (160 + Math.random() * 20) / 60 // 160-180 steps per minute
                steps += (2 + Math.random() * 2).toInt() // 2-4 steps per second
                
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
            if (distance > 0) {
                pace = elapsedTime / 60000f / distance
            }

            // Simulate heart rate (between 120-150 bpm)
            val newHeartRate = (120 + (Math.random() * 30).toInt())
            heartRate = newHeartRate
            heartRateData.add(newHeartRate)

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
                        steps = 35
                    )
                    Text("${targetTime} minuten")

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Hartslagzones gebruiken",
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = useHeartRateZones,
                            onCheckedChange = { useHeartRateZones = it }
                        )
                    }

                    if (useHeartRateZones) {
                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Doelzone")
                        TabRow(selectedTabIndex = targetHeartRateZone - 1) {
                            for (i in 1..5) {
                                Tab(
                                    selected = targetHeartRateZone == i,
                                    onClick = { targetHeartRateZone = i },
                                    text = { Text("Zone $i") }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = when (targetHeartRateZone) {
                                1 -> "Zone 1: 50-60% van max HR - Herstel"
                                2 -> "Zone 2: 60-70% van max HR - Vetverbranding"
                                3 -> "Zone 3: 70-80% van max HR - Aerobe training"
                                4 -> "Zone 4: 80-90% van max HR - Anaerobe training"
                                5 -> "Zone 5: 90-100% van max HR - Maximale inspanning"
                                else -> ""
                            },
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
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
                    onClick = { navController.navigate(Screen.WorkoutCompleted.route) },
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
                    value = if (pace > 0) {
                        val paceUnit = if (unitSystem == UnitSystem.IMPERIAL) "min/mi" else "min/km"
                        String.format("%.1f %s", pace, paceUnit)
                    } else "--:--",
                    icon = Icons.Default.Speed,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.padding(4.dp))

                StatCard(
                    title = "Hartslag",
                    value = "$heartRate bpm",
                    icon = Icons.Default.Favorite,
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

                        if (useHeartRateZones && heartRate != 75) {
                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Hartslagzone",
                                fontWeight = FontWeight.Medium
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            val currentZone = when (heartRate) {
                                in 0..120 -> 1
                                in 121..140 -> 2
                                in 141..160 -> 3
                                in 161..180 -> 4
                                else -> 5
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                for (i in 1..5) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(24.dp)
                                            .background(
                                                color = when {
                                                    i == currentZone -> MaterialTheme.colorScheme.primary
                                                    i == targetHeartRateZone -> MaterialTheme.colorScheme.primaryContainer
                                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                                }
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "$i",
                                            color = when {
                                                i == currentZone -> MaterialTheme.colorScheme.onPrimary
                                                i == targetHeartRateZone -> MaterialTheme.colorScheme.onPrimaryContainer
                                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                                            },
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = if (currentZone == targetHeartRateZone)
                                    "Je zit in je doelzone!"
                                else if (currentZone < targetHeartRateZone)
                                    "Verhoog je intensiteit om zone $targetHeartRateZone te bereiken"
                                else
                                    "Verlaag je intensiteit om zone $targetHeartRateZone te bereiken",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (currentZone == targetHeartRateZone)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Secondary stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SecondaryStatCard(
                    title = "Stappen",
                    value = steps.toString(),
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.padding(4.dp))

                SecondaryStatCard(
                    title = "Calorieën",
                    value = "$calories kcal",
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.padding(4.dp))

                SecondaryStatCard(
                    title = "Gem. Tempo",
                    value = if (pace > 0) {
                        val paceUnit = if (unitSystem == UnitSystem.IMPERIAL) "min/mi" else "min/km"
                        String.format("%.1f %s", pace, paceUnit)
                    } else "--:--",
                    modifier = Modifier.weight(1f)
                )
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
                    text = { Text("Hartslag") }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Tempo") }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text("Hoogte") }
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
                                    mapStyle = CartoMapStyle.POSITRON // Clean, light style perfect for running
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
                            // Heart rate graph
                            if (heartRateData.isNotEmpty()) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val maxHeartRate = 200f
                                    val minHeartRate = 60f
                                    val width = size.width
                                    val height = size.height
                                    val xStep = width / (heartRateData.size.coerceAtLeast(2) - 1)

                                    // Draw horizontal grid lines
                                    for (hr in 80..180 step 20) {
                                        val y = height - (hr - minHeartRate) / (maxHeartRate - minHeartRate) * height
                                        drawLine(
                                            color = Color.LightGray,
                                            start = Offset(0f, y),
                                            end = Offset(width, y),
                                            strokeWidth = 1f
                                        )

                                        // Draw heart rate labels
                                        drawContext.canvas.nativeCanvas.drawText(
                                            hr.toString(),
                                            10f,
                                            y - 5,
                                            androidx.compose.ui.graphics.Paint().asFrameworkPaint().apply {
                                                color = android.graphics.Color.GRAY
                                                textSize = 30f
                                            }
                                        )
                                    }

                                    // Draw heart rate line
                                    if (heartRateData.size > 1) {
                                        val path = Path()
                                        val startX = 0f
                                        val startY = height - (heartRateData[0] - minHeartRate) / (maxHeartRate - minHeartRate) * height
                                        path.moveTo(startX, startY)

                                        for (i in 1 until heartRateData.size) {
                                            val x = i * xStep
                                            val y = height - (heartRateData[i] - minHeartRate) / (maxHeartRate - minHeartRate) * height
                                            path.lineTo(x, y)
                                        }

                                        drawPath(
                                            path = path,
                                            color = Color.Red,
                                            style = Stroke(width = 3f)
                                        )
                                    }
                                }
                            } else {
                                Text("Nog geen hartslaggegevens beschikbaar")
                            }
                        }
                        2 -> {
                            // Pace graph
                            if (paceData.isNotEmpty()) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val maxPace = 10f // 10 min/km (slower)
                                    val minPace = 3f  // 3 min/km (faster)
                                    val width = size.width
                                    val height = size.height
                                    val xStep = width / (paceData.size.coerceAtLeast(2) - 1)

                                    // Draw horizontal grid lines
                                    for (p in 4..9) {
                                        val y = height - (p - minPace) / (maxPace - minPace) * height
                                        drawLine(
                                            color = Color.LightGray,
                                            start = Offset(0f, y),
                                            end = Offset(width, y),
                                            strokeWidth = 1f
                                        )

                                        // Draw pace labels
                                        drawContext.canvas.nativeCanvas.drawText(
                                            "$p min/km",
                                            10f,
                                            y - 5,
                                            androidx.compose.ui.graphics.Paint().asFrameworkPaint().apply {
                                                color = android.graphics.Color.GRAY
                                                textSize = 30f
                                            }
                                        )
                                    }

                                    // Draw pace line
                                    if (paceData.size > 1) {
                                        val path = Path()
                                        val startX = 0f
                                        val startY = height - (paceData[0].coerceIn(minPace, maxPace) - minPace) / (maxPace - minPace) * height
                                        path.moveTo(startX, startY)

                                        for (i in 1 until paceData.size) {
                                            val x = i * xStep
                                            val y = height - (paceData[i].coerceIn(minPace, maxPace) - minPace) / (maxPace - minPace) * height
                                            path.lineTo(x, y)
                                        }

                                        drawPath(
                                            path = path,
                                            color = Color.Green,
                                            style = Stroke(width = 3f)
                                        )
                                    }
                                }
                            } else {
                                Text("Nog geen tempogegevens beschikbaar")
                            }
                        }
                        3 -> {
                            // Elevation graph
                            if (elevationData.isNotEmpty()) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val maxElevation = 50f
                                    val minElevation = 0f
                                    val width = size.width
                                    val height = size.height
                                    val xStep = width / (elevationData.size.coerceAtLeast(2) - 1)

                                    // Draw horizontal grid lines
                                    for (e in 0..50 step 10) {
                                        val y = height - (e - minElevation) / (maxElevation - minElevation) * height
                                        drawLine(
                                            color = Color.LightGray,
                                            start = Offset(0f, y),
                                            end = Offset(width, y),
                                            strokeWidth = 1f
                                        )

                                        // Draw elevation labels
                                        drawContext.canvas.nativeCanvas.drawText(
                                            "$e m",
                                            10f,
                                            y - 5,
                                            androidx.compose.ui.graphics.Paint().asFrameworkPaint().apply {
                                                color = android.graphics.Color.GRAY
                                                textSize = 30f
                                            }
                                        )
                                    }

                                    // Draw elevation line
                                    if (elevationData.size > 1) {
                                        val path = Path()
                                        val startX = 0f
                                        val startY = height - (elevationData[0] - minElevation) / (maxElevation - minElevation) * height
                                        path.moveTo(startX, startY)

                                        for (i in 1 until elevationData.size) {
                                            val x = i * xStep
                                            val y = height - (elevationData[i] - minElevation) / (maxElevation - minElevation) * height
                                            path.lineTo(x, y)
                                        }

                                        drawPath(
                                            path = path,
                                            color = Color(0xFF8BC34A),
                                            style = Stroke(width = 3f)
                                        )
                                    }
                                }
                            } else {
                                Text("Nog geen hoogtegegevens beschikbaar")
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
