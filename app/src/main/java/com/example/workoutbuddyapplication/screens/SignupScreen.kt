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

suspend fun registerUser(email: String, password: String, name: String): Boolean = withContext(Dispatchers.IO) {
    val client = OkHttpClient()
    val json = JSONObject()
    json.put("email", email)
    json.put("emailVisibility", true)
    json.put("password", password)
    json.put("passwordConfirm", password)
    json.put("name", name)

    val body = json.toString().toRequestBody("application/json".toMediaType())
    val request = Request.Builder()
        .url("http://192.168.2.10:8090/api/collections/users/records")
        .post(body)
        .build()

    try {
        val response = client.newCall(request).execute()
        val responseBody = response.body?.string()
        if (!response.isSuccessful) {
            println("Signup error: $responseBody")
        }
        response.isSuccessful
    } catch (e: Exception) {
        e.printStackTrace()
        println("Signup exception: ${e.message}")
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