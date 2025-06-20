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
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.FitnessCenter
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
import com.example.workoutbuddyapplication.ui.theme.strings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartWorkoutScreen(navController: NavController) {
    val strings = strings()
    var selectedWorkoutType by remember { mutableStateOf(WorkoutType.RUNNING) }
    var useBluetooth by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(strings.startWorkoutTitle) }, navigationIcon = {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = strings.back)
                }
            })
        }) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = strings.chooseWorkoutType, fontSize = 20.sp, fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                WorkoutTypeCard(
                    type = WorkoutType.RUNNING,
                    icon = Icons.AutoMirrored.Filled.DirectionsRun,
                    isSelected = selectedWorkoutType == WorkoutType.RUNNING,
                    onClick = { selectedWorkoutType = WorkoutType.RUNNING })

                WorkoutTypeCard(
                    type = WorkoutType.CYCLING,
                    icon = Icons.AutoMirrored.Filled.DirectionsBike,
                    isSelected = selectedWorkoutType == WorkoutType.CYCLING,
                    onClick = { selectedWorkoutType = WorkoutType.CYCLING })

                WorkoutTypeCard(
                    type = WorkoutType.STRENGTH,
                    icon = Icons.Default.FitnessCenter,
                    isSelected = selectedWorkoutType == WorkoutType.STRENGTH,
                    onClick = { selectedWorkoutType = WorkoutType.STRENGTH })
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
                            text = strings.trackingOptions,
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
                                    text = strings.bluetoothDevices, fontWeight = FontWeight.Medium
                                )

                                Text(
                                    text = strings.connectSmartwatch,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                            Switch(
                                checked = useBluetooth, onCheckedChange = { useBluetooth = it })
                        }

                        if (useBluetooth) {
                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = { navController.navigate(Screen.BluetoothDevice.route) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    Icons.Default.Bluetooth, contentDescription = "Bluetooth"
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(strings.pairDevice)
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
                        WorkoutType.CYCLING -> navController.navigate(Screen.CyclingWorkout.route)
                        else -> navController.navigate(Screen.RunningWorkout.route)
                    }
                }, modifier = Modifier.fillMaxWidth()
            ) {
                val workoutTypeName = when (selectedWorkoutType) {
                    WorkoutType.RUNNING -> strings.running
                    WorkoutType.STRENGTH -> strings.strengthTraining
                    WorkoutType.CYCLING -> strings.cycling
                    else -> strings.running
                }
                Text("${strings.startWorkout.split(" ")[0]} $workoutTypeName")
            }
        }
    }
}

@Composable
fun WorkoutTypeCard(
    type: WorkoutType, icon: ImageVector, isSelected: Boolean, onClick: () -> Unit
) {
    val strings = strings()

    Card(
        modifier = Modifier
            .size(100.dp)
            .padding(4.dp), elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 2.dp
        ), colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ), onClick = onClick
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

            val workoutTypeName = when (type) {
                WorkoutType.RUNNING -> strings.running
                WorkoutType.STRENGTH -> strings.strengthTraining
                WorkoutType.CYCLING -> strings.cycling
                else -> type.displayName
            }

            Text(
                text = workoutTypeName,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
fun TrackingOption(
    title: String, description: String, isEnabled: Boolean, onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title, fontWeight = FontWeight.Medium
            )

            Text(
                text = description, style = MaterialTheme.typography.bodySmall
            )
        }

        Switch(
            checked = isEnabled, onCheckedChange = onToggle
        )
    }
}
