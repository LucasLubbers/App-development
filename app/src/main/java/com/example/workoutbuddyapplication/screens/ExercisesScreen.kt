package com.example.workoutbuddyapplication.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.workoutbuddyapplication.navigation.Screen
import com.example.workoutbuddyapplication.models.Exercise
import com.example.workoutbuddyapplication.data.SupabaseClient
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import io.github.jan.supabase.postgrest.postgrest

@Serializable
data class ExerciseDTO(
    val id: String? = null,
    val name: String,
    val force: String? = null,
    val level: String? = null,
    val mechanic: String? = null,
    val equipment: String? = null,
    val primary_muscles: List<String>,
    val secondary_muscles: List<String>? = null,
    val category: String,
    val instructions: List<String>,
    val created_at: String? = null,
    val updated_at: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ExercisesScreen(navController: NavController) {
    var selectedTabIndex by remember { mutableStateOf(2) }
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedBodyPart by remember { mutableStateOf("Any Body Part") }
    var selectedCategory by remember { mutableStateOf("Any Category") }
    var showBodyPartDialog by remember { mutableStateOf(false) }
    var showCategoryDialog by remember { mutableStateOf(false) }
    
    var exercises by remember { mutableStateOf<List<Exercise>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    
    val coroutineScope = rememberCoroutineScope()
    
    // Fetch exercises from Supabase
    LaunchedEffect(key1 = true) {
        coroutineScope.launch {
            try {
                val exerciseDTOs = SupabaseClient.client.postgrest
                    .from("exercises")
                    .select()
                    .decodeList<ExerciseDTO>()
                
                // Convert DTOs to Exercise model objects
                exercises = exerciseDTOs.map { dto ->
                    Exercise(
                        name = dto.name,
                        level = dto.level ?: "Beginner",
                        equipment = dto.equipment,
                        primaryMuscles = dto.primary_muscles,
                        category = dto.category,
                        instructions = dto.instructions
                    )
                }
                isLoading = false
            } catch (e: Exception) {
                error = "Failed to load exercises: ${e.message}"
                isLoading = false
            }
        }
    }
    
    // Extract unique body parts and categories
    val bodyParts = remember(exercises) {
        exercises.flatMap { it.primaryMuscles }.distinct().sorted()
    }
    
    val categories = remember(exercises) {
        exercises.map { it.category }.distinct().sorted()
    }

    if (showBodyPartDialog) {
        AlertDialog(
            onDismissRequest = { showBodyPartDialog = false },
            title = { Text("Select Body Part") },
            text = {
                LazyColumn {
                    item { 
                        TextButton(
                            onClick = { 
                                selectedBodyPart = "Any Body Part"
                                showBodyPartDialog = false 
                            }
                        ) {
                            Text("Any Body Part")
                        }
                    }
                    items(bodyParts) { bodyPart ->
                        TextButton(
                            onClick = { 
                                selectedBodyPart = bodyPart
                                showBodyPartDialog = false 
                            }
                        ) {
                            Text(bodyPart)
                        }
                    }
                }
            },
            confirmButton = { }
        )
    }

    if (showCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showCategoryDialog = false },
            title = { Text("Select Category") },
            text = {
                LazyColumn {
                    item { 
                        TextButton(
                            onClick = { 
                                selectedCategory = "Any Category"
                                showCategoryDialog = false 
                            }
                        ) {
                            Text("Any Category")
                        }
                    }
                    items(categories) { category ->
                        TextButton(
                            onClick = { 
                                selectedCategory = category
                                showCategoryDialog = false 
                            }
                        ) {
                            Text(category)
                        }
                    }
                }
            },
            confirmButton = { }
        )
    }

    val filteredExercises = remember(searchQuery, selectedBodyPart, selectedCategory, exercises) {
        exercises.filter { exercise ->
            val matchesSearch = if (searchQuery.isBlank()) {
                true
            } else {
                exercise.name.contains(searchQuery, ignoreCase = true) ||
                exercise.category.contains(searchQuery, ignoreCase = true) ||
                exercise.primaryMuscles.any { it.contains(searchQuery, ignoreCase = true) }
            }
            
            val matchesBodyPart = selectedBodyPart == "Any Body Part" || 
                exercise.primaryMuscles.any { it == selectedBodyPart }
            
            val matchesCategory = selectedCategory == "Any Category" || 
                exercise.category == selectedCategory

            matchesSearch && matchesBodyPart && matchesCategory
        }
    }

    Scaffold(
        topBar = {
            Column {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onSearch = { },
                    active = false,
                    onActiveChange = { },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    placeholder = { Text("Search exercises...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    content = { }
                )

                Text(
                    text = "Oefeningen",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledTonalButton(
                        onClick = { showBodyPartDialog = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(selectedBodyPart)
                    }
                    
                    FilledTonalButton(
                        onClick = { showCategoryDialog = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(selectedCategory)
                    }
                }

                Button(
                    onClick = { /* TODO: Handle create exercise */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .height(36.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Create",
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        "Create Exercise",
                        fontSize = 16.sp
                    )
                }
            }
        },
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
                                val exerciseDTOs = SupabaseClient.client.postgrest
                                    .from("exercises")
                                    .select()
                                    .decodeList<ExerciseDTO>()
                                
                                exercises = exerciseDTOs.map { dto ->
                                    Exercise(
                                        name = dto.name,
                                        level = dto.level ?: "Beginner",
                                        equipment = dto.equipment,
                                        primaryMuscles = dto.primary_muscles,
                                        category = dto.category,
                                        instructions = dto.instructions
                                    )
                                }
                                isLoading = false
                            } catch (e: Exception) {
                                error = "Failed to load exercises: ${e.message}"
                                isLoading = false
                            }
                        }
                    }) {
                        Text("Retry")
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    items(filteredExercises) { exercise ->
                        ExerciseCard(
                            exercise = exercise,
                            navController = navController
                        )
                        Divider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ExerciseCard(exercise: Exercise, navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .clickable {
                navController.navigate(Screen.ExerciseDetail.createRoute(exercise.name))
            }
    ) {
        Text(
            text = exercise.name,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
        Text(
            text = exercise.primaryMuscles.firstOrNull() ?: "",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
} 