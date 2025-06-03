package com.example.workoutbuddyapplication.components

import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.workoutbuddyapplication.navigation.Screen

@Composable
fun BottomNavBar(
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    navController: NavController
) {
    NavigationBar {
        NavigationBarItem(
            selected = selectedTabIndex == 0,
            onClick = {
                onTabSelected(0)
                navController.navigate(Screen.Dashboard.route)
            },
            icon = { Icon(Icons.Default.FitnessCenter, contentDescription = "Dashboard") },
            label = { Text("Dashboard", fontSize = 9.sp) }
        )
        NavigationBarItem(
            selected = selectedTabIndex == 1,
            onClick = {
                onTabSelected(1)
                navController.navigate(Screen.History.route)
            },
            icon = { Icon(Icons.AutoMirrored.Filled.DirectionsRun, contentDescription = "Geschiedenis") },
            label = { Text("Geschiedenis", fontSize = 9.sp) }
        )
        NavigationBarItem(
            selected = selectedTabIndex == 2,
            onClick = {
                onTabSelected(2)
                navController.navigate(Screen.Exercises.route)
            },
            icon = { Icon(Icons.Default.FitnessCenter, contentDescription = "Oefeningen") },
            label = { Text("Oefeningen", fontSize = 9.sp) }
        )
        NavigationBarItem(
            selected = selectedTabIndex == 3,
            onClick = {
                onTabSelected(3)
                navController.navigate(Screen.Stats.route)
            },
            icon = { Icon(Icons.Default.SelfImprovement, contentDescription = "Statistieken") },
            label = { Text("Statistieken", fontSize = 9.sp) }
        )
        NavigationBarItem(
            selected = selectedTabIndex == 4,
            onClick = {
                onTabSelected(4)
                navController.navigate(Screen.Profile.route)
            },
            icon = { Icon(Icons.Default.AccountCircle, contentDescription = "Profiel") },
            label = { Text("Profiel", fontSize = 9.sp) }
        )
    }
}