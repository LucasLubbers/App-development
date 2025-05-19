package com.example.workoutbuddyapplication.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.workoutbuddyapplication.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ExercisesScreen(navController: NavController) {
    var selectedTabIndex by remember { mutableStateOf(2) }

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
                text = "Oefeningen",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
} 