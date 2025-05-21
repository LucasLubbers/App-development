package com.example.workoutbuddyapplication.screens

import Workout
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.workoutbuddyapplication.navigation.Screen
import com.example.workoutbuddyapplication.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.time.Month
import java.time.format.TextStyle
import java.util.Locale

suspend fun fetchWorkouts(): List<Workout> = withContext(Dispatchers.IO) {
    val client = OkHttpClient()
    val request = Request.Builder()
        .url("https://attsgwsxdlblbqxnboqx.supabase.co/rest/v1/workouts?select=*")
        .addHeader("apikey", BuildConfig.SUPABASE_ANON_KEY)
        .addHeader("Authorization", "Bearer ${BuildConfig.SUPABASE_ANON_KEY}")
        .build()

    try {
        val response = client.newCall(request).execute()
        val responseBody = response.body?.string()
        if (!response.isSuccessful || responseBody == null) return@withContext emptyList()
        val jsonArray = JSONArray(responseBody)
        val workouts = mutableListOf<Workout>()
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            workouts.add(
                Workout(
                    id = obj.getInt("id"),
                    type = WorkoutType.fromString(obj.getString("type")),
                    date = java.time.LocalDate.parse(obj.getString("date")),
                    duration = obj.getInt("duration"),
                    distance = if (obj.isNull("distance")) null else obj.getDouble("distance"),
                    notes = if (obj.isNull("notes")) null else obj.getString("notes")
                )
            )
        }
        workouts
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HistoryScreen(navController: NavController) {
    var workouts by remember { mutableStateOf<List<Workout>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var selectedTabIndex by remember { mutableStateOf(1) }

    LaunchedEffect(Unit) {
        isLoading = true
        error = null
        val result = fetchWorkouts()
        if (result.isNotEmpty()) {
            workouts = result
        } else {
            error = "No workouts found or failed to fetch."
        }
        isLoading = false
    }

    // Group workouts by month
    val workoutsByMonth = workouts.groupBy {
        Month.of(it.date.monthValue).getDisplayName(TextStyle.FULL, Locale.getDefault()) + " " + it.date.year
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTabIndex == 0,
                    onClick = {
                        selectedTabIndex = 0
                        navController.navigate(Screen.Dashboard.route)
                    },
                    icon = { Icon(Icons.Default.FitnessCenter, contentDescription = "Dashboard") },
                    label = { Text("Dashboard") }
                )
                NavigationBarItem(
                    selected = selectedTabIndex == 1,
                    onClick = {
                        selectedTabIndex = 1
                        navController.navigate(Screen.History.route)
                    },
                    icon = { Icon(Icons.Default.DirectionsRun, contentDescription = "Geschiedenis") },
                    label = { Text("Geschiedenis") }
                )
                NavigationBarItem(
                    selected = selectedTabIndex == 2,
                    onClick = {
                        selectedTabIndex = 2
                        navController.navigate(Screen.Exercises.route)
                    },
                    icon = { Icon(Icons.Default.FitnessCenter, contentDescription = "Oefeningen") },
                    label = { Text("Oefeningen") }
                )
                NavigationBarItem(
                    selected = selectedTabIndex == 3,
                    onClick = {
                        selectedTabIndex = 3
                        navController.navigate(Screen.Stats.route)
                    },
                    icon = { Icon(Icons.Default.SelfImprovement, contentDescription = "Statistieken") },
                    label = { Text("Statistieken") }
                )
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
                text = "Workout Geschiedenis",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            when {
                isLoading -> {
                    CircularProgressIndicator()
                }
                error != null -> {
                    Text(error!!, color = MaterialTheme.colorScheme.error)
                }
                workouts.isEmpty() -> {
                    Text("No workouts found.")
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        workoutsByMonth.forEach { (month, workouts) ->
                            item {
                                Text(
                                    text = month,
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                            items(workouts.sortedByDescending { it.date }) { workout ->
                                WorkoutItem(workout = workout)
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}