package com.example.workoutbuddyapplication.screens

import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.navigation.NavController
import com.example.workoutbuddyapplication.models.Exercise
import com.example.workoutbuddyapplication.components.BottomNavBar
import com.example.workoutbuddyapplication.components.ExerciseDetailContent
import com.example.workoutbuddyapplication.components.ExerciseDetailError
import com.example.workoutbuddyapplication.components.ExerciseDetailLoading
import com.example.workoutbuddyapplication.components.ExerciseDetailTopBar
import com.example.workoutbuddyapplication.data.ExerciseRepository.fetchExerciseByName

@Composable
fun ExerciseDetailScreen(
    navController: NavController,
    exerciseName: String,
    fetchExercise: suspend (String) -> Exercise? = { fetchExerciseByName(it) }
) {
    var exercise by remember { mutableStateOf<Exercise?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var selectedTabIndex by remember { mutableStateOf(2) }

    LaunchedEffect(exerciseName) {
        isLoading = true
        error = null
        try {
            exercise = fetchExercise(exerciseName)
        } catch (e: Exception) {
            error = "Could not load exercise."
        }
        isLoading = false
    }

    Scaffold(
        topBar = {
            ExerciseDetailTopBar(
                title = exerciseName,
                onBack = { navController.navigateUp() }
            )
        },
        bottomBar = {
            BottomNavBar(
                selectedTabIndex = selectedTabIndex,
                onTabSelected = { selectedTabIndex = it },
                navController = navController
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.TopCenter
        ) {
            when {
                isLoading -> ExerciseDetailLoading(Modifier.align(Alignment.Center))
                error != null -> ExerciseDetailError(error!!, Modifier.align(Alignment.Center))
                exercise != null -> ExerciseDetailContent(exercise!!)
            }
        }
    }
}