package com.example.workoutbuddyapplication.screens

import com.example.workoutbuddyapplication.models.Workout
import com.example.workoutbuddyapplication.models.WorkoutType
import androidx.compose.foundation.clickable
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
import java.time.LocalDate
import com.example.workoutbuddyapplication.models.Exercise
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import com.example.workoutbuddyapplication.R
import com.example.workoutbuddyapplication.components.BottomNavBar
import com.example.workoutbuddyapplication.models.WorkoutExerciseWithDetails
import com.example.workoutbuddyapplication.navigation.Screen

suspend fun fetchWorkoutById(workoutId: Int): Workout? = withContext(Dispatchers.IO) {
    val client = OkHttpClient()
    val request = Request.Builder()
        .url("https://attsgwsxdlblbqxnboqx.supabase.co/rest/v1/workouts?id=eq.$workoutId&select=*")
        .addHeader("apikey", BuildConfig.SUPABASE_ANON_KEY)
        .addHeader("Authorization", "Bearer ${BuildConfig.SUPABASE_ANON_KEY}")
        .build()
    
    try {
        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: return@withContext null
        
        if (!response.isSuccessful) {
            println("Error fetching workout: HTTP ${response.code}")
            return@withContext null
        }
        
        val jsonArray = JSONArray(responseBody)
        if (jsonArray.length() == 0) {
            println("No workout found with id: $workoutId")
            return@withContext null
        }
        
        val obj = jsonArray.getJSONObject(0)
        println("Fetched workout data: $obj")
        
        Workout(
            id = obj.getInt("id"),
            type = obj.getString("type"),
            date = obj.getString("date"),
            duration = if (obj.get("duration") is String) obj.getString("duration").toInt() else obj.getInt("duration"),
            distance = if (obj.isNull("distance")) null else {
                when (val distanceValue = obj.get("distance")) {
                    is String -> distanceValue.toDouble()
                    is Number -> distanceValue.toDouble()
                    else -> null
                }
            },
            notes = if (obj.isNull("notes")) null else obj.getString("notes"),
            profileId = if (obj.isNull("profile_id")) null else obj.getString("profile_id")
        )
    } catch (e: Exception) {
        println("Error parsing workout data: ${e.message}")
        e.printStackTrace()
        null
    }
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
            error = "Failed to load workout details"
        }
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.workout_details)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        },
        bottomBar = {
            BottomNavBar(
                selectedTabIndex = currentTabIndex,
                onTabSelected = { currentTabIndex = it },
                navController = navController
            )
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
                workout == null -> Text(stringResource(R.string.workout_not_found))
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
                                imageVector = workout!!.workoutTypeEnum.icon,
                                contentDescription = workout!!.workoutTypeEnum.displayName,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = workout!!.workoutTypeEnum.displayName,
                                    style = MaterialTheme.typography.titleLarge
                                )
                                Text("${stringResource(R.string.date)}: ${workout!!.date}")
                                Text("${stringResource(R.string.duration)}: ${workout!!.duration} ${stringResource(R.string.minutes)}")
                                workout!!.distance?.let { Text("${stringResource(R.string.distance)}: $it km") }
                                workout!!.notes?.let { Text("${stringResource(R.string.notes)}: $it") }
                            }
                        }
                    }
                    Text("${stringResource(R.string.exercises)}:", style = MaterialTheme.typography.titleMedium)
                    LazyColumn {
                        items(exercises) { item ->
                            WorkoutExerciseDetailCard(item, navController)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WorkoutExerciseDetailCard(item: WorkoutExerciseWithDetails, navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                navController.navigate(Screen.ExerciseDetail.createRoute(item.exercise.name))
            }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = item.exercise.name,
                style = MaterialTheme.typography.titleMedium
            )
            item.sets?.let { Text("${stringResource(R.string.sets)}: $it") }
            item.reps?.let { Text("${stringResource(R.string.reps)}: $it") }
            item.weight?.let { Text("${stringResource(R.string.weight)}: $it kg") }
            item.restTime?.let { Text("${stringResource(R.string.rest_time)}: $it sec") }
            item.notes?.takeIf { it.isNotBlank() }?.let { Text("${stringResource(R.string.notes)}: $it") }
        }
    }
}