package com.example.workoutbuddyapplication.screens

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.workoutbuddyapplication.R
import com.example.workoutbuddyapplication.components.BottomNavBar
import com.example.workoutbuddyapplication.data.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.first
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.launch
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.text.input.PasswordVisualTransformation
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.storage.storage

suspend fun getUserId(context: Context): String? {
    val prefs = context.dataStore.data.first()
    return prefs[USER_ID_KEY]
}

@Serializable
data class UserProfile(
    val name: String?,
    val email: String,
    @SerialName("picture")
    val pictureUrl: String?,
    val language: String? = "nl", // Default to Dutch
    @SerialName("unit_system")
    val unitSystem: String? = "metric" // Default to metric
)

fun getProfileImageUrl(url: String?, version: Int): String? {
    return url?.let { "$it?v=$version" }
}

suspend fun fetchUserProfile(userId: String): UserProfile? {
    return SupabaseClient.client
        .from("profiles")
        .select(Columns.list("name", "email", "picture", "language", "unit_system")) {
            filter {
                eq("id", userId)
            }
        }
        .decodeList<UserProfile>()
        .firstOrNull()
}

suspend fun updateUserProfile(userId: String, name: String, pictureUrl: String? = null): Boolean {
    val updateMap = mutableMapOf("name" to name)
    if (pictureUrl != null) updateMap["picture"] = pictureUrl
    return try {
        SupabaseClient.client
            .from("profiles")
            .update(updateMap) {
                filter { eq("id", userId) }
            }
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

suspend fun updateUserLanguage(userId: String, language: String): Boolean {
    return try {
        SupabaseClient.client
            .from("profiles")
            .update(mapOf("language" to language)) {
                filter { eq("id", userId) }
            }
        true
    } catch (e: Exception) {
        e.printStackTrace()
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
        e.printStackTrace()
        false
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {
    val context = LocalContext.current
    var selectedTabIndex by remember { mutableStateOf(4) }
    var profile by remember { mutableStateOf<UserProfile?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    var nameInput by remember { mutableStateOf("") }

    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmNewPassword by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()
    var profileSaveMessage by remember { mutableStateOf<String?>(null) }
    var passwordSaveMessage by remember { mutableStateOf<String?>(null) }

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
            uri: Uri? -> imageUri = uri
    }
    var imageVersion by remember { mutableStateOf(0) }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var deletePassword by remember { mutableStateOf("") }
    var deleteAccountMessage by remember { mutableStateOf<String?>(null) }
    var isDeleting by remember { mutableStateOf(false) }
    var showDeleteSuccessScreen by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val userId = getUserId(context)
        if (userId != null) {
            profile = fetchUserProfile(userId)
            nameInput = profile?.name ?: ""
        }
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profiel") },
                actions = {
                    IconButton(onClick = { /* Navigate to settings */ }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        bottomBar = {
            BottomNavBar(
                selectedTabIndex = selectedTabIndex,
                onTabSelected = { selectedTabIndex = it },
                navController = navController
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 8.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            if (isLoading) {
                CircularProgressIndicator()
            } else {
                // Profile Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 220.dp)
                        .padding(vertical = 4.dp),
                    shape = MaterialTheme.shapes.medium,
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(28.dp)
                            .fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.Top
                        ) {
                            val painter = if (profile?.pictureUrl.isNullOrBlank()) {
                                painterResource(id = R.drawable.aktiv_logo)
                            } else {
                                rememberAsyncImagePainter(getProfileImageUrl(profile?.pictureUrl,
                                    imageVersion))
                            }
                            Image(
                                painter = painter,
                                contentDescription = "Profile Picture",
                                modifier = Modifier
                                    .size(110.dp)
                                    .clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.width(24.dp))
                            Column(
                                verticalArrangement = Arrangement.Top,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = profile?.name ?: "",
                                    fontSize = 18.sp,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = profile?.email ?: "",
                                    fontSize = 13.sp,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { imagePicker.launch("image/*") },
                                    modifier = Modifier.fillMaxWidth(0.7f)
                                ) {
                                    Text("Upload Foto", modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Center)
                                }

                                imageUri?.let { uri ->
                                    LaunchedEffect(uri) {
                                        val userId = getUserId(context)
                                        if (userId != null) {
                                            val inputStream = context.contentResolver.
                                            openInputStream(uri)
                                            val bytes = inputStream?.readBytes()
                                            inputStream?.close()
                                            if (bytes != null) {
                                                val fileName = "$userId.jpg"
                                                SupabaseClient.client.storage.from(
                                                    "profile-pictures").upload(fileName,
                                                    bytes, upsert = true)
                                                val publicUrl = SupabaseClient.client.storage.from(
                                                    "profile-pictures").publicUrl(fileName)
                                                updateUserProfile(userId, nameInput, publicUrl)
                                                profile = fetchUserProfile(userId)
                                                imageVersion++
                                                profileSaveMessage = "Profielfoto bijgewerkt"
                                            }
                                        }
                                        imageUri = null
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            OutlinedTextField(
                                value = nameInput,
                                onValueChange = { nameInput = it },
                                label = { Text("Naam") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    val userId = getUserId(context)
                                    if (userId != null) {
                                        val profileSuccess = updateUserProfile(userId, nameInput)
                                        if (profileSuccess) {
                                            profile = fetchUserProfile(userId)
                                            profileSaveMessage = "Profiel bijgewerkt"
                                        } else {
                                            profileSaveMessage = "Bijwerken mislukt"
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Opslaan")
                        }

                        profileSaveMessage?.let {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(it, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Password Change Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = MaterialTheme.shapes.medium,
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(28.dp)
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = "Verander Wachtwoord",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = currentPassword,
                            onValueChange = { currentPassword = it },
                            label = { Text("Huidig Wachtwoord") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = { newPassword = it },
                            label = { Text("Nieuw Wachtwoord") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = confirmNewPassword,
                            onValueChange = { confirmNewPassword = it },
                            label = { Text("Bevestig Nieuw Wachtwoord") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    passwordSaveMessage = null
                                    if (newPassword != confirmNewPassword) {
                                        passwordSaveMessage = "Wachtwoorden komen niet overeen"
                                        return@launch
                                    }
                                    try {
                                        SupabaseClient.client.auth.signInWith(
                                            io.github.jan.supabase.gotrue.providers.builtin.Email
                                        ) {
                                            email = profile?.email ?: ""
                                            password = currentPassword
                                        }
                                        SupabaseClient.client.auth.modifyUser {
                                            password = newPassword
                                        }
                                        passwordSaveMessage = "Wachtwoord succesvol gewijzigd"
                                        currentPassword = ""
                                        newPassword = ""
                                        confirmNewPassword = ""
                                    } catch (e: Exception) {
                                        passwordSaveMessage = "Wachtwoord wijzigen mislukt"
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Bevestigen")
                        }
                        passwordSaveMessage?.let {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(it, color = if (it.contains("succesvol")) MaterialTheme.
                            colorScheme.primary else MaterialTheme.colorScheme.error)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Delete Account Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = MaterialTheme.shapes.medium,
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(28.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Button(
                            onClick = { showDeleteDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Verwijder Account", color = MaterialTheme.
                            colorScheme.onError)
                        }
                        deleteAccountMessage?.let {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(it, color = if (it.contains("succesvol")) MaterialTheme.
                            colorScheme.primary else MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }

    // Delete Account Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                deletePassword = ""
                deleteAccountMessage = null
            },
            title = { Text("Account verwijderen") },
            text = {
                Column {
                    Text("Voer je wachtwoord in om je account te verwijderen.")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = deletePassword,
                        onValueChange = { deletePassword = it },
                        label = { Text("Wachtwoord") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (isDeleting) {
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                    deleteAccountMessage?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            isDeleting = true
                            deleteAccountMessage = null
                            try {
                                SupabaseClient.client.auth.signInWith(
                                    io.github.jan.supabase.gotrue.providers.builtin.Email
                                ) {
                                    email = profile?.email ?: ""
                                    password = deletePassword
                                }
                                showDeleteDialog = false
                                deletePassword = ""
                                showDeleteSuccessScreen = true
                            } catch (e: Exception) {
                                deleteAccountMessage = "Onjuist wachtwoord."
                            }
                            isDeleting = false
                        }
                    },
                    enabled = deletePassword.isNotBlank() && !isDeleting
                ) {
                    Text("Bevestigen")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        deletePassword = ""
                        deleteAccountMessage = null
                    }
                ) {
                    Text("Annuleren")
                }
            }
        )
    }

    if (showDeleteSuccessScreen) {
        AlertDialog(
            onDismissRequest = { showDeleteSuccessScreen = false },
            title = { Text("Account verwijderen") },
            text = { Text("Het verzoek om je account te verwijderen is verstuurd.") },
            confirmButton = {
                TextButton(onClick = { showDeleteSuccessScreen = false }) {
                    Text("OK")
                }
            }
        )
    }
}