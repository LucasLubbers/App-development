package com.example.workoutbuddyapplication.data

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.auth
import com.example.workoutbuddyapplication.BuildConfig
import kotlinx.serialization.Serializable
import io.github.jan.supabase.storage.Storage


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
        install(Storage)
    }

    suspend fun testConnection(): Result<Boolean> = runCatching {
        try {
            val postgrestResponse = client.postgrest.from("users").select()
            
            // Then check if auth is working
            val authSession = client.auth.currentUserOrNull()
            
            true
        } catch (e: Exception) {
            println("Supabase connection test failed: ${e.message}")
            false
        }
    }
} 