package com.example.workoutbuddyapplication.components

import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.workoutbuddyapplication.R
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
            icon = { Icon(Icons.Default.FitnessCenter, contentDescription = stringResource(R.string.dashboard)) },
            label = { Text(stringResource(R.string.dashboard), fontSize = 9.sp) }
        )
        NavigationBarItem(
            selected = selectedTabIndex == 1,
            onClick = {
                onTabSelected(1)
                navController.navigate(Screen.History.route)
            },
            icon = { Icon(Icons.AutoMirrored.Filled.DirectionsRun, contentDescription = stringResource(R.string.history)) },
            label = { Text(stringResource(R.string.history), fontSize = 9.sp) }
        )
        NavigationBarItem(
            selected = selectedTabIndex == 2,
            onClick = {
                onTabSelected(2)
                navController.navigate(Screen.Exercises.route)
            },
            icon = { Icon(Icons.Default.FitnessCenter, contentDescription = stringResource(R.string.exercises)) },
            label = { Text(stringResource(R.string.exercises), fontSize = 9.sp) }
        )
        NavigationBarItem(
            selected = selectedTabIndex == 3,
            onClick = {
                onTabSelected(3)
                navController.navigate(Screen.Stats.route)
            },
            icon = { Icon(Icons.Default.SelfImprovement, contentDescription = stringResource(R.string.statistics)) },
            label = { Text(stringResource(R.string.statistics), fontSize = 9.sp) }
        )
        NavigationBarItem(
            selected = selectedTabIndex == 4,
            onClick = {
                onTabSelected(4)
                navController.navigate(Screen.Profile.route)
            },
            icon = { Icon(Icons.Default.AccountCircle, contentDescription = stringResource(R.string.profile)) },
            label = { Text(stringResource(R.string.profile), fontSize = 9.sp) }
        )
    }
}