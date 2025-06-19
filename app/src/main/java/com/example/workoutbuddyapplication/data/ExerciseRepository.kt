package com.example.workoutbuddyapplication.data

import com.example.workoutbuddyapplication.models.Exercise
import com.example.workoutbuddyapplication.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray

object ExerciseRepository {
    suspend fun fetchExerciseByName(name: String): Exercise? = withContext(Dispatchers.IO) {
        val client = OkHttpClient()
        val url =
            "https://attsgwsxdlblbqxnboqx.supabase.co/rest/v1/exercises?name=eq.${name}&select=*"
        val request = Request.Builder()
            .url(url)
            .addHeader("apikey", BuildConfig.SUPABASE_ANON_KEY)
            .addHeader("Authorization", "Bearer ${BuildConfig.SUPABASE_ANON_KEY}")
            .build()
        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: return@withContext null
        val jsonArray = JSONArray(responseBody)
        if (jsonArray.length() == 0) return@withContext null
        val obj = jsonArray.getJSONObject(0)
        Exercise(
            name = obj.getString("name"),
            force = obj.optString("force", ""),
            level = obj.optString("level", ""),
            mechanic = obj.optString("mechanic", ""),
            equipment = obj.optString("equipment", null),
            primaryMuscles = obj.optJSONArray("primary_muscles")
                ?.let { arr -> List(arr.length()) { arr.getString(it) } } ?: emptyList(),
            secondaryMuscles = obj.optJSONArray("secondary_muscles")
                ?.let { arr -> List(arr.length()) { arr.getString(it) } } ?: emptyList(),
            instructions = obj.optJSONArray("instructions")
                ?.let { arr -> List(arr.length()) { arr.getString(it) } } ?: emptyList(),
            category = obj.optString("category", "")
        )
    }
}