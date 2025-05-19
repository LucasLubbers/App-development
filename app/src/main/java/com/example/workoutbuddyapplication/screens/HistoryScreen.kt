package com.example.workoutbuddyapplication.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.workoutbuddyapplication.models.Workout
import com.example.workoutbuddyapplication.models.WorkoutType
import com.example.workoutbuddyapplication.navigation.Screen
import java.time.LocalDate
import java.time.Month
import java.time.format.TextStyle
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HistoryScreen(navController: NavController) {
    val sampleWorkouts = remember {
        listOf(
            Workout(
                id = 1,
                type = WorkoutType.RUNNING,
                date = LocalDate.now(),
                duration = 30,
                distance = 5.0,
                notes = "Goed gevoel"
            ),
            Workout(
                id = 2,
                type = WorkoutType.STRENGTH,
                date = LocalDate.now().minusDays(1),
                duration = 45,
                distance = null,
                notes = "Zwaar maar productief"
            ),
            Workout(
                id = 3,
                type = WorkoutType.YOGA,
                date = LocalDate.now().minusDays(2),
                duration = 60,
                distance = null,
                notes = "Ontspannend"
            ),
            Workout(
                id = 4,
                type = WorkoutType.RUNNING,
                date = LocalDate.now().minusDays(4),
                duration = 25,
                distance = 4.0,
                notes = "Kort maar krachtig"
            ),
            Workout(
                id = 5,
                type = WorkoutType.CYCLING,
                date = LocalDate.now().minusDays(6),
                duration = 90,
                distance = 30.0,
                notes = "Lange fietstocht"
            )
        )
    }

    var selectedTabIndex by remember { mutableStateOf(1) }

    // Group workouts by month
    val workoutsByMonth = sampleWorkouts.groupBy {
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
