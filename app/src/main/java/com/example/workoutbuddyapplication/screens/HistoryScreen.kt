package com.example.workoutbuddyapplication.screens

import com.example.workoutbuddyapplication.models.Workout
import com.example.workoutbuddyapplication.models.WorkoutType
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.workoutbuddyapplication.BuildConfig
import com.example.workoutbuddyapplication.components.BottomNavBar
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.time.Month
import java.time.format.TextStyle
import java.util.Locale
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.workoutbuddyapplication.viewmodel.HistoryViewModel

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
                    type = obj.getString("type"),
                    date = obj.getString("date"),
                    duration = obj.getInt("duration"),
                    distance = if (obj.isNull("distance")) null else obj.getDouble("distance"),
                    notes = if (obj.isNull("notes")) null else obj.getString("notes"),
                    profileId = obj.getString("profile_id")
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
fun HistoryScreen(navController: NavController, selectedLanguage: String) {
    val context = LocalContext.current
    val viewModel: HistoryViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return HistoryViewModel() as T
            }
        }
    )

    val workouts by viewModel.workouts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val selectedType by viewModel.selectedType.collectAsState()
    val showCalendarView by viewModel.showCalendarView.collectAsState()
    val calendarMonth by viewModel.calendarMonth.collectAsState()

    var selectedTabIndex by remember { mutableStateOf(1) }
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val userId = getUserId(context)
        if (userId != null) {
            viewModel.fetchWorkouts(userId, ::fetchWorkouts)
        } else {
            viewModel.setSelectedType(null)
        }
    }

    val filteredWorkouts = selectedType?.let { type ->
        workouts.filter { it.workoutTypeEnum == type }
    } ?: workouts

    val sortedWorkouts = filteredWorkouts.sortedByDescending { it.date }

    val workoutsByMonth = sortedWorkouts.groupBy {
        val localDate = LocalDate.parse(it.date)
        Pair(localDate.year, localDate.monthValue)
    }

    val monthsOrdered = workoutsByMonth.keys.sortedByDescending { (year, month) ->
        year * 100 + month
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

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp, top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                                viewModel.setSelectedType(null)
                                expanded = false
                            }
                        )
                        WorkoutType.values().forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.displayName) },
                                onClick = {
                                    viewModel.setSelectedType(type)
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Button(onClick = { viewModel.toggleCalendarView() }) {
                    Text(if (showCalendarView) "Lijst" else "Kalender")
                }
            }

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
                    if (showCalendarView) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = {
                                viewModel.setCalendarMonth(calendarMonth.minusMonths(1))
                            }) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Previous month"
                                )
                            }
                            Text(
                                text = "${
                                    calendarMonth.month.getDisplayName(
                                        TextStyle.FULL,
                                        Locale(selectedLanguage)
                                    )
                                } ${calendarMonth.year}",
                                style = MaterialTheme.typography.titleMedium
                            )
                            IconButton(onClick = {
                                viewModel.setCalendarMonth(calendarMonth.plusMonths(1))
                            }) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Next month"
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        val monthWorkouts = filteredWorkouts.filter {
                            val date = LocalDate.parse(it.date)
                            date.year == calendarMonth.year && date.monthValue == calendarMonth.monthValue
                        }
                        CalendarMonthViewStyled(
                            year = calendarMonth.year,
                            month = calendarMonth.monthValue,
                            workoutDays = monthWorkouts.map { LocalDate.parse(it.date).dayOfMonth }
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            monthsOrdered.forEach { (year, month) ->
                                val monthLocale = Locale(selectedLanguage)
                                val monthName =
                                    Month.of(month).getDisplayName(TextStyle.FULL, monthLocale)
                                val header = "$monthName $year"
                                val monthWorkouts =
                                    workoutsByMonth[Pair(year, month)] ?: emptyList()
                                item {
                                    Text(
                                        text = header,
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                                items(monthWorkouts) { workout ->
                                    WorkoutItem(
                                        workout = workout,
                                        onClick = if (workout.workoutTypeEnum == WorkoutType.STRENGTH) {
                                            { navController.navigate("workoutDetail/${workout.id}/1") }
                                        } else {
                                            {}
                                        }
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
}