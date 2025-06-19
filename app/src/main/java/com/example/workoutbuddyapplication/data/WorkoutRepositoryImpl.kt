package com.example.workoutbuddyapplication.data

import com.example.workoutbuddyapplication.models.Workout
import com.example.workoutbuddyapplication.models.WorkoutExerciseWithDetails
import com.example.workoutbuddyapplication.models.Exercise
import com.example.workoutbuddyapplication.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray

class WorkoutRepositoryImpl : WorkoutRepository {
    override suspend fun getWorkoutById(workoutId: Int): Workout? = fetchWorkoutById(workoutId)
    override suspend fun getExercisesForWorkout(workoutId: Int): List<WorkoutExerciseWithDetails> =
        fetchExercisesForWorkout(workoutId)

    private suspend fun fetchWorkoutById(workoutId: Int): Workout? = withContext(Dispatchers.IO) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://attsgwsxdlblbqxnboqx.supabase.co/rest/v1/workouts?id=eq.$workoutId&select=*")
            .addHeader("apikey", BuildConfig.SUPABASE_ANON_KEY)
            .addHeader("Authorization", "Bearer ${BuildConfig.SUPABASE_ANON_KEY}")
            .build()

        try {
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: return@withContext null

            if (!response.isSuccessful) {
                println("Error fetching workout: HTTP ${response.code}")
                return@withContext null
            }

            val jsonArray = JSONArray(responseBody)
            if (jsonArray.length() == 0) {
                println("No workout found with id: $workoutId")
                return@withContext null
            }

            val obj = jsonArray.getJSONObject(0)
            println("Fetched workout data: $obj")

            Workout(
                id = obj.getInt("id"),
                type = obj.getString("type"),
                date = obj.getString("date"),
                duration = if (obj.get("duration") is String) obj.getString("duration")
                    .toInt() else obj.getInt("duration"),
                distance = if (obj.isNull("distance")) null else {
                    when (val distanceValue = obj.get("distance")) {
                        is String -> distanceValue.toDouble()
                        is Number -> distanceValue.toDouble()
                        else -> null
                    }
                },
                notes = if (obj.isNull("notes")) null else obj.getString("notes"),
                profileId = if (obj.isNull("profile_id")) null else obj.getString("profile_id")
            )
        } catch (e: Exception) {
            println("Error parsing workout data: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    private suspend fun fetchExercisesForWorkout(workoutId: Int): List<WorkoutExerciseWithDetails> =
        withContext(Dispatchers.IO) {
            val client = OkHttpClient()
            val request = Request.Builder()
                .url("https://attsgwsxdlblbqxnboqx.supabase.co/rest/v1/workout_exercises?workout_id=eq.$workoutId&select=*,exercise:exercises(*)")
                .addHeader("apikey", BuildConfig.SUPABASE_ANON_KEY)
                .addHeader("Authorization", "Bearer ${BuildConfig.SUPABASE_ANON_KEY}")
                .build()
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: return@withContext emptyList()
            val jsonArray = JSONArray(responseBody)
            val result = mutableListOf<WorkoutExerciseWithDetails>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val exerciseObj = obj.getJSONObject("exercise")
                val exercise = Exercise(
                    name = exerciseObj.getString("name"),
                    force = exerciseObj.optString("force", ""),
                    level = exerciseObj.optString("level", ""),
                    mechanic = exerciseObj.optString("mechanic", ""),
                    equipment = exerciseObj.optString("equipment", ""),
                    primaryMuscles = exerciseObj.optJSONArray("primary_muscles")?.let { arr ->
                        List(arr.length()) { arr.getString(it) }
                    } ?: emptyList(),
                    secondaryMuscles = exerciseObj.optJSONArray("secondary_muscles")?.let { arr ->
                        List(arr.length()) { arr.getString(it) }
                    } ?: emptyList(),
                    instructions = exerciseObj.optJSONArray("instructions")?.let { arr ->
                        List(arr.length()) { arr.getString(it) }
                    } ?: emptyList(),
                    category = exerciseObj.optString("category", "")
                )
                result.add(
                    WorkoutExerciseWithDetails(
                        exercise = exercise,
                        sets = if (obj.isNull("sets")) null else obj.getInt("sets"),
                        reps = if (obj.isNull("reps")) null else obj.getInt("reps"),
                        weight = if (obj.isNull("weight")) null else obj.getDouble("weight"),
                        notes = if (obj.isNull("notes")) null else obj.getString("notes"),
                        restTime = if (obj.isNull("rest_time")) null else obj.getInt("rest_time")
                    )
                )
            }
            result
        }
}