package com.example.workoutbuddyapplication.screens

import Workout
import WorkoutType
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.workoutbuddyapplication.BuildConfig
import com.example.workoutbuddyapplication.components.BottomNavBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.time.Month
import java.time.format.TextStyle
import java.util.Locale
import androidx.compose.ui.platform.LocalContext

suspend fun fetchWorkouts(userId: String): List<Workout> = withContext(Dispatchers.IO) {
    val client = OkHttpClient()
    val request = Request.Builder()
        .url("https://attsgwsxdlblbqxnboqx.supabase.co/rest/v1/workouts?profile_id=eq.$userId&select=*")
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
    val context = LocalContext.current
    var workouts by remember { mutableStateOf<List<Workout>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var selectedTabIndex by remember { mutableStateOf(1) }

    // Filter state
    var expanded by remember { mutableStateOf(false) }
    var selectedType by remember { mutableStateOf<  WorkoutType?>(null) }

    LaunchedEffect(Unit) {
        isLoading = true
        error = null
        val userId = getUserId(context)
        if (userId != null) {
            val result = fetchWorkouts(userId)
            if (result.isNotEmpty()) {
                workouts = result
            } else {
                error = "No workouts found or failed to fetch."
            }
        } else {
            error = "User not logged in."
        }
        isLoading = false
    }

    // Filter workouts by selected type
    val filteredWorkouts = selectedType?.let { type ->
        workouts.filter { it.type == type }
    } ?: workouts

    val workoutsByMonth = filteredWorkouts.groupBy {
        Month.of(it.date.monthValue).getDisplayName(TextStyle.FULL, Locale.getDefault()) + " " + it.date.year
    }

    Scaffold(
        bottomBar = {
            BottomNavBar(
                selectedTabIndex = selectedTabIndex,
                onTabSelected = { selectedTabIndex = it },
                navController = navController
            )
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

            // Filter dropdown
            Box {
                Button(onClick = { expanded = true }) {
                    Text(selectedType?.displayName ?: "Alle types")
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Alle types") },
                        onClick = {
                            selectedType = null
                            expanded = false
                        }
                    )
                    WorkoutType.values().forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type.displayName) },
                            onClick = {
                                selectedType = type
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when {
                isLoading -> {
                    CircularProgressIndicator()
                }
                error != null -> {
                    Text(error!!, color = MaterialTheme.colorScheme.error)
                }
                filteredWorkouts.isEmpty() -> {
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
                                WorkoutItem(
                                    workout = workout,
                                    onClick = { navController.navigate("workoutDetail/${workout.id}/1") }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}