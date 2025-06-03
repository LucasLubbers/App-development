package com.example.workoutbuddyapplication.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.workoutbuddyapplication.navigation.Screen
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import com.example.workoutbuddyapplication.BuildConfig
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.example.workoutbuddyapplication.R
import com.example.workoutbuddyapplication.data.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import androidx.compose.ui.platform.LocalContext
import com.example.workoutbuddyapplication.ui.theme.strings
import com.example.workoutbuddyapplication.ui.theme.dutchStrings

suspend fun registerUser(email: String, password: String, name: String): Boolean = withContext(Dispatchers.IO) {
    val client = OkHttpClient()
    val json = JSONObject()
    json.put("email", email)
    json.put("password", password)
    val userMeta = JSONObject()
    userMeta.put("name", name)
    json.put("data", userMeta)

    val body = json.toString().toRequestBody("application/json".toMediaType())
    val request = Request.Builder()
        .url("https://attsgwsxdlblbqxnboqx.supabase.co/auth/v1/signup")
        .post(body)
        .addHeader("apikey", BuildConfig.SUPABASE_ANON_KEY)
        .addHeader("Authorization", "Bearer ${BuildConfig.SUPABASE_ANON_KEY}")
        .build()

    try {
        val response = client.newCall(request).execute()
        val responseBody = response.body?.string()
        if (response.isSuccessful) {
            // Update the profile with name, language, and unit preferences
            val updateJson = JSONObject()
            updateJson.put("name", name)
            updateJson.put("language", "nl") // Default to Dutch
            updateJson.put("unit_system", "metric") // Default to metric
            val updateBody = updateJson.toString().toRequestBody("application/json".toMediaType())
            val updateRequest = Request.Builder()
                .url("https://attsgwsxdlblbqxnboqx.supabase.co/rest/v1/profiles?email=eq.$email")
                .patch(updateBody)
                .addHeader("apikey", BuildConfig.SUPABASE_ANON_KEY)
                .addHeader("Authorization", "Bearer ${BuildConfig.SUPABASE_ANON_KEY}")
                .addHeader("Content-Type", "application/json")
                .build()
            val updateResponse = client.newCall(updateRequest).execute()
            if (!updateResponse.isSuccessful) {
                println("Failed to update profile: ${updateResponse.body?.string()}")
            }
        } else {
            println("Supabase signup error: $responseBody")
        }
        response.isSuccessful
    } catch (e: Exception) {
        e.printStackTrace()
        println("Supabase signup exception: ${e.message}")
        false
    }
}

@Composable
fun SignupScreen(navController: NavController) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val strings = strings()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.aktiv_logo),
            contentDescription = "App Icon",
            modifier = Modifier.size(140.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = strings.appName,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = strings.createAccount,
            fontSize = 24.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(strings.name) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(strings.email) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(strings.password) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Bevestig Wachtwoord") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (password != confirmPassword) {
                    errorMessage = "Wachtwoorden komen niet overeen"
                    return@Button
                }
                coroutineScope.launch {
                    val success = registerUser(email, password, name)
                    if (success) {
                        try {
                            SupabaseClient.client.auth.signInWith(Email) {
                                this.email = email
                                this.password = password
                            }
                            val user = SupabaseClient.client.auth.currentUserOrNull()
                            user?.id?.let { userId ->
                                saveUserId(context, userId)
                            }
                            navController.navigate(Screen.Dashboard.route)
                        } catch (e: Exception) {
                            errorMessage = "Automatisch inloggen mislukt: ${e.message}"
                        }
                    } else {
                        errorMessage = "Registratie mislukt"
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.height(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text(strings.createAccount)
            }
        }

        errorMessage?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(
            onClick = { navController.navigate(Screen.Login.route) },
            enabled = !isLoading
        ) {
            Text(if (strings === dutchStrings) "Al een account? Log hier in" else "Already have an account? Login here")
        }
    }
}