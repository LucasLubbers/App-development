package com.example.workoutbuddyapplication.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.workoutbuddyapplication.R
import com.example.workoutbuddyapplication.components.BottomNavBar
import com.example.workoutbuddyapplication.navigation.Screen
import com.example.workoutbuddyapplication.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: ProfileViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return ProfileViewModel(context) as T
            }
        }
    )

    val profile by viewModel.profile.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val nameInput by viewModel.nameInput.collectAsState()
    val weightInput by viewModel.weightInput.collectAsState()
    val weightSaveMessage by viewModel.weightSaveMessage.collectAsState()
    val profileSaveMessage by viewModel.profileSaveMessage.collectAsState()
    val imageUri by viewModel.imageUri.collectAsState()
    val imageVersion by viewModel.imageVersion.collectAsState()
    val currentPassword by viewModel.currentPassword.collectAsState()
    val newPassword by viewModel.newPassword.collectAsState()
    val confirmNewPassword by viewModel.confirmNewPassword.collectAsState()
    val passwordSaveMessage by viewModel.passwordSaveMessage.collectAsState()
    val showDeleteDialog by viewModel.showDeleteDialog.collectAsState()
    val deletePassword by viewModel.deletePassword.collectAsState()
    val deleteAccountMessage by viewModel.deleteAccountMessage.collectAsState()
    val isDeleting by viewModel.isDeleting.collectAsState()
    val showDeleteSuccessScreen by viewModel.showDeleteSuccessScreen.collectAsState()

    var selectedTabIndex by remember { mutableStateOf(4) }

    val imagePicker =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            viewModel.setImageUri(uri)
        }

    var userId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        userId = getUserId(context)
        userId?.let { viewModel.loadProfile(it) }
    }

    LaunchedEffect(Unit) {
        val userId = getUserId(context)
        if (userId != null) {
            viewModel.loadProfile(userId)
        }
    }

    LaunchedEffect(imageUri) {
        val userId = getUserId(context)
        if (userId != null && imageUri != null) {
            viewModel.uploadProfileImage(userId, imageUri)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profiel") },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.Settings.route) }) {
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
                                rememberAsyncImagePainter(
                                    viewModel.getProfileImageUrl(
                                        profile?.pictureUrl,
                                        imageVersion
                                    )
                                )
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
                                    Text(
                                        "Upload Foto",
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Center
                                    )
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
                                onValueChange = { viewModel.setNameInput(it) },
                                label = { Text("Naam") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                userId?.let { viewModel.saveProfile(it) }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Opslaan")
                        }
                        profileSaveMessage?.let {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(it, color = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            OutlinedTextField(
                                value = weightInput,
                                onValueChange = { viewModel.setWeightInput(it) },
                                label = { Text("Gewicht (kg)") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.saveWeight() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Opslaan")
                        }
                        weightSaveMessage?.let {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(it, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

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
                            onValueChange = { viewModel.setCurrentPassword(it) },
                            label = { Text("Huidig Wachtwoord") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = { viewModel.setNewPassword(it) },
                            label = { Text("Nieuw Wachtwoord") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = confirmNewPassword,
                            onValueChange = { viewModel.setConfirmNewPassword(it) },
                            label = { Text("Bevestig Nieuw Wachtwoord") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                profile?.email?.let { email ->
                                    viewModel.changePassword(email)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Bevestigen")
                        }
                        passwordSaveMessage?.let {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                it,
                                color = if (it.contains("succesvol")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

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
                            onClick = { viewModel.setShowDeleteDialog(true) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Verwijder Account", color = MaterialTheme.colorScheme.onError)
                        }
                        deleteAccountMessage?.let {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                it,
                                color = if (it.contains("succesvol")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = {
                viewModel.setShowDeleteDialog(false)
                viewModel.setDeletePassword("")
            },
            title = { Text("Account verwijderen") },
            text = {
                Column {
                    Text("Voer je wachtwoord in om je account te verwijderen.")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = deletePassword,
                        onValueChange = { viewModel.setDeletePassword(it) },
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
                        profile?.email?.let { email ->
                            viewModel.deleteAccount(email)
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
                        viewModel.setShowDeleteDialog(false)
                        viewModel.setDeletePassword("")
                    }
                ) {
                    Text("Annuleren")
                }
            }
        )
    }

    if (showDeleteSuccessScreen) {
        AlertDialog(
            onDismissRequest = { viewModel.setShowDeleteSuccessScreen(false) },
            title = { Text("Account verwijderen") },
            text = { Text("Het verzoek om je account te verwijderen is verstuurd.") },
            confirmButton = {
                TextButton(onClick = { viewModel.setShowDeleteSuccessScreen(false) }) {
                    Text("OK")
                }
            }
        )
    }
}