package com.example.workoutbuddyapplication.screens

import Workout
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.workoutbuddyapplication.navigation.Screen
import java.time.format.DateTimeFormatter
import androidx.compose.material.icons.filled.Timer
import com.example.workoutbuddyapplication.components.BottomNavBar
import androidx.compose.ui.platform.LocalContext

@Composable
fun SummaryCard(workouts: List<Workout> = emptyList()) {
    val totalWorkouts = workouts.size
    val totalDistance = workouts.mapNotNull { it.distance }.sum()
    val totalDuration = workouts.sumOf { it.duration }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        StatCard(
            title = "Workouts",
            value = "$totalWorkouts",
            icon = Icons.Default.FitnessCenter,
            modifier = Modifier.weight(1f).padding(end = 4.dp)
        )
        StatCard(
            title = "Afstand",
            value = "${"%.1f".format(totalDistance)} km",
            icon = Icons.AutoMirrored.Filled.DirectionsRun,
            modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
        )
        StatCard(
            title = "Tijd",
            value = "$totalDuration min",
            icon = Icons.Default.Timer,
            modifier = Modifier.weight(1f).padding(start = 4.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DashboardScreen(navController: NavController) {
    val context = LocalContext.current
    var workouts by remember { mutableStateOf<List<Workout>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var selectedTabIndex by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        isLoading = true
        error = null
        val userId = getUserId(context)
        if (userId != null) {
            val result = fetchWorkouts(userId)
            if (result.isNotEmpty()) {
                workouts = result
            } else {
                error = "No workouts found or failed to fetch."
            }
        } else {
            error = "User not logged in."
        }
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Aktiv") },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.Login.route) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Uitloggen"
                        )
                    }
                }
            )
        },
        bottomBar = {
            BottomNavBar(
                selectedTabIndex = selectedTabIndex,
                onTabSelected = { selectedTabIndex = it },
                navController = navController
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navController.navigate(Screen.StartWorkout.route) },
                icon = { Icon(Icons.Default.PlayArrow, contentDescription = "Workout starten") },
                text = { Text("Workout starten") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = "Welkom bij Aktiv",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            SummaryCard(workouts)

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recente Workouts",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium
                )

                TextButton(
                    onClick = { navController.navigate(Screen.AddWorkout.route) }
                ) {
                    Text("Handmatig toevoegen")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            when {
                isLoading -> {
                    CircularProgressIndicator()
                }
                error != null -> {
                    Text(error!!, color = MaterialTheme.colorScheme.error)
                }
                workouts.isEmpty() -> {
                    Text("No workouts found.")
                }
                else -> {
                    LazyColumn {
                        items(workouts.sortedByDescending { it.date }.take(5)) { workout ->
                            WorkoutItem(
                                workout = workout,
                                onClick = { navController.navigate("workoutDetail/${workout.id}/0") }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WorkoutItem(workout: Workout, onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = workout.type.icon,
                contentDescription = workout.type.displayName,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = workout.type.displayName,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                )
                Text(
                    text = workout.date.format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "${workout.duration} minuten" +
                            (workout.distance?.let { " | ${it} km" } ?: ""),
                    style = MaterialTheme.typography.bodySmall
                )
                if (!workout.notes.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = workout.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}