package com.example.workoutbuddyapplication.data

import com.example.workoutbuddyapplication.models.UserProfile
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns

suspend fun updateUserLanguage(userId: String, language: String): Boolean {
    return try {
        SupabaseClient.client
            .from("profiles")
            .update(mapOf("language" to language)) {
                filter { eq("id", userId) }
            }
        true
    } catch (e: Exception) {
        false
    }
}

suspend fun updateUserUnitSystem(userId: String, unitSystem: String): Boolean {
    return try {
        SupabaseClient.client
            .from("profiles")
            .update(mapOf("unit_system" to unitSystem)) {
                filter { eq("id", userId) }
            }
        true
    } catch (e: Exception) {
        false
    }
}

suspend fun fetchUserProfile(userId: String): UserProfile? {
    return SupabaseClient.client
        .from("profiles")
        .select(Columns.list("name", "email", "picture", "language", "unit_system")) {
            filter { eq("id", userId) }
        }
        .decodeList<UserProfile>()
        .firstOrNull()
}