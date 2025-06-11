package com.example.workoutbuddyapplication.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.workoutbuddyapplication.data.SupabaseClient
import com.example.workoutbuddyapplication.navigation.Screen
import com.example.workoutbuddyapplication.models.Workout
import com.example.workoutbuddyapplication.models.WorkoutType
import com.example.workoutbuddyapplication.ui.theme.strings
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.app.DatePickerDialog
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.platform.LocalContext
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWorkoutScreen(navController: NavController) {
    val strings = strings()
    var selectedWorkoutType by remember { mutableStateOf(WorkoutType.RUNNING) }
    var customTypeName by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(LocalDate.now().toString()) }
    var duration by remember { mutableStateOf("30") }
    var distance by remember { mutableStateOf("5.0") }
    var notes by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    var saveError by remember { mutableStateOf<String?>(null) }
    var isDatePickerOpen by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.addWorkoutTitle) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = strings.back)
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
            Text(strings.workoutType, fontWeight = FontWeight.Medium)

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
                            WorkoutType.RUNNING -> strings.running
                            WorkoutType.STRENGTH -> strings.strengthTraining
                            WorkoutType.YOGA -> strings.yoga
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
                    label = { Text(strings.typeName) },
                    modifier = Modifier.fillMaxWidth()
                    // Removed isError to keep the field style consistent
                )
            }

            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = date,
                    onValueChange = {},
                    label = { Text(strings.date) },
                    modifier = Modifier
                        .fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { isDatePickerOpen = true }) {
                            Icon(Icons.Default.DateRange, contentDescription = strings.selectDate)
                        }
                    },
                    readOnly = true
                )
                if (isDatePickerOpen) {
                    val today = LocalDate.now()
                    LaunchedEffect(Unit) {
                        val initialDate = try {
                            LocalDate.parse(date)
                        } catch (_: Exception) {
                            today
                        }
                        val datePicker = DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                val picked = LocalDate.of(year, month + 1, dayOfMonth)
                                date = picked.toString()
                                isDatePickerOpen = false
                            },
                            initialDate.year,
                            initialDate.monthValue - 1,
                            initialDate.dayOfMonth
                        )
                        datePicker.setOnCancelListener {
                            isDatePickerOpen = false
                        }
                        datePicker.show()
                    }
                }
            }

            OutlinedTextField(
                value = duration,
                onValueChange = { duration = it },
                label = { Text(strings.durationMinutes) },
                modifier = Modifier.fillMaxWidth()
            )

            if (selectedWorkoutType == WorkoutType.RUNNING || selectedWorkoutType == WorkoutType.CYCLING) {
                OutlinedTextField(
                    value = distance,
                    onValueChange = { distance = it },
                    label = { Text(strings.distanceKm) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text(strings.notes) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )

            if (saveError != null) {
                Text("${strings.saveError}: $saveError", color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(16.dp))

            val isOtherType = selectedWorkoutType == WorkoutType.OTHER
            val canSave = !isSaving &&
                (!isOtherType || customTypeName.isNotBlank()) &&
                duration.toIntOrNull() != null &&
                (selectedWorkoutType != WorkoutType.RUNNING && selectedWorkoutType != WorkoutType.CYCLING || distance.toDoubleOrNull() != null)

            Button(
                onClick = {
                    isSaving = true
                    saveError = null

                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val user = SupabaseClient.client.auth.currentUserOrNull()
                            if (user == null) {
                                withContext(Dispatchers.Main) {
                                    saveError = strings.noUserLoggedIn
                                    isSaving = false
                                }
                                return@launch
                            }

                            val workoutTypeString = if (selectedWorkoutType == WorkoutType.OTHER && customTypeName.isNotBlank())
                                customTypeName
                            else
                                selectedWorkoutType.name

                            val workout = Workout(
                                type = workoutTypeString,
                                date = date,
                                duration = duration.toIntOrNull() ?: 0,
                                distance = if (selectedWorkoutType == WorkoutType.RUNNING || selectedWorkoutType == WorkoutType.CYCLING) distance.toDoubleOrNull() else null,
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
                enabled = canSave
            ) {
                Text(if (isSaving) strings.saving else strings.saveWorkout)
            }
        }
    }
}