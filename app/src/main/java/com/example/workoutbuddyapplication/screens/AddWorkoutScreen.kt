package com.example.workoutbuddyapplication.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.workoutbuddyapplication.R
import com.example.workoutbuddyapplication.data.SupabaseClient
import com.example.workoutbuddyapplication.navigation.Screen
import com.example.workoutbuddyapplication.models.Workout
import com.example.workoutbuddyapplication.models.WorkoutType
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWorkoutScreen(navController: NavController) {
    var selectedWorkoutType by remember { mutableStateOf(WorkoutType.RUNNING) }
    var customTypeName by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("2023-05-11") }
    var duration by remember { mutableStateOf("30") }
    var distance by remember { mutableStateOf("5.0") }
    var notes by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    var saveError by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.add_workout_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(stringResource(R.string.workout_type), fontWeight = FontWeight.Medium)

            Column(modifier = Modifier.fillMaxWidth()) {
                WorkoutType.values().forEach { workoutType ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .selectable(
                                selected = (workoutType == selectedWorkoutType),
                                onClick = { selectedWorkoutType = workoutType },
                                role = androidx.compose.ui.semantics.Role.RadioButton
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (workoutType == selectedWorkoutType),
                            onClick = null
                        )
                        val workoutTypeName = when (workoutType) {
                            WorkoutType.RUNNING -> stringResource(R.string.running)
                            WorkoutType.STRENGTH -> stringResource(R.string.strength_training)
                            WorkoutType.YOGA -> stringResource(R.string.yoga)
                            else -> workoutType.displayName
                        }
                        Text(
                            text = workoutTypeName,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }

            if (selectedWorkoutType == WorkoutType.OTHER) {
                OutlinedTextField(
                    value = customTypeName,
                    onValueChange = { customTypeName = it },
                    label = { Text(stringResource(R.string.type_name)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            OutlinedTextField(
                value = date,
                onValueChange = { date = it },
                label = { Text(stringResource(R.string.date)) },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    Icon(Icons.Default.DateRange, contentDescription = stringResource(R.string.select_date))
                }
            )

            OutlinedTextField(
                value = duration,
                onValueChange = { duration = it },
                label = { Text(stringResource(R.string.duration_minutes)) },
                modifier = Modifier.fillMaxWidth()
            )

            if (selectedWorkoutType == WorkoutType.RUNNING || selectedWorkoutType == WorkoutType.CYCLING) {
                OutlinedTextField(
                    value = distance,
                    onValueChange = { distance = it },
                    label = { Text(stringResource(R.string.distance_km)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text(stringResource(R.string.notes)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )

            if (saveError != null) {
                Text("${stringResource(R.string.save_error)}: $saveError", color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    isSaving = true
                    saveError = null

                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val user = SupabaseClient.client.auth.currentUserOrNull()
                            if (user == null) {
                                withContext(Dispatchers.Main) {
                                    saveError = "No user logged in"
                                    isSaving = false
                                }
                                return@launch
                            }

                            val workout = Workout(
                                type = if (selectedWorkoutType == WorkoutType.OTHER && customTypeName.isNotBlank())
                                    customTypeName
                                else
                                    selectedWorkoutType.name,
                                date = date,
                                duration = duration.toIntOrNull() ?: 0,
                                distance = distance.toDoubleOrNull(),
                                notes = notes.takeIf { it.isNotBlank() },
                                profileId = user.id
                            )

                            SupabaseClient.client.postgrest.from("workouts")
                                .insert(workout)
                            
                            withContext(Dispatchers.Main) {
                                navController.navigate(Screen.Dashboard.route)
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                saveError = e.message
                                isSaving = false
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSaving
            ) {
                Text(if (isSaving) stringResource(R.string.saving) else stringResource(R.string.save_workout))
            }
        }
    }
}