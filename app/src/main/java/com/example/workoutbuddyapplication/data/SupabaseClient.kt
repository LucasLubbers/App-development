package com.example.workoutbuddyapplication.data

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.auth
import com.example.workoutbuddyapplication.BuildConfig
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class TestResponse(
    val success: Boolean = false
)

object SupabaseClient {
    val client = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_ANON_KEY
    ) {
        install(Postgrest)
        install(Auth)
    }

    // Test function to check connection
    suspend fun testConnection(): Result<Boolean> = runCatching {
        try {
            // First check if we can connect to the database
            val postgrestResponse = client.postgrest.from("users").select()
            
            // Then check if auth is working
            val authSession = client.auth.currentUserOrNull()
            
            // If we get here, both database and auth are working
            true
        } catch (e: Exception) {
            println("Supabase connection test failed: ${e.message}")
            false
        }
    }
} 