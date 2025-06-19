package com.example.workoutbuddyapplication.screens

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import com.example.workoutbuddyapplication.components.AddGoalDialog
import com.example.workoutbuddyapplication.components.EditGoalDialog
import com.example.workoutbuddyapplication.components.GoalCard
import okhttp3.RequestBody.Companion.toRequestBody
import com.example.workoutbuddyapplication.services.NotificationService

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

@SuppressLint("AutoboxingStateCreation")
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
                                NotificationService.sendGoalDeadlineNotification(
                                    context,
                                    goal.id ?: 0,
                                    goal.title
                                )
                            }
                        } catch (_: Exception) {
                        }
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