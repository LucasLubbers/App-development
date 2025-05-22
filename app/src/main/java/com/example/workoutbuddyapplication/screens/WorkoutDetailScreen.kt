package com.example.workoutbuddyapplication.screens

import Workout
import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.withContext
import com.example.workoutbuddyapplication.BuildConfig
import kotlinx.coroutines.Dispatchers
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import com.example.workoutbuddyapplication.models.Exercise
import androidx.compose.ui.Alignment
import com.example.workoutbuddyapplication.models.WorkoutExerciseWithDetails

suspend fun fetchWorkoutById(workoutId: Int): Workout? = withContext(Dispatchers.IO) {
    val client = OkHttpClient()
    val request = Request.Builder()
        .url("https://attsgwsxdlblbqxnboqx.supabase.co/rest/v1/workouts?id=eq.$workoutId&select=*")
        .addHeader("apikey", BuildConfig.SUPABASE_ANON_KEY)
        .addHeader("Authorization", "Bearer ${BuildConfig.SUPABASE_ANON_KEY}")
        .build()
    val response = client.newCall(request).execute()
    val responseBody = response.body?.string() ?: return@withContext null
    val jsonArray = JSONArray(responseBody)
    if (jsonArray.length() == 0) return@withContext null
    val obj = jsonArray.getJSONObject(0)
    Workout(
        id = obj.getInt("id"),
        type = WorkoutType.fromString(obj.getString("type")),
        date = java.time.LocalDate.parse(obj.getString("date")),
        duration = obj.getInt("duration"),
        distance = if (obj.isNull("distance")) null else obj.getDouble("distance"),
        notes = if (obj.isNull("notes")) null else obj.getString("notes")
    )
}

suspend fun fetchExercisesForWorkout(workoutId: Int): List<WorkoutExerciseWithDetails> = withContext(Dispatchers.IO) {
    val client = OkHttpClient()
    val request = Request.Builder()
        .url("https://attsgwsxdlblbqxnboqx.supabase.co/rest/v1/workout_exercises?workout_id=eq.$workoutId&select=*,exercise:exercises(*)")
        .addHeader("apikey", BuildConfig.SUPABASE_ANON_KEY)
        .addHeader("Authorization", "Bearer ${BuildConfig.SUPABASE_ANON_KEY}")
        .build()
    val response = client.newCall(request).execute()
    val responseBody = response.body?.string() ?: return@withContext emptyList()
    val jsonArray = JSONArray(responseBody)
    val result = mutableListOf<WorkoutExerciseWithDetails>()
    for (i in 0 until jsonArray.length()) {
        val obj = jsonArray.getJSONObject(i)
        val exerciseObj = obj.getJSONObject("exercise")
        val exercise = Exercise(
            name = exerciseObj.getString("name"),
            force = exerciseObj.optString("force", ""),
            level = exerciseObj.optString("level", ""),
            mechanic = exerciseObj.optString("mechanic", ""),
            equipment = exerciseObj.optString("equipment", ""),
            primaryMuscles = exerciseObj.optJSONArray("primary_muscles")?.let { arr ->
                List(arr.length()) { arr.getString(it) }
            } ?: emptyList(),
            secondaryMuscles = exerciseObj.optJSONArray("secondary_muscles")?.let { arr ->
                List(arr.length()) { arr.getString(it) }
            } ?: emptyList(),
            instructions = exerciseObj.optJSONArray("instructions")?.let { arr ->
                List(arr.length()) { arr.getString(it) }
            } ?: emptyList(),
            category = exerciseObj.optString("category", "")
        )
        result.add(
            WorkoutExerciseWithDetails(
                exercise = exercise,
                sets = if (obj.isNull("sets")) null else obj.getInt("sets"),
                reps = if (obj.isNull("reps")) null else obj.getInt("reps"),
                weight = if (obj.isNull("weight")) null else obj.getDouble("weight"),
                notes = if (obj.isNull("notes")) null else obj.getString("notes"),
                restTime = if (obj.isNull("rest_time")) null else obj.getInt("rest_time")
            )
        )
    }
    result
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutDetailScreen(
    workoutId: Int,
    navController: NavController,
    selectedTabIndex: Int
) {
    var workout by remember { mutableStateOf<Workout?>(null) }
    var exercises by remember { mutableStateOf<List<WorkoutExerciseWithDetails>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var currentTabIndex by remember { mutableIntStateOf(selectedTabIndex) }

    LaunchedEffect(workoutId) {
        isLoading = true
        error = null
        try {
            workout = fetchWorkoutById(workoutId)
            exercises = fetchExercisesForWorkout(workoutId)
        } catch (e: Exception) {
            error = "Workout details niet kunnen laden."
        }
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Workout Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentTabIndex == 0,
                    onClick = {
                        currentTabIndex = 0
                        navController.navigate("dashboard")
                    },
                    icon = { Icon(Icons.Default.FitnessCenter, contentDescription = "Dashboard") },
                    label = { Text("Dashboard") }
                )
                NavigationBarItem(
                    selected = currentTabIndex == 1,
                    onClick = {
                        currentTabIndex = 1
                        navController.navigate("history")
                    },
                    icon = { Icon(Icons.AutoMirrored.Filled.DirectionsRun, contentDescription = "History") },
                    label = { Text("Geschiedenis") }
                )
                NavigationBarItem(
                    selected = currentTabIndex == 2,
                    onClick = {
                        currentTabIndex = 2
                        navController.navigate("exercises")
                    },
                    icon = { Icon(Icons.Default.FitnessCenter, contentDescription = "Exercises") },
                    label = { Text("Oefeningen") }
                )
                NavigationBarItem(
                    selected = currentTabIndex == 3,
                    onClick = {
                        currentTabIndex = 3
                        navController.navigate("stats")
                    },
                    icon = { Icon(Icons.Default.SelfImprovement, contentDescription = "Stats") },
                    label = { Text("Statistieken") }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            when {
                isLoading -> CircularProgressIndicator()
                error != null -> Text(error!!, color = MaterialTheme.colorScheme.error)
                workout == null -> Text("Workout not found.")
                else -> {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = workout!!.type.icon,
                                contentDescription = workout!!.type.displayName,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = workout!!.type.displayName,
                                    style = MaterialTheme.typography.titleLarge
                                )
                                Text("Datum: ${workout!!.date}")
                                Text("Duur: ${workout!!.duration} min")
                                workout!!.distance?.let { Text("Afstand: $it km") }
                                workout!!.notes?.let { Text("Notities: $it") }
                            }
                        }
                    }
                    Text("Oefeningen:", style = MaterialTheme.typography.titleMedium)
                    LazyColumn {
                        items(exercises) { item ->
                            WorkoutExerciseDetailCard(item)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WorkoutExerciseDetailCard(item: WorkoutExerciseWithDetails) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(item.exercise.name, style = MaterialTheme.typography.titleMedium)
            item.sets?.let { Text("Sets: $it") }
            item.reps?.let { Text("Reps: $it") }
            item.weight?.let { Text("Gewicht: $it kg") }
            item.restTime?.let { Text("Rust: $it sec") }
            item.notes?.takeIf { it.isNotBlank() }?.let { Text("Notities: $it") }
        }
    }
}