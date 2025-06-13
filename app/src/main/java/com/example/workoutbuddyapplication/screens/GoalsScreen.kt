package com.example.workoutbuddyapplication.screens

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.workoutbuddyapplication.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import androidx.compose.ui.platform.LocalContext
import com.example.workoutbuddyapplication.models.Goal
import com.example.workoutbuddyapplication.models.GoalType
import com.example.workoutbuddyapplication.models.WorkoutType
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.example.workoutbuddyapplication.models.Workout
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.datastore.preferences.core.stringSetPreferencesKey
import okhttp3.RequestBody.Companion.toRequestBody
import com.example.workoutbuddyapplication.services.NotificationService
import java.time.temporal.ChronoUnit
import java.time.LocalDateTime
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.first
import androidx.compose.ui.res.stringResource
import com.example.workoutbuddyapplication.R
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

suspend fun updateGoal(
    goalId: Int,
    title: String,
    workoutType: WorkoutType,
    goalType: GoalType,
    targetValue: Double,
    unit: String,
    startDate: String,
    endDate: String
): Boolean = withContext(Dispatchers.IO) {
    val client = OkHttpClient()
    val json = JSONObject()
    json.put("title", title)
    json.put("workout_type", workoutType.name)
    json.put("goal_type", goalType.name)
    json.put("target_value", targetValue)
    json.put("unit", unit)
    json.put("start_date", startDate)
    json.put("end_date", endDate)
    
    val body = json.toString().toRequestBody("application/json".toMediaTypeOrNull())
    val request = Request.Builder()
        .url("https://attsgwsxdlblbqxnboqx.supabase.co/rest/v1/goals?id=eq.$goalId")
        .patch(body)
        .addHeader("apikey", BuildConfig.SUPABASE_ANON_KEY)
        .addHeader("Authorization", "Bearer ${BuildConfig.SUPABASE_ANON_KEY}")
        .addHeader("Content-Type", "application/json")
        .build()
    
    try {
        val response = client.newCall(request).execute()
        response.isSuccessful
    } catch (e: Exception) {
        false
    }
}

suspend fun addGoal(
    userId: String,
    title: String,
    workoutType: WorkoutType,
    goalType: GoalType,
    targetValue: Double,
    unit: String,
    startDate: String,
    endDate: String
): Boolean = withContext(Dispatchers.IO) {
    val client = OkHttpClient()
    val json = JSONObject()
    json.put("profile_id", userId)
    json.put("title", title)
    json.put("workout_type", workoutType.name)
    json.put("goal_type", goalType.name)
    json.put("target_value", targetValue)
    json.put("unit", unit)
    json.put("start_date", startDate)
    json.put("end_date", endDate)
    
    val body = json.toString().toRequestBody("application/json".toMediaTypeOrNull())
    val request = Request.Builder()
        .url("https://attsgwsxdlblbqxnboqx.supabase.co/rest/v1/goals")
        .post(body)
        .addHeader("apikey", BuildConfig.SUPABASE_ANON_KEY)
        .addHeader("Authorization", "Bearer ${BuildConfig.SUPABASE_ANON_KEY}")
        .addHeader("Content-Type", "application/json")
        .build()
    
    try {
        val response = client.newCall(request).execute()
        response.isSuccessful
    } catch (e: Exception) {
        false
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun calculateGoalProgress(goal: Goal, workouts: List<Workout>): Double {
    val formatter = DateTimeFormatter.ISO_DATE
    val start = goal.startDate?.let { LocalDate.parse(it, formatter) }
    val end = goal.endDate?.let { LocalDate.parse(it, formatter) }

    val relevantWorkouts = workouts.filter { workout ->
        workout.workoutTypeEnum == goal.workoutType &&
                (start == null || !LocalDate.parse(workout.date, formatter).isBefore(start)) &&
                (end == null || !LocalDate.parse(workout.date, formatter).isAfter(end))
    }

    return when (goal.goalType) {
        GoalType.COUNT -> relevantWorkouts.size.toDouble()
        GoalType.DISTANCE -> relevantWorkouts.sumOf { it.distance ?: 0.0 }
        GoalType.TIME -> relevantWorkouts.sumOf { it.duration.toDouble() }
        else -> 0.0
    }
}

suspend fun fetchGoals(userId: String): List<Goal> = withContext(Dispatchers.IO) {
    val client = OkHttpClient()
    val request = Request.Builder()
        .url("https://attsgwsxdlblbqxnboqx.supabase.co/rest/v1/goals?profile_id=eq.$userId&select=*")
        .addHeader("apikey", BuildConfig.SUPABASE_ANON_KEY)
        .addHeader("Authorization", "Bearer ${BuildConfig.SUPABASE_ANON_KEY}")
        .build()
    try {
        val response = client.newCall(request).execute()
        val responseBody = response.body?.string()
        if (!response.isSuccessful || responseBody == null) return@withContext emptyList()
        val jsonArray = JSONArray(responseBody)
        val goals = mutableListOf<Goal>()
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            goals.add(
                Goal(
                    id = obj.getInt("id"),
                    profileId = obj.getString("profile_id"),
                    title = obj.getString("title"),
                    workoutType = WorkoutType.fromString(obj.getString("workout_type")),
                    goalType = GoalType.fromString(obj.getString("goal_type")),
                    target = obj.getDouble("target_value"),
                    unit = obj.getString("unit"),
                    startDate = obj.optString("start_date"),
                    endDate = obj.optString("end_date"),
                    createdAt = obj.optString("created_at"),
                    description = obj.optString("description", ""),
                    current = 0.0
                )
            )
        }
        goals
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }
}

suspend fun deleteGoal(goalId: Int): Boolean = withContext(Dispatchers.IO) {
    val client = OkHttpClient()
    val request = Request.Builder()
        .url("https://attsgwsxdlblbqxnboqx.supabase.co/rest/v1/goals?id=eq.$goalId")
        .addHeader("apikey", BuildConfig.SUPABASE_ANON_KEY)
        .addHeader("Authorization", "Bearer ${BuildConfig.SUPABASE_ANON_KEY}")
        .delete()
        .build()
    try {
        val response = client.newCall(request).execute()
        response.isSuccessful
    } catch (e: Exception) {
        false
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EditGoalDialog(
    goal: Goal,
    onDismiss: () -> Unit,
    onGoalUpdated: () -> Unit,
    userId: String
) {
    var title by remember { mutableStateOf(goal.title) }
    var target by remember { mutableStateOf(goal.target.toString()) }
    var selectedWorkoutType by remember { mutableStateOf(goal.workoutType) }
    var selectedGoalType by remember { mutableStateOf<GoalType?>(goal.goalType) }
    var startDate by remember { mutableStateOf(goal.startDate ?: "") }
    var endDate by remember { mutableStateOf(goal.endDate ?: "") }
    var isStartDatePickerOpen by remember { mutableStateOf(false) }
    var isEndDatePickerOpen by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // Filter goal types based on workout type
    val availableGoalTypes = when (selectedWorkoutType) {
        null -> emptyList()
        WorkoutType.STRENGTH, WorkoutType.YOGA -> listOf(GoalType.COUNT, GoalType.TIME)
        else -> listOf(GoalType.COUNT, GoalType.DISTANCE, GoalType.TIME, GoalType.CALORIES)
    }

    // Auto-fill unit based on goal type
    val unit = when (selectedGoalType) {
        GoalType.TIME -> "min"
        GoalType.DISTANCE -> "km"
        GoalType.COUNT -> "workouts"
        GoalType.CALORIES -> "kcal"
        else -> ""
    }

    val isFormValid = title.isNotBlank() && target.toDoubleOrNull() != null &&
            selectedWorkoutType != null && selectedGoalType != null &&
            startDate.isNotBlank() && endDate.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.edit_goal)) },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.title)) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                // Workout type selection
                ExposedDropdownMenuBoxDisplayName(
                    label = stringResource(R.string.workout_type),
                    options = WorkoutType.entries,
                    selected = selectedWorkoutType,
                    displayName = { it.displayName },
                    placeholder = stringResource(R.string.select_workout_type),
                    onSelected = {
                        selectedWorkoutType = it
                        selectedGoalType = null
                    }
                )
                Spacer(Modifier.height(8.dp))
                // Goal type selection, disabled if no workout type
                ExposedDropdownMenuBoxDisplayName(
                    label = stringResource(R.string.goal_type),
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
                        label = { Text(stringResource(R.string.value)) },
                        modifier = Modifier.weight(1f),
                        enabled = selectedGoalType != null
                    )
                    OutlinedTextField(
                        value = unit,
                        onValueChange = {},
                        label = { Text(stringResource(R.string.unit)) },
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
                            label = { Text(stringResource(R.string.start_date)) },
                            readOnly = true,
                            trailingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
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
                            label = { Text(stringResource(R.string.end_date)) },
                            readOnly = true,
                            trailingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
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
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }
                error?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
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
                            onGoalUpdated()
                            onDismiss()
                        } else {
                            error = "Bijwerken mislukt."
                        }
                    }
                },
                enabled = isFormValid && !isLoading
            ) { Text(stringResource(R.string.save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}

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

    // Filter goal types based on workout type
    val availableGoalTypes = when (selectedWorkoutType) {
        null -> emptyList()
        WorkoutType.STRENGTH, WorkoutType.YOGA -> listOf(GoalType.COUNT, GoalType.TIME)
        else -> listOf(GoalType.COUNT, GoalType.DISTANCE, GoalType.TIME, GoalType.CALORIES)
    }

    // Auto-fill unit based on goal type
    val unit = when (selectedGoalType) {
        GoalType.TIME -> "min"
        GoalType.DISTANCE -> "km"
        GoalType.COUNT -> "workouts"
        GoalType.CALORIES -> "kcal"
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
        title = { Text(stringResource(R.string.new_goal)) },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.title)) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                // Workout type selection
                ExposedDropdownMenuBoxDisplayName(
                    label = stringResource(R.string.workout_type),
                    options = WorkoutType.entries,
                    selected = selectedWorkoutType,
                    displayName = { it.displayName },
                    placeholder = stringResource(R.string.select_workout_type),
                    onSelected = {
                        selectedWorkoutType = it
                        selectedGoalType = null // Reset goal type when workout type changes
                    }
                )
                Spacer(Modifier.height(8.dp))
                // Goal type selection, disabled if no workout type
                ExposedDropdownMenuBoxDisplayName(
                    label = stringResource(R.string.goal_type),
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
                        label = { Text(stringResource(R.string.value)) },
                        modifier = Modifier.weight(1f),
                        enabled = selectedGoalType != null
                    )
                    OutlinedTextField(
                        value = unit,
                        onValueChange = {},
                        label = { Text(stringResource(R.string.unit)) },
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
                            label = { Text(stringResource(R.string.start_date)) },
                            readOnly = true,
                            trailingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
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
                            label = { Text(stringResource(R.string.end_date)) },
                            readOnly = true,
                            trailingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
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
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }
                error?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    isLoading = true
                    coroutineScope.launch {
                        val success = addGoal(
                            userId = userId,
                            title = title,
                            workoutType = selectedWorkoutType!!,
                            goalType = selectedGoalType!!,
                            targetValue = target.toDouble(),
                            unit = unit,
                            startDate = startDate,
                            endDate = endDate
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
            ) { Text(stringResource(R.string.add)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}

@SuppressLint("NewApi")
@Composable
fun MaterialDatePickerDialog(
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val today = LocalDate.now()
    val dialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
            onDateSelected(selectedDate.format(DateTimeFormatter.ISO_DATE))
        },
        today.year,
        today.monthValue - 1,
        today.dayOfMonth
    )
    dialog.setOnDismissListener { onDismiss() }
    dialog.show()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> ExposedDropdownMenuBox(
    label: String,
    options: List<T>,
    selected: T?,
    displayName: (T) -> String,
    placeholder: String,
    onSelected: (T) -> Unit,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selected.toString(),
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            enabled = enabled
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item.toString()) },
                    onClick = {
                        onSelected(item)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> ExposedDropdownMenuBoxDisplayName(
    label: String,
    options: List<T>,
    selected: T?,
    displayName: (T) -> String,
    placeholder: String,
    onSelected: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selected?.let(displayName) ?: placeholder,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { item ->
                DropdownMenuItem(
                    text = { Text(displayName(item)) },
                    onClick = {
                        onSelected(item)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun GoalCard(
    goal: Goal,
    onEdit: (Goal) -> Unit,
    onDelete: (Goal) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val progress = (goal.current / goal.target).toFloat().coerceIn(0f, 1f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                goal.title,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                "Doel: ${goal.target} ${goal.unit}",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = goal.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${goal.workoutType.displayName} - ${goal.goalType.displayName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = { onEdit(goal) }) {
                    Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit)))
                }
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete)))
                }
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Collapse" else "Expand"
                    )
                }
            }
            Text(
                if (isComplete) "Voltooid" else "Voortgang: ${goal.current} ${goal.unit}",
                color = if (isComplete) Color(0xFF388E3C) else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (isComplete) FontWeight.Bold else FontWeight.Normal
            )
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                strokeCap = StrokeCap.Round
            )
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Workout type: ${goal.workoutType.displayName}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Type doel: ${goal.goalType.displayName}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (goal.description.isNotBlank()) {
                    Text(
                        "Beschrijving: ${goal.description}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (!goal.startDate.isNullOrBlank()) {
                    Text(
                        "Startdatum: ${goal.startDate}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                goal.endDate?.let {
                    Text(
                        "Einddatum: ${goal.endDate}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    "Aangemaakt op: ${goal.createdAt}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Doel verwijderen") },
            text = { Text("Weet je zeker dat je dit doel wilt verwijderen?") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    onDelete(goal)
                }) { Text("Verwijderen") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Annuleren") }
            }
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(navController: NavController) {
    val context = LocalContext.current
    var goals by remember { mutableStateOf<List<Goal>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var showAddGoalDialog by remember { mutableStateOf(false) }
    var refreshTrigger by remember { mutableIntStateOf(0) }
    var goalToEdit by remember { mutableStateOf<Goal?>(null) }
    var goalToDelete by remember { mutableStateOf<Goal?>(null) }

    var userId by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        userId = getUserId(context)
    }

    LaunchedEffect(userId, refreshTrigger) {
        if (userId != null) {
            isLoading = true
            error = null
            val fetchedGoals = fetchGoals(userId!!)
            val fetchedWorkouts = fetchWorkouts(userId!!)
            goals = fetchedGoals.map { goal ->
                goal.copy(current = calculateGoalProgress(goal, fetchedWorkouts))
            }
            isLoading = false

            if (NotificationService.areGoalReminderNotificationsEnabled(context)) {
                val formatter = DateTimeFormatter.ISO_DATE
                val today = LocalDate.now()
                for (goal in goals) {
                    val endDateStr = goal.endDate
                    val progress = (goal.current / goal.target).coerceIn(0.0, 1.0)
                    if (!endDateStr.isNullOrBlank() && progress < 1.0) {
                        try {
                            val endDate = LocalDate.parse(endDateStr, formatter)
                            if (endDate.isEqual(today)) {
                                NotificationService.createNotificationChannel(context)
                                NotificationService.sendGoalDeadlineNotification(context, goal.id ?: 0, goal.title)
                            }
                        } catch (_: Exception) {}
                    }
                }
            }
        }
    }

    val (completedGoals, activeGoals) = remember(goals) {
        goals.partition {
            val progress = (it.current / it.target).coerceIn(0.0, 1.0)
            progress >= 1.0
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mijn Doelen") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Terug")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddGoalDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Doel toevoegen")
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
            Text(
                text = "Actieve Doelen",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(16.dp))

            when {
                isLoading -> CircularProgressIndicator()
                error != null -> Text(error!!, color = MaterialTheme.colorScheme.error)
                activeGoals.isEmpty() -> Text("Geen actieve doelen gevonden.")
                else -> Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    activeGoals.forEach { goal ->
                        GoalCard(
                            goal = goal,
                            onEdit = { goalToEdit = it },
                            onDelete = { goalToDelete = it }
                        )
                    }
                }
            }

            if (completedGoals.isNotEmpty()) {
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = "Voltooide Doelen",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(16.dp))
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    completedGoals.forEach { goal ->
                        GoalCard(
                            goal = goal,
                            onEdit = { goalToEdit = it },
                            onDelete = { goalToDelete = it }
                        )
                    }
                }
            }
        }

        if (showAddGoalDialog && userId != null) {
            AddGoalDialog(
                onDismiss = { showAddGoalDialog = false },
                onGoalCreated = { refreshTrigger++ },
                userId = userId!!
            )
        }
    }
    if (goalToEdit != null && userId != null) {
        EditGoalDialog(
            goal = goalToEdit!!,
            onDismiss = { goalToEdit = null },
            onGoalUpdated = { refreshTrigger++; goalToEdit = null },
            userId = userId!!
        )
    }

    if (goalToDelete != null) {
        LaunchedEffect(goalToDelete) {
            val success = deleteGoal(goalToDelete!!.id!!)
            if (success) refreshTrigger++
            goalToDelete = null
        }
    }
}