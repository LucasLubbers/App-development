package com.example.workoutbuddyapplication.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.compose.ui.platform.LocalContext
import com.example.workoutbuddyapplication.models.Exercise
import com.example.workoutbuddyapplication.screens.*
import org.json.JSONObject

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
    object Exercises : Screen("exercises")
    object ExerciseDetail : Screen("exercise_detail/{exerciseName}") {
        fun createRoute(exerciseName: String) = "exercise_detail/$exerciseName"
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigation(navController: NavHostController) {
    val context = LocalContext.current
    
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
        composable(Screen.Exercises.route) {
            ExercisesScreen(navController = navController)
        }
        composable(
            route = "workoutDetail/{workoutId}",
            arguments = listOf(navArgument("workoutId") { type = NavType.IntType })
        ) { backStackEntry ->
            val workoutId = backStackEntry.arguments?.getInt("workoutId") ?: return@composable
            WorkoutDetailScreen(navController = navController, workoutId = workoutId)
        }
        composable(
            route = Screen.ExerciseDetail.route,
            arguments = listOf(
                navArgument("exerciseName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val exerciseName = backStackEntry.arguments?.getString("exerciseName") ?: return@composable
            val loadExercise = remember(context) { 
                { name: String ->
                    try {
                        val jsonString = context.assets.open("exercises.json")
                            .bufferedReader()
                            .use { it.readText() }
                        
                        val jsonObject = JSONObject(jsonString)
                        val exercisesArray = jsonObject.getJSONArray("exercises")
                        
                        for (i in 0 until exercisesArray.length()) {
                            val exerciseObj = exercisesArray.getJSONObject(i)
                            if (exerciseObj.getString("name") == name) {
                                val primaryMuscles = mutableListOf<String>()
                                val musclesArray = exerciseObj.getJSONArray("primaryMuscles")
                                for (j in 0 until musclesArray.length()) {
                                    primaryMuscles.add(musclesArray.getString(j))
                                }

                                val instructions = mutableListOf<String>()
                                val instructionsArray = exerciseObj.getJSONArray("instructions")
                                for (j in 0 until instructionsArray.length()) {
                                    instructions.add(instructionsArray.getString(j))
                                }

                                Exercise(
                                    name = exerciseObj.getString("name"),
                                    force = exerciseObj.optString("force", ""),
                                    level = exerciseObj.getString("level"),
                                    mechanic = exerciseObj.optString("mechanic", ""),
                                    equipment = exerciseObj.optString("equipment", ""),
                                    primaryMuscles = primaryMuscles,
                                    secondaryMuscles = if (exerciseObj.has("secondaryMuscles")) {
                                        val arr = exerciseObj.getJSONArray("secondaryMuscles")
                                        List(arr.length()) { arr.getString(it) }
                                    } else emptyList(),
                                    category = exerciseObj.getString("category"),
                                    instructions = instructions
                                )
                            }
                        }
                        null
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }
                }
            }
            
            ExerciseDetailScreen(
                navController = navController,
                exerciseName = exerciseName,
                onLoadExercise = loadExercise
            )
        }
    }
}
