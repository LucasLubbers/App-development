package com.example.workoutbuddyapplication.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.workoutbuddyapplication.screens.AddWorkoutScreen
import com.example.workoutbuddyapplication.screens.BluetoothDevicesScreen
import com.example.workoutbuddyapplication.screens.DashboardScreen
import com.example.workoutbuddyapplication.screens.GoalsScreen
import com.example.workoutbuddyapplication.screens.HistoryScreen
import com.example.workoutbuddyapplication.screens.LoginScreen
import com.example.workoutbuddyapplication.screens.RunningWorkoutScreen
import com.example.workoutbuddyapplication.screens.SignupScreen
import com.example.workoutbuddyapplication.screens.StartWorkoutScreen
import com.example.workoutbuddyapplication.screens.StatsScreen
import com.example.workoutbuddyapplication.screens.StrengthWorkoutScreen
import com.example.workoutbuddyapplication.screens.WorkoutCompletedScreen
import com.example.workoutbuddyapplication.screens.YogaWorkoutScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Signup : Screen("signup")
    object Dashboard : Screen("dashboard")
    object AddWorkout : Screen("add_workout")
    object History : Screen("history")
    object Stats : Screen("stats")
    object Goals : Screen("goals")
    object StartWorkout : Screen("start_workout")
    object RunningWorkout : Screen("running_workout")
    object StrengthWorkout : Screen("strength_workout")
    object YogaWorkout : Screen("yoga_workout")
    object WorkoutCompleted : Screen("workout_completed")
    object BluetoothDevices : Screen("bluetooth_devices")
    object QRScanner : Screen("qr_scanner")
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Login.route) {
        composable(Screen.Login.route) {
            LoginScreen(navController = navController)
        }
        composable(Screen.Signup.route) {
            SignupScreen(navController = navController)
        }
        composable(Screen.Dashboard.route) {
            DashboardScreen(navController = navController)
        }
        composable(Screen.AddWorkout.route) {
            AddWorkoutScreen(navController = navController)
        }
        composable(Screen.History.route) {
            HistoryScreen(navController = navController)
        }
        composable(Screen.Stats.route) {
            StatsScreen(navController = navController)
        }
        composable(Screen.Goals.route) {
            GoalsScreen(navController = navController)
        }
        composable(Screen.StartWorkout.route) {
            StartWorkoutScreen(navController = navController)
        }
        composable(Screen.RunningWorkout.route) {
            RunningWorkoutScreen(navController = navController)
        }
        composable(Screen.StrengthWorkout.route) {
            StrengthWorkoutScreen(navController = navController)
        }
        composable(Screen.YogaWorkout.route) {
            YogaWorkoutScreen(navController = navController)
        }
        composable(Screen.WorkoutCompleted.route) {
            WorkoutCompletedScreen(navController = navController)
        }
        composable(Screen.BluetoothDevices.route) {
            BluetoothDevicesScreen(navController = navController)
        }
    }
}
