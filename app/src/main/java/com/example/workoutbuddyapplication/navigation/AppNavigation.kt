package com.example.workoutbuddyapplication.navigation

import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.compose.ui.platform.LocalContext
import com.example.workoutbuddyapplication.models.Exercise
import com.example.workoutbuddyapplication.screens.*
import com.example.workoutbuddyapplication.ui.theme.UserPreferencesManager
import com.example.workoutbuddyapplication.data.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import org.json.JSONObject
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.jan.supabase.gotrue.SessionStatus

val DEBUG_MODE = false

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Signup : Screen("signup")
    object Dashboard : Screen("dashboard")
    object AddWorkout : Screen("add_workout")
    object History : Screen("history")
    object Stats : Screen("stats")
    object Goals : Screen("goals")
    object Settings : Screen("settings")
    object NotificationSettings : Screen("notification_settings")
    object StartWorkout : Screen("start_workout")
    object RunningWorkout : Screen("running_workout")
    object StrengthWorkout : Screen("strength_workout")
    object CyclingWorkout : Screen("cycling_workout")
    object WorkoutCompleted : Screen("workout_completed/{duration}/{distance}/{calories}/{notes}") {
        fun createRoute(duration: String, distance: String, calories: Int, notes: String) =
            "workout_completed/$duration/$distance/$calories/${Uri.encode(notes)}"
    }
    object StrengthWorkoutCompleted : Screen("strength_workout_completed/{duration}/{notes}") {
        fun createRoute(duration: String, notes: String) =
            "strength_workout_completed/$duration/${Uri.encode(notes)}"
    }
    object BluetoothDevice : Screen("bluetooth_device")
    object QRScanner : Screen("qr_scanner")
    object Exercises : Screen("exercises")
    object ExerciseDetail : Screen("exercise_detail/{exerciseName}") {
        fun createRoute(exerciseName: String) = "exercise_detail/$exerciseName"
    }
    object Profile : Screen("profile")
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigation(navController: NavHostController) {
    val context = LocalContext.current
    val preferencesManager = remember { UserPreferencesManager(context) }
    val selectedLanguage by preferencesManager.selectedLanguage.collectAsState(initial = "nl")
    var startDestination by remember { mutableStateOf<String?>(null) }
    val sessionStatus by SupabaseClient.client.auth.sessionStatus.collectAsState(initial = null)

    LaunchedEffect(sessionStatus) {
        if (sessionStatus is SessionStatus.Authenticated) {
            startDestination = Screen.Dashboard.route
        } else if (sessionStatus is SessionStatus.NotAuthenticated) {
            startDestination = Screen.Login.route
        }
    }

    if (startDestination != null) {
        NavHost(navController = navController, startDestination = startDestination!!) {

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
                HistoryScreen(navController = navController, selectedLanguage = selectedLanguage)
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
            composable(Screen.CyclingWorkout.route) {
                CyclingWorkoutScreen(navController = navController)
            }
            composable(
                route = Screen.StrengthWorkoutCompleted.route,
                arguments = listOf(
                    navArgument("duration") { type = NavType.StringType },
                    navArgument("notes") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val duration = backStackEntry.arguments?.getString("duration") ?: "0"
                val notes = backStackEntry.arguments?.getString("notes")
                WorkoutCompletedScreen(
                    navController = navController,
                    duration = duration,
                    notes = notes
                )
            }
            composable(
                route = Screen.WorkoutCompleted.route,
                arguments = listOf(
                    navArgument("duration") { type = NavType.StringType },
                    navArgument("distance") { type = NavType.StringType },
                    navArgument("calories") { type = NavType.IntType },
                    navArgument("notes") { type = NavType.StringType}
                )
            ) { backStackEntry ->
                val duration = backStackEntry.arguments?.getString("duration") ?: "00:00"
                val distance = backStackEntry.arguments?.getString("distance") ?: "0.00 km"
                val calories = backStackEntry.arguments?.getInt("calories") ?: 0
                val notes = backStackEntry.arguments?.getString("notes")

                WorkoutCompletedScreen(
                    navController = navController,
                    duration = duration,
                    workoutDistance = distance,
                    calories = calories,
                    notes = notes
                )
            }
            composable(Screen.BluetoothDevice.route) {
                BluetoothDeviceScreen(navController = navController)
            }
            composable(Screen.Exercises.route) {
                ExercisesScreen(navController = navController)
            }
            composable(Screen.Profile.route) {
                ProfileScreen(navController = navController)
            }
            composable(Screen.Settings.route) {
                SettingsScreen(navController = navController)
            }
            composable(Screen.NotificationSettings.route) {
                NotificationSettingsScreen(navController = navController)
            }
            composable(
                route = "workoutDetail/{workoutId}/{selectedTabIndex}",
                arguments = listOf(
                    navArgument("workoutId") { type = NavType.IntType },
                    navArgument("selectedTabIndex") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val workoutId = backStackEntry.arguments?.getInt("workoutId") ?: return@composable
                val selectedTabIndex = backStackEntry.arguments?.getInt("selectedTabIndex") ?: 0
                WorkoutDetailScreen(navController = navController, workoutId = workoutId, selectedTabIndex = selectedTabIndex)
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
                )
            }
        }
    }
}