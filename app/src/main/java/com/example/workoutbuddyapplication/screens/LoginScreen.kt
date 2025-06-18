package com.example.workoutbuddyapplication.screens

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.workoutbuddyapplication.R
import com.example.workoutbuddyapplication.data.SupabaseClient
import com.example.workoutbuddyapplication.navigation.Screen
import com.example.workoutbuddyapplication.ui.theme.strings
import com.example.workoutbuddyapplication.ui.theme.dutchStrings
import com.example.workoutbuddyapplication.util.EmailValidator
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

val Context.dataStore by preferencesDataStore(name = "user_prefs")
val USER_ID_KEY = stringPreferencesKey("user_id")

suspend fun saveUserId(context: Context, userId: String) {
    context.dataStore.edit { prefs ->
        prefs[USER_ID_KEY] = userId
    }
}

suspend fun getUserId(context: Context): String? {
    val prefs = context.dataStore.data.first()
    return prefs[USER_ID_KEY]
}

@Composable
fun LoginScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val strings = strings()
    val emailValidator = remember { EmailValidator() }

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
            text = strings.login,
            fontSize = 24.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                errorMessage = null
            },
            label = { Text(strings.email) },
            modifier = Modifier.fillMaxWidth(),
            isError = errorMessage != null
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                errorMessage = null
            },
            label = { Text(strings.password) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            isError = errorMessage != null
        )

        errorMessage?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (email.isBlank() || password.isBlank()) {
                    errorMessage = if (strings === dutchStrings) "Vul alle velden in" else "Fill in all fields"
                } else if (!emailValidator.isValid(email)) {
                    errorMessage = if (strings === dutchStrings) "Ongeldig e-mailadres" else "Invalid email address"
                } else {
                    isLoading = true
                    errorMessage = null

                    coroutineScope.launch {
                        try {
                            // Authenticate with Supabase
                            SupabaseClient.client.auth.signInWith(Email) {
                                this.email = email
                                this.password = password
                            }

                            // Get user ID and save it
                            val user = SupabaseClient.client.auth.currentUserOrNull()
                            user?.id?.let { userId ->
                                saveUserId(context, userId)
                            }

                            navController.navigate(Screen.Dashboard.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        } catch (e: Exception) {
                            errorMessage = when {
                                e.message?.contains("invalid login credentials", ignoreCase = true) == true ->
                                    if (strings === dutchStrings) "Ongeldige inloggegevens" else "Invalid login credentials"
                                e.message?.contains("network", ignoreCase = true) == true ->
                                    if (strings === dutchStrings) "Netwerkfout. Controleer je internetverbinding." else "Network error. Check your internet connection."
                                else -> if (strings === dutchStrings) "Fout bij inloggen: ${e.message}" else "Login error: ${e.message}"
                            }
                        } finally {
                            isLoading = false
                        }
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
                Text(strings.login)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(
            onClick = { navController.navigate(Screen.Signup.route) },
            enabled = !isLoading
        ) {
            Text(if (strings === dutchStrings) "Nog geen account? Registreer hier" else "Don't have an account? Register here")
        }
    }
}
