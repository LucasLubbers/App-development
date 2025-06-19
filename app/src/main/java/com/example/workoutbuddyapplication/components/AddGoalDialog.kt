package com.example.workoutbuddyapplication.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.workoutbuddyapplication.models.GoalType
import com.example.workoutbuddyapplication.models.WorkoutType
import com.example.workoutbuddyapplication.screens.createGoal
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AddGoalDialog(
    onDismiss: () -> Unit,
    onGoalCreated: () -> Unit,
    userId: String
) {
    var title by remember { mutableStateOf("") }
    var target by remember { mutableStateOf("") }
    var selectedWorkoutType by remember { mutableStateOf<WorkoutType?>(null) }
    var selectedGoalType by remember { mutableStateOf<GoalType?>(null) }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var isStartDatePickerOpen by remember { mutableStateOf(false) }
    var isEndDatePickerOpen by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    val availableGoalTypes = when (selectedWorkoutType) {
        null -> emptyList()
        WorkoutType.STRENGTH -> listOf(GoalType.COUNT, GoalType.TIME)
        else -> listOf(GoalType.COUNT, GoalType.DISTANCE, GoalType.TIME)
    }

    val unit = when (selectedGoalType) {
        GoalType.TIME -> "min"
        GoalType.DISTANCE -> "km"
        GoalType.COUNT -> "workouts"
        else -> ""
    }

    val isFormValid = title.isNotBlank()
            && target.toDoubleOrNull() != null
            && selectedWorkoutType != null
            && selectedGoalType != null
            && startDate.isNotBlank()
            && endDate.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nieuw Doel") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Titel") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                ExposedDropdownMenuBoxDisplayName(
                    label = "Workout Type",
                    options = WorkoutType.entries,
                    selected = selectedWorkoutType,
                    displayName = { it.displayName },
                    placeholder = "Selecteer workout type",
                    onSelected = {
                        selectedWorkoutType = it
                        selectedGoalType = null
                    }
                )
                Spacer(Modifier.height(8.dp))
                ExposedDropdownMenuBoxDisplayName(
                    label = "Doel Type",
                    options = availableGoalTypes,
                    selected = selectedGoalType,
                    displayName = { it.displayName },
                    placeholder = if (selectedWorkoutType == null) "Eerst workout type kiezen" else "Selecteer doel type",
                    onSelected = { selectedGoalType = it },
                    enabled = selectedWorkoutType != null
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = target,
                        onValueChange = { target = it },
                        label = { Text("Waarde") },
                        modifier = Modifier.weight(1f),
                        enabled = selectedGoalType != null
                    )
                    OutlinedTextField(
                        value = unit,
                        onValueChange = {},
                        label = { Text("Eenheid") },
                        modifier = Modifier.weight(1f),
                        readOnly = true,
                        enabled = false
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = startDate,
                            onValueChange = {},
                            label = { Text("Startdatum") },
                            readOnly = true,
                            trailingIcon = {
                                Icon(
                                    Icons.Default.CalendarToday,
                                    contentDescription = null
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { isStartDatePickerOpen = true }
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = endDate,
                            onValueChange = {},
                            label = { Text("Einddatum") },
                            readOnly = true,
                            trailingIcon = {
                                Icon(
                                    Icons.Default.CalendarToday,
                                    contentDescription = null
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { isEndDatePickerOpen = true }
                        )
                    }
                }
                if (isStartDatePickerOpen) {
                    MaterialDatePickerDialog(
                        onDateSelected = {
                            startDate = it
                            isStartDatePickerOpen = false
                        },
                        onDismiss = { isStartDatePickerOpen = false }
                    )
                }
                if (isEndDatePickerOpen) {
                    MaterialDatePickerDialog(
                        onDateSelected = {
                            endDate = it
                            isEndDatePickerOpen = false
                        },
                        onDismiss = { isEndDatePickerOpen = false }
                    )
                }
                if (error != null) Text(error!!, color = MaterialTheme.colorScheme.error)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    isLoading = true
                    error = null
                    coroutineScope.launch {
                        val success = createGoal(
                            userId, title, selectedWorkoutType!!, selectedGoalType!!,
                            target.toDouble(), unit, startDate, endDate
                        )
                        isLoading = false
                        if (success) {
                            onGoalCreated()
                            onDismiss()
                        } else {
                            error = "Aanmaken mislukt."
                        }
                    }
                },
                enabled = isFormValid && !isLoading
            ) { Text("Opslaan") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuleren") }
        }
    )
}