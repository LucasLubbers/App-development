package com.example.workoutbuddyapplication.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.workoutbuddyapplication.models.Exercise

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDetailScreen(
    navController: NavController,
    exerciseName: String,
    onLoadExercise: (String) -> Exercise?
) {
    val exercise = remember(exerciseName) { onLoadExercise(exerciseName) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(exerciseName) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (exercise != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Exercise Name
                Text(
                    text = exercise.name,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Level
                Text(
                    text = "Level",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(text = exercise.level)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Equipment
                Text(
                    text = "Equipment",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(text = exercise.equipment ?: "No equipment needed")
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Primary Muscles
                Text(
                    text = "Primary Muscles",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
                exercise.primaryMuscles.forEach { muscle ->
                    Text(text = "• $muscle")
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Category
                Text(
                    text = "Category",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(text = exercise.category)

                Spacer(modifier = Modifier.height(24.dp))

                // Instructions
                Text(
                    text = "Instructions",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                exercise.instructions.forEachIndexed { index, instruction ->
                    Text(
                        text = "${index + 1}. $instruction",
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        } else {
            // Show error or loading state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Exercise not found")
            }
        }
    }
} 