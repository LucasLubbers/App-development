package com.example.workoutbuddyapplication.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
                    current = obj.optDouble("current_value", 0.0)
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
            Text("Voortgang: ${goal.current} ${goal.unit}")

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
            val result = fetchGoals(userId)
            goals = result
        } else {
            error = "User not logged in."
        }
        isLoading = false
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
                goals.isEmpty() -> Text("Geen doelen gevonden.")
                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(goals) { goal ->
                        GoalCard(goal = goal)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { /* Open goal suggestions */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Doelsuggesties Bekijken")
            }
        }
    }
}