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

suspend fun registerUser(email: String, password: String, name: String): Boolean = withContext(Dispatchers.IO) {
    val client = OkHttpClient()
    val json = JSONObject()
    json.put("email", email)
    json.put("password", password)
    // Optionally, add user metadata
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
        if (!response.isSuccessful) {
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
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Aktiv",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "Account Aanmaken",
            fontSize = 24.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Naam") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Wachtwoord") },
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
                        navController.navigate(Screen.Dashboard.route)
                    } else {
                        errorMessage = "Registratie mislukt"
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Registreren")
        }

        errorMessage?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(onClick = { navController.navigate(Screen.Login.route) }) {
            Text("Al een account? Log hier in")
        }
    }
}