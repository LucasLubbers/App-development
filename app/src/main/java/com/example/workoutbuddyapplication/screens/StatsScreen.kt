package com.example.workoutbuddyapplication.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.workoutbuddyapplication.components.*
import com.example.workoutbuddyapplication.navigation.Screen
import com.example.workoutbuddyapplication.viewmodel.StatsViewModel
import com.example.workoutbuddyapplication.utils.StatsResult
import com.example.workoutbuddyapplication.data.WorkoutRepositoryImpl
import com.example.workoutbuddyapplication.data.SupabaseClient
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.jan.supabase.gotrue.auth
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun StatsScreen(
    navController: NavController
) {
    val user = remember { SupabaseClient.client.auth.currentUserOrNull() }
    if (user == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Je bent niet ingelogd.")
        }
        return
    }
    val profileId = user.id

    val viewModel: StatsViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return StatsViewModel(WorkoutRepositoryImpl(profileId)) as T
            }
        }
    )

    var selectedTabIndex by remember { mutableStateOf(3) }
    var selectedTimeRange by remember { mutableStateOf(0) }
    val timeRanges = listOf("Week", "Maand", "Jaar")

    val stats by viewModel.stats.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadStats()
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
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Statistieken",
                    fontSize = 24.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = { navController.navigate(Screen.Goals.route) }
                ) {
                    Text("Doelen Bekijken")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TabRow(selectedTabIndex = selectedTimeRange) {
                timeRanges.forEachIndexed { index, label ->
                    Tab(
                        selected = selectedTimeRange == index,
                        onClick = { selectedTimeRange = index },
                        text = { Text(label) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when {
                isLoading -> CircularProgressIndicator()
                error != null -> Text(error!!, color = MaterialTheme.colorScheme.error)
                stats == null -> {}
                else -> {
                    val stat: StatsResult = stats!!
                    val now = LocalDate.now()
                    val (workouts, totalDuration, totalDistance) = when (selectedTimeRange) {
                        0 -> Triple(
                            stat.currentWeekWorkouts,
                            stat.currentWeekWorkouts.sumOf { it.duration },
                            stat.currentWeekWorkouts.mapNotNull { it.distance }.sum()
                        )

                        1 -> Triple(
                            stat.currentMonthWorkouts,
                            stat.currentMonthWorkouts.sumOf { it.duration },
                            stat.currentMonthWorkouts.mapNotNull { it.distance }.sum()
                        )

                        else -> Triple(
                            stat.currentYearWorkouts,
                            stat.currentYearWorkouts.sumOf { it.duration },
                            stat.currentYearWorkouts.mapNotNull { it.distance }.sum()
                        )
                    }

                    StatsSummaryCards(
                        totalWorkouts = workouts.size,
                        totalDurationHours = totalDuration / 60.0,
                        totalDistance = totalDistance,
                        avgPerWeek = stat.avgPerWeek,
                        showAvgPerWeek = selectedTimeRange == 1,
                        avgDurationPerWorkoutWeek = stat.avgDurationPerWorkoutWeek,
                        showAvgDurationPerWorkoutWeek = selectedTimeRange == 0,
                        avgWorkoutsPerMonth = stat.avgWorkoutsPerMonth,
                        showAvgWorkoutsPerMonth = selectedTimeRange == 2
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Workout Activiteit",
                        fontSize = 20.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    when (selectedTimeRange) {
                        0 -> WorkoutActivityChart(stat.currentWeekWorkouts)
                        1 -> CalendarMonthViewStyled(
                            year = now.year,
                            month = now.monthValue,
                            workoutDays = stat.currentMonthWorkouts.map { LocalDate.parse(it.date).dayOfMonth }
                        )

                        2 -> YearHeatmap(
                            year = now.year,
                            workoutDates = stat.currentYearWorkouts.map { LocalDate.parse(it.date) }
                        )
                    }

                    Spacer(modifier = Modifier.height(48.dp))
                    Text(
                        text = "Verdeling per type",
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    val filteredTypeDist = when (selectedTimeRange) {
                        0 -> stat.currentWeekWorkouts
                        1 -> stat.currentMonthWorkouts
                        else -> stat.currentYearWorkouts
                    }.groupingBy { it.workoutTypeEnum }.eachCount().withDefault { 0 }
                    val totalForDist = filteredTypeDist.values.sum().takeIf { it > 0 } ?: 1
                    fun percent(count: Int) = (count * 100 / totalForDist)

                    WorkoutTypeDistribution(
                        running = percent(filteredTypeDist.getValue(com.example.workoutbuddyapplication.models.WorkoutType.RUNNING)),
                        cycling = percent(filteredTypeDist.getValue(com.example.workoutbuddyapplication.models.WorkoutType.CYCLING)),
                        strength = percent(filteredTypeDist.getValue(com.example.workoutbuddyapplication.models.WorkoutType.STRENGTH)),
                        other = percent(filteredTypeDist.getValue(com.example.workoutbuddyapplication.models.WorkoutType.OTHER))
                    )
                }
            }
        }
    }
}