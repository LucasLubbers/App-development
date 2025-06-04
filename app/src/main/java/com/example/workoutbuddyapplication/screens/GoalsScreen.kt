package com.example.workoutbuddyapplication.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
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
import kotlin.div

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
                    startDate = obj.optString("start_date", null),
                    endDate = obj.optString("end_date", null),
                    createdAt = obj.optString("created_at", null),
                    description = obj.optString("description", ""),
                    current = 0.0 // Always set to 0, will be calculated locally
                )
            )
        }
        goals
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }
}

@Composable
fun GoalCard(goal: Goal) {
    var expanded by remember { mutableStateOf(false) }
    val rawProgress = (goal.current / goal.target).coerceIn(0.0, 1.0).toFloat()
    val isComplete = rawProgress >= 1.0f
    val progress = if (isComplete) 0.999f else rawProgress // Avoid dot at end

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { expanded = !expanded },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (expanded) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(goal.title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(
                        "Doel: ${goal.target} ${goal.unit}",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Minder info" else "Meer info"
                )
            }
            Text(
                if (isComplete) "Voltooid" else "Voortgang: ${goal.current} ${goal.unit}",
                color = if (isComplete) Color(0xFF388E3C) else LocalContentColor.current,
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
                Text("Workout type: ${goal.workoutType.displayName}")
                Text("Type doel: ${goal.goalType.displayName}")
                if (goal.description.isNotBlank()) {
                    Text("Beschrijving: ${goal.description}")
                }
                if (!goal.startDate.isNullOrBlank()) {
                    Text("Startdatum: ${goal.startDate}")
                }
                if (!goal.endDate.isNullOrBlank()) {
                    Text("Einddatum: ${goal.endDate}")
                }
                Text("Aangemaakt op: ${goal.createdAt}")
            }
        }
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

    LaunchedEffect(Unit) {
        isLoading = true
        error = null
        val userId = getUserId(context)
        if (userId != null) {
            val fetchedGoals = fetchGoals(userId)
            val fetchedWorkouts = fetchWorkouts(userId)
            goals = fetchedGoals.map { goal ->
                goal.copy(current = calculateGoalProgress(goal, fetchedWorkouts))
            }
        } else {
            error = "User not logged in."
        }
        isLoading = false
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
                onClick = { /* Open add goal dialog */ }
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
                        GoalCard(goal = goal)
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
                        GoalCard(goal = goal)
                    }
                }
            }
        }
    }
}