package com.example.workoutbuddyapplication.screens

import com.example.workoutbuddyapplication.models.Workout
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
import androidx.compose.ui.Alignment
import com.example.workoutbuddyapplication.components.BottomNavBar
import com.example.workoutbuddyapplication.data.WorkoutRepositoryImpl
import com.example.workoutbuddyapplication.models.WorkoutExerciseWithDetails
import com.example.workoutbuddyapplication.navigation.Screen
import com.example.workoutbuddyapplication.ui.theme.strings
import com.example.workoutbuddyapplication.viewmodel.WorkoutDetailViewModel
import com.example.workoutbuddyapplication.viewmodel.WorkoutDetailViewModelFactory
import com.example.workoutbuddyapplication.data.SupabaseClient
import io.github.jan.supabase.gotrue.auth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutDetailScreen(
    workoutId: Int,
    navController: NavController,
    selectedTabIndex: Int
) {
    val strings = strings()
    val user = remember { SupabaseClient.client.auth.currentUserOrNull() }
    if (user == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("You are not logged in.")
        }
        return
    }
    val profileId = user.id

    val viewModel: WorkoutDetailViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = WorkoutDetailViewModelFactory(WorkoutRepositoryImpl(profileId))
    )

    val workout by viewModel.workout.collectAsState()
    val exercises by viewModel.exercises.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    var currentTabIndex by remember { mutableIntStateOf(selectedTabIndex) }

    LaunchedEffect(workoutId) {
        viewModel.loadWorkoutDetails(workoutId, strings.failedToLoadWorkoutDetails)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.workoutDetails) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
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
                workout == null -> Text(strings.workoutNotFound)
                else -> {
                    WorkoutDetailContent(
                        workout = workout!!,
                        exercises = exercises,
                        navController = navController
                    )
                }
            }
        }
    }
}

@Composable
fun WorkoutDetailContent(
    workout: Workout,
    exercises: List<WorkoutExerciseWithDetails>,
    navController: NavController
) {
    val strings = strings()
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Icon(
                imageVector = when (workout.type) {
                    "RUNNING" -> Icons.AutoMirrored.Filled.DirectionsRun
                    "STRENGTH" -> Icons.Filled.FitnessCenter
                    else -> Icons.Filled.SelfImprovement
                },
                contentDescription = null,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = workout.type,
                style = MaterialTheme.typography.titleLarge
            )
        }
        Text("${strings.date}: ${workout.date}")
        Text("${strings.duration}: ${workout.duration} min")
        workout.distance?.let { Text("${strings.distance}: $it km") }
        workout.notes?.takeIf { it.isNotBlank() }?.let { Text("${strings.notes}: $it") }

        Spacer(modifier = Modifier.height(16.dp))

        if (exercises.isNotEmpty()) {
            Text(strings.exercises, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn {
                items(exercises) { item ->
                    WorkoutExerciseDetailCard(item, navController)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun WorkoutExerciseDetailCard(item: WorkoutExerciseWithDetails, navController: NavController) {
    val strings = strings()
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
            item.sets?.let { Text("${strings.sets}: $it") }
            item.reps?.let { Text("${strings.reps}: $it") }
            item.weight?.let { Text("${strings.weight}: $it kg") }
            item.restTime?.let { Text("${strings.restTime}: $it sec") }
            item.notes?.takeIf { it.isNotBlank() }?.let { Text("${strings.notes}: $it") }
        }
    }
}