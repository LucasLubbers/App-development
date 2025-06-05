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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
import com.example.workoutbuddyapplication.ui.theme.strings
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import okhttp3.RequestBody.Companion.toRequestBody
import com.example.workoutbuddyapplication.screens.fetchWorkouts

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
    val strings = strings()
    var title by remember { mutableStateOf(goal.title) }
    var target by remember { mutableStateOf(goal.target.toString()) }
    var unit by remember { mutableStateOf(goal.unit) }
    var selectedWorkoutType by remember { mutableStateOf(goal.workoutType) }
    var selectedGoalType by remember { mutableStateOf(goal.goalType) }
    var startDate by remember { mutableStateOf(goal.startDate ?: "") }
    var endDate by remember { mutableStateOf(goal.endDate ?: "") }
    var isStartDatePickerOpen by remember { mutableStateOf(false) }
    var isEndDatePickerOpen by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    val isFormValid = title.isNotBlank() && target.toDoubleOrNull() != null && unit.isNotBlank() &&
            startDate.isNotBlank() && endDate.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.editGoal) },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(strings.title) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                ExposedDropdownMenuBoxDisplayName(
                    label = strings.workoutType,
                    options = WorkoutType.values().toList(),
                    selected = selectedWorkoutType,
                    displayName = { it.displayName },
                    placeholder = strings.selectWorkoutType,
                    onSelected = { selectedWorkoutType = it }
                )
                Spacer(Modifier.height(8.dp))
                ExposedDropdownMenuBoxDisplayName(
                    label = strings.goalType,
                    options = GoalType.values().toList(),
                    selected = selectedGoalType,
                    displayName = { it.displayName },
                    placeholder = strings.selectGoalType,
                    onSelected = { selectedGoalType = it }
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = target,
                        onValueChange = { target = it },
                        label = { Text(strings.value) },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text(strings.unit) },
                        modifier = Modifier.weight(1f)
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
                            label = { Text(strings.startDate) },
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
                            label = { Text(strings.endDate) },
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
                if (error != null) Text(error!!, color = MaterialTheme.colorScheme.error)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    isLoading = true
                    error = null
                    coroutineScope.launch {
                        val success = updateGoal(
                            goal.id!!, title, selectedWorkoutType, selectedGoalType,
                            target.toDouble(), unit, startDate, endDate
                        )
                        isLoading = false
                        if (success) {
                            onGoalUpdated()
                            onDismiss()
                        } else {
                            error = strings.updateFailed
                        }
                    }
                },
                enabled = isFormValid && !isLoading
            ) { Text(strings.update) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(strings.cancel) }
        }
    )
}

suspend fun updateGoal(
    goalId: Int,
    title: String,
    workoutType: WorkoutType,
    goalType: GoalType,
    target: Double,
    unit: String,
    startDate: String?,
    endDate: String?
): Boolean = withContext(Dispatchers.IO) {
    val client = OkHttpClient()
    val json = buildString {
        append("{")
        append("\"title\":\"$title\",")
        append("\"workout_type\":\"${workoutType.name}\",")
        append("\"goal_type\":\"${goalType.name}\",")
        append("\"target_value\":$target,")
        append("\"unit\":\"$unit\"")
        if (!startDate.isNullOrBlank()) append(",\"start_date\":\"$startDate\"")
        if (!endDate.isNullOrBlank()) append(",\"end_date\":\"$endDate\"")
        append("}")
    }
    val body = json
        .toRequestBody("application/json".toMediaTypeOrNull())
    val request = Request.Builder()
        .url("https://attsgwsxdlblbqxnboqx.supabase.co/rest/v1/goals?id=eq.$goalId")
        .addHeader("apikey", BuildConfig.SUPABASE_ANON_KEY)
        .addHeader("Authorization", "Bearer ${BuildConfig.SUPABASE_ANON_KEY}")
        .addHeader("Content-Type", "application/json")
        .patch(body)
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
fun AddGoalDialog(
    onDismiss: () -> Unit,
    onGoalAdded: () -> Unit,
    userId: String
) {
    val strings = strings()
    var title by remember { mutableStateOf("") }
    var target by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }
    var selectedWorkoutType by remember { mutableStateOf<WorkoutType?>(null) }
    var selectedGoalType by remember { mutableStateOf<GoalType?>(null) }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var isStartDatePickerOpen by remember { mutableStateOf(false) }
    var isEndDatePickerOpen by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // Validation: all fields must be filled and target must be a valid number
    val isFormValid = title.isNotBlank()
            && target.toDoubleOrNull() != null
            && unit.isNotBlank()
            && selectedWorkoutType != null
            && selectedGoalType != null
            && startDate.isNotBlank()
            && endDate.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.newGoal) },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(strings.title) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                ExposedDropdownMenuBoxDisplayName(
                    label = strings.workoutType,
                    options = WorkoutType.values().toList(),
                    selected = selectedWorkoutType,
                    displayName = { it.displayName },
                    placeholder = strings.selectWorkoutType,
                    onSelected = { selectedWorkoutType = it }
                )
                Spacer(Modifier.height(8.dp))
                ExposedDropdownMenuBoxDisplayName(
                    label = strings.goalType,
                    options = GoalType.values().toList(),
                    selected = selectedGoalType,
                    displayName = { it.displayName },
                    placeholder = strings.selectGoalType,
                    onSelected = { selectedGoalType = it }
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = target,
                        onValueChange = { target = it },
                        label = { Text(strings.value) },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text(strings.unit) },
                        modifier = Modifier.weight(1f)
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
                            label = { Text(strings.startDate) },
                            readOnly = true,
                            trailingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                            modifier = Modifier
                                .fillMaxWidth()
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
                            label = { Text(strings.endDate) },
                            readOnly = true,
                            trailingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                            modifier = Modifier
                                .fillMaxWidth()
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
                            onGoalAdded()
                            onDismiss()
                        } else {
                            error = strings.createFailed
                        }
                    }
                },
                enabled = isFormValid && !isLoading
            ) { Text(strings.update) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(strings.cancel) }
        }
    )
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
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selected?.let(displayName) ?: "",
            onValueChange = {},
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(displayName(option)) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MaterialDatePickerDialog(
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val today = LocalDate.now()
    var openDialog by remember { mutableStateOf(true) }

    if (openDialog) {
        LaunchedEffect(Unit) {
            val datePicker = DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    openDialog = false
                    val picked = LocalDate.of(year, month + 1, dayOfMonth)
                    onDateSelected(picked.toString())
                },
                today.year,
                today.monthValue - 1,
                today.dayOfMonth
            )
            datePicker.setOnCancelListener {
                openDialog = false
                onDismiss()
            }
            datePicker.show()
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
suspend fun createGoal(
    userId: String,
    title: String,
    workoutType: WorkoutType,
    goalType: GoalType,
    target: Double,
    unit: String,
    startDate: String?,
    endDate: String?
): Boolean = withContext(Dispatchers.IO) {
    val client = OkHttpClient()
    val now = LocalDate.now().toString()
    val json = buildString {
        append("{")
        append("\"profile_id\":\"$userId\",")
        append("\"title\":\"$title\",")
        append("\"workout_type\":\"${workoutType.name}\",")
        append("\"goal_type\":\"${goalType.name}\",")
        append("\"target_value\":$target,")
        append("\"unit\":\"$unit\",")
        append("\"created_at\":\"$now\"")
        if (!startDate.isNullOrBlank()) append(",\"start_date\":\"$startDate\"")
        if (!endDate.isNullOrBlank()) append(",\"end_date\":\"$endDate\"")
        append("}")
    }
    val body = json
        .toRequestBody("application/json".toMediaTypeOrNull())
    val request = Request.Builder()
        .url("https://attsgwsxdlblbqxnboqx.supabase.co/rest/v1/goals")
        .addHeader("apikey", BuildConfig.SUPABASE_ANON_KEY)
        .addHeader("Authorization", "Bearer ${BuildConfig.SUPABASE_ANON_KEY}")
        .addHeader("Content-Type", "application/json")
        .post(body)
        .build()
    try {
        val response = client.newCall(request).execute()
        response.isSuccessful
    } catch (e: Exception) {
        false
    }
}

@Composable
fun GoalCard(
    goal: Goal,
    onEdit: (Goal) -> Unit,
    onDelete: (Goal) -> Unit
) {
    val strings = strings()
    var expanded by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val progress = (goal.current / goal.target).coerceIn(0.0, 1.0).toFloat()
    val isComplete = progress >= 1.0f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                goal.title,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                "${strings.goalType}: ${goal.target} ${goal.unit}",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = { onEdit(goal) }) {
                    Icon(Icons.Default.Edit, contentDescription = strings.edit)
                }
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(Icons.Default.Delete, contentDescription = strings.delete)
                }
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Less info" else "More info"
                    )
                }
            }
            Text(
                if (isComplete) strings.completedGoal else "${strings.of}: ${goal.current} ${goal.unit}",
                color = if (isComplete) Color(0xFF388E3C) else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (isComplete) FontWeight.Bold else FontWeight.Normal
            )
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .requiredHeight(20.dp)
                    .padding(vertical = 8.dp),
                color = if (isComplete) Color(0xFF388E3C) else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = StrokeCap.Butt
            )

            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Divider()
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "${strings.workoutType}: ${goal.workoutType.displayName}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "${strings.goalType}: ${goal.goalType.displayName}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (goal.description.isNotBlank()) {
                    Text(
                        "${strings.notes}: ${goal.description}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (!goal.startDate.isNullOrBlank()) {
                    Text(
                        "${strings.startDate}: ${goal.startDate}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (!goal.endDate.isNullOrBlank()) {
                    Text(
                        "${strings.endDate}: ${goal.endDate}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    "${strings.date}: ${goal.createdAt}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(strings.deleteGoal) },
            text = { Text(String.format(strings.deleteGoalConfirm, goal.title)) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    onDelete(goal)
                }) { Text(strings.delete) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text(strings.cancel) }
            }
        )
    }
}

@SuppressLint("AutoboxingStateCreation")
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(navController: NavController, userId: String) {
    val strings = strings()
    var goals by remember { mutableStateOf<List<Goal>>(emptyList()) }
    var workouts by remember { mutableStateOf<List<Workout>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var showAddGoalDialog by remember { mutableStateOf(false) }
    var showEditGoalDialog by remember { mutableStateOf<Goal?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf<Goal?>(null) }

    val coroutineScope = rememberCoroutineScope()

    fun loadData() {
        coroutineScope.launch {
            isLoading = true
            try {
                val fetchedGoals = fetchGoals(userId)
                val fetchedWorkouts = fetchWorkouts(userId)
                goals = fetchedGoals.map { goal ->
                    goal.copy(current = calculateGoalProgress(goal, fetchedWorkouts))
                }
                workouts = fetchedWorkouts
                error = null
            } catch (e: Exception) {
                error = e.message
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadData()
    }

    if (showAddGoalDialog) {
        AddGoalDialog(
            onDismiss = { showAddGoalDialog = false },
            onGoalAdded = {
                showAddGoalDialog = false
                loadData()
            },
            userId = userId
        )
    }

    showEditGoalDialog?.let { goal ->
        EditGoalDialog(
            goal = goal,
            onDismiss = { showEditGoalDialog = null },
            onGoalUpdated = {
                showEditGoalDialog = null
                loadData()
            },
            userId = userId
        )
    }

    showDeleteConfirmDialog?.let { goal ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = null },
            title = { Text(strings.deleteGoal) },
            text = { Text(String.format(strings.deleteGoalConfirm, goal.title)) },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            if (deleteGoal(goal.id!!)) {
                                showDeleteConfirmDialog = null
                                loadData()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text(strings.delete) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = null }) {
                    Text(strings.cancel)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.goals) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = strings.back)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddGoalDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = strings.addGoalPrompt)
            }
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (error != null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("${strings.loadingError}: $error")
            }
        } else if (goals.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(strings.noGoalsFound)
                    Text(strings.addGoalPrompt)
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                goals.forEach { goal ->
                    GoalCard(
                        goal = goal,
                        onEdit = { showEditGoalDialog = goal },
                        onDelete = { showDeleteConfirmDialog = goal }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun GoalItem(goal: Goal, onEdit: () -> Unit, onDelete: () -> Unit) {
    val strings = strings()
    var expanded by remember { mutableStateOf(false) }
    val progress = (goal.current / goal.target).toFloat().coerceIn(0f, 1f)
    
    val workoutTypeName = when(goal.workoutType) {
        WorkoutType.RUNNING -> strings.running
        WorkoutType.STRENGTH -> strings.strengthTraining
        WorkoutType.YOGA -> strings.yoga
        else -> goal.workoutType.displayName
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .animateContentSize()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(goal.title, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.weight(1f))
                IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = strings.edit) }
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = strings.delete) }
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (expanded) {
                Text(goal.description, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text("${strings.workoutType}: $workoutTypeName", style = MaterialTheme.typography.bodySmall)
                Text("${strings.goalType}: ${goal.goalType.displayName}", style = MaterialTheme.typography.bodySmall)
                goal.startDate?.let { Text("${strings.startDate}: $it", style = MaterialTheme.typography.bodySmall) }
                goal.endDate?.let { Text("${strings.endDate}: $it", style = MaterialTheme.typography.bodySmall) }
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                strokeCap = StrokeCap.Round
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${goal.current.toInt()} ${strings.of} ${goal.target.toInt()} ${goal.unit} ${strings.completedGoal}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}