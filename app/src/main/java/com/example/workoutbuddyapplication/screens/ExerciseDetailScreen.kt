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
import com.example.workoutbuddyapplication.models.ExerciseDTO
import com.example.workoutbuddyapplication.data.SupabaseClient
import kotlinx.coroutines.launch
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDetailScreen(
    navController: NavController,
    exerciseName: String
) {
    var exercise by remember { mutableStateOf<Exercise?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // Fetch the exercise details from Supabase
    LaunchedEffect(key1 = exerciseName) {
        coroutineScope.launch {
            try {
                // Get all exercises with matching name - standard approach
                val response = SupabaseClient.client.postgrest
                    .from("exercises")
                    .select()
                    .decodeList<ExerciseDTO>()
                
                // Find the one with matching name
                val exerciseData = response.find { it.name == exerciseName }
                
                if (exerciseData != null) {
                    exercise = Exercise(
                        id = exerciseData.id,
                        name = exerciseData.name,
                        force = exerciseData.force,
                        level = exerciseData.level ?: "Beginner",
                        mechanic = exerciseData.mechanic,
                        equipment = exerciseData.equipment,
                        primaryMuscles = exerciseData.primary_muscles,
                        secondaryMuscles = exerciseData.secondary_muscles,
                        category = exerciseData.category,
                        instructions = exerciseData.instructions
                    )
                    isLoading = false
                } else {
                    error = "Exercise not found"
                    isLoading = false
                }
            } catch (e: Exception) {
                error = "Failed to load exercise: ${e.message}"
                isLoading = false
                println("Error loading exercise detail: ${e.message}")
                e.printStackTrace()
            }
        }
    }

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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (error != null) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(error!!, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = {
                        isLoading = true
                        error = null
                        coroutineScope.launch {
                            try {
                                // Get all exercises with matching name
                                val response = SupabaseClient.client.postgrest
                                    .from("exercises")
                                    .select()
                                    .decodeList<ExerciseDTO>()
                                
                                // Find the one with matching name
                                val exerciseData = response.find { it.name == exerciseName }
                                
                                if (exerciseData != null) {
                                    exercise = Exercise(
                                        id = exerciseData.id,
                                        name = exerciseData.name,
                                        force = exerciseData.force,
                                        level = exerciseData.level ?: "Beginner",
                                        mechanic = exerciseData.mechanic,
                                        equipment = exerciseData.equipment,
                                        primaryMuscles = exerciseData.primary_muscles,
                                        secondaryMuscles = exerciseData.secondary_muscles,
                                        category = exerciseData.category,
                                        instructions = exerciseData.instructions
                                    )
                                    isLoading = false
                                } else {
                                    error = "Exercise not found"
                                    isLoading = false
                                }
                            } catch (e: Exception) {
                                error = "Failed to load exercise: ${e.message}"
                                isLoading = false
                            }
                        }
                    }) {
                        Text("Retry")
                    }
                }
            } else if (exercise != null) {
                val exerciseData = exercise!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Exercise Name
                    Text(
                        text = exerciseData.name,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Force
                    if (exerciseData.force != null) {
                        Text(
                            text = "Force",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(text = exerciseData.force)
                        
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    // Level
                    Text(
                        text = "Level",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(text = exerciseData.level)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Mechanic
                    if (exerciseData.mechanic != null) {
                        Text(
                            text = "Mechanic",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(text = exerciseData.mechanic)
                        
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    // Equipment
                    Text(
                        text = "Equipment",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(text = exerciseData.equipment ?: "No equipment needed")
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Primary Muscles
                    Text(
                        text = "Primary Muscles",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                    exerciseData.primaryMuscles.forEach { muscle ->
                        Text(text = "• $muscle")
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Secondary Muscles
                    if (exerciseData.secondaryMuscles != null && exerciseData.secondaryMuscles.isNotEmpty()) {
                        Text(
                            text = "Secondary Muscles",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                        exerciseData.secondaryMuscles.forEach { muscle ->
                            Text(text = "• $muscle")
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    // Category
                    Text(
                        text = "Category",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(text = exerciseData.category)

                    Spacer(modifier = Modifier.height(24.dp))

                    // Instructions
                    Text(
                        text = "Instructions",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    exerciseData.instructions.forEachIndexed { index, instruction ->
                        Text(
                            text = "${index + 1}. $instruction",
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            } else {
                // Show error when exercise is null but there's no error
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Exercise not found")
                }
            }
        }
    }
} 