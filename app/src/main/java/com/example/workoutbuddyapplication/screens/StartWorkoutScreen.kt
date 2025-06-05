package com.example.workoutbuddyapplication.screens

import com.example.workoutbuddyapplication.models.WorkoutType
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.workoutbuddyapplication.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartWorkoutScreen(navController: NavController) {
    var selectedWorkoutType by remember { mutableStateOf(WorkoutType.RUNNING) }
    var useBluetooth by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Workout Starten") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Terug")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Kies je workout type",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                WorkoutTypeCard(
                    type = WorkoutType.RUNNING,
                    icon = Icons.Default.DirectionsRun,
                    isSelected = selectedWorkoutType == WorkoutType.RUNNING,
                    onClick = { selectedWorkoutType = WorkoutType.RUNNING }
                )

                WorkoutTypeCard(
                    type = WorkoutType.STRENGTH,
                    icon = Icons.Default.FitnessCenter,
                    isSelected = selectedWorkoutType == WorkoutType.STRENGTH,
                    onClick = { selectedWorkoutType = WorkoutType.STRENGTH }
                )

                WorkoutTypeCard(
                    type = WorkoutType.YOGA,
                    icon = Icons.Default.SelfImprovement,
                    isSelected = selectedWorkoutType == WorkoutType.YOGA,
                    onClick = { selectedWorkoutType = WorkoutType.YOGA }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Only show tracking options for running and cycling
            if (selectedWorkoutType == WorkoutType.RUNNING || selectedWorkoutType == WorkoutType.CYCLING) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Tracking Opties",
                            fontWeight = FontWeight.Medium,
                            fontSize = 18.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Bluetooth-apparaten",
                                    fontWeight = FontWeight.Medium
                                )

                                Text(
                                    text = "Verbind met smartwatch of sporthorloge",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                            Switch(
                                checked = useBluetooth,
                                onCheckedChange = { useBluetooth = it }
                            )
                        }

                        if (useBluetooth) {
                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = { navController.navigate(Screen.BluetoothDevice.route) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    Icons.Default.Bluetooth,
                                    contentDescription = "Bluetooth"
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Apparaat koppelen")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }

            Button(
                onClick = {
                    when (selectedWorkoutType) {
                        WorkoutType.RUNNING -> navController.navigate(Screen.RunningWorkout.route)
                        WorkoutType.STRENGTH -> navController.navigate(Screen.StrengthWorkout.route)
                        WorkoutType.YOGA -> navController.navigate(Screen.YogaWorkout.route)
                        else -> navController.navigate(Screen.RunningWorkout.route)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Start ${selectedWorkoutType.displayName}")
            }
        }
    }
}

@Composable
fun WorkoutTypeCard(
    type: WorkoutType,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .size(100.dp)
            .padding(4.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 2.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = type.displayName,
                modifier = Modifier.size(40.dp),
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = type.displayName,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
fun TrackingOption(
    title: String,
    description: String,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Switch(
            checked = isEnabled,
            onCheckedChange = onToggle
        )
    }
}
