package com.example.workoutbuddyapplication.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.workoutbuddyapplication.R
import com.example.workoutbuddyapplication.navigation.Screen
import com.example.workoutbuddyapplication.data.SupabaseClient
import com.example.workoutbuddyapplication.ui.theme.ThemeManager
import com.example.workoutbuddyapplication.ui.theme.UserPreferencesManager
import com.example.workoutbuddyapplication.util.LocaleHelper
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showLogoutDialog by remember { mutableStateOf(false) }
    
    val themeManager = remember { ThemeManager(context) }
    val preferencesManager = remember { UserPreferencesManager(context) }
    val isDarkMode by themeManager.isDarkMode.collectAsState(initial = false)
    val selectedLanguage by preferencesManager.selectedLanguage.collectAsState(initial = "nl")
    val selectedUnitSystem by preferencesManager.selectedUnitSystem.collectAsState(initial = "metric")
    val debugMode by preferencesManager.debugMode.collectAsState(initial = false)

    // Logout confirmation dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(stringResource(R.string.logout)) },
            text = { Text(stringResource(R.string.logout_confirm)) },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            try {
                                SupabaseClient.client.auth.signOut()
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            } catch (e: Exception) {
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        }
                        showLogoutDialog = false
                    }
                ) {
                    Text(stringResource(R.string.logout))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLogoutDialog = false }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                SettingsItem(
                    title = stringResource(R.string.edit_profile),
                    subtitle = stringResource(R.string.edit_profile_subtitle),
                    icon = Icons.Default.Person,
                    onClick = { navController.navigate(Screen.Profile.route) }
                )
            }

            item {
                SettingsItem(
                    title = stringResource(R.string.notifications),
                    subtitle = stringResource(R.string.notifications_subtitle),
                    icon = Icons.Default.Notifications,
                    onClick = { /* Navigate to notifications settings */ }
                )
            }

            item {
                var showLanguageDialog by remember { mutableStateOf(false) }

                if (showLanguageDialog) {
                    AlertDialog(
                        onDismissRequest = { showLanguageDialog = false },
                        title = { Text(stringResource(R.string.choose_language)) },
                        text = {
                            Column {
                                ListItem(
                                    headlineContent = { Text("Nederlands") },
                                    leadingContent = {
                                        RadioButton(
                                            selected = selectedLanguage == "nl",
                                            onClick = {
                                                coroutineScope.launch {
                                                    preferencesManager.setLanguage("nl")
                                                    val updatedContext = LocaleHelper.setLocale(context, "nl")
                                                    LocaleHelper.getActivity(context)?.recreate()
                                                }
                                                showLanguageDialog = false
                                            }
                                        )
                                    }
                                )
                                ListItem(
                                    headlineContent = { Text("English") },
                                    leadingContent = {
                                        RadioButton(
                                            selected = selectedLanguage == "en",
                                            onClick = {
                                                coroutineScope.launch {
                                                    preferencesManager.setLanguage("en")
                                                    val updatedContext = LocaleHelper.setLocale(context, "en")
                                                    LocaleHelper.getActivity(context)?.recreate()
                                                }
                                                showLanguageDialog = false
                                            }
                                        )
                                    }
                                )
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { showLanguageDialog = false }) {
                                Text(stringResource(R.string.close))
                            }
                        }
                    )
                }

                SettingsItem(
                    title = stringResource(R.string.language),
                    subtitle = if (selectedLanguage == "nl") "Nederlands" else "English",
                    icon = Icons.Default.Language,
                    onClick = { showLanguageDialog = true }
                )
            }

            item {
                var showUnitsDialog by remember { mutableStateOf(false) }

                if (showUnitsDialog) {
                    AlertDialog(
                        onDismissRequest = { showUnitsDialog = false },
                        title = { Text(stringResource(R.string.units)) },
                        text = {
                            Column {
                                ListItem(
                                    headlineContent = { Text(stringResource(R.string.metric)) },
                                    leadingContent = {
                                        RadioButton(
                                            selected = selectedUnitSystem == "metric",
                                            onClick = {
                                                coroutineScope.launch {
                                                    preferencesManager.setUnitSystem("metric")
                                                }
                                                showUnitsDialog = false
                                            }
                                        )
                                    }
                                )
                                ListItem(
                                    headlineContent = { Text(stringResource(R.string.imperial)) },
                                    leadingContent = {
                                        RadioButton(
                                            selected = selectedUnitSystem == "imperial",
                                            onClick = {
                                                coroutineScope.launch {
                                                    preferencesManager.setUnitSystem("imperial")
                                                }
                                                showUnitsDialog = false
                                            }
                                        )
                                    }
                                )
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { showUnitsDialog = false }) {
                                Text(stringResource(R.string.close))
                            }
                        }
                    )
                }

                SettingsItem(
                    title = stringResource(R.string.units),
                    subtitle = if (selectedUnitSystem == "imperial") stringResource(R.string.imperial) else stringResource(R.string.metric),
                    icon = Icons.Default.Straighten,
                    onClick = { showUnitsDialog = true }
                )
            }

            item {
                // Dark Mode Toggle
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                            contentDescription = stringResource(R.string.dark_mode),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.dark_mode),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = if (isDarkMode) stringResource(R.string.dark_mode_enabled) else stringResource(R.string.light_mode_enabled),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isDarkMode,
                            onCheckedChange = { newValue ->
                                coroutineScope.launch {
                                    themeManager.setDarkMode(newValue)
                                }
                            }
                        )
                    }
                }
            }

            item {
                // Debug Mode Toggle
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.BugReport,
                            contentDescription = stringResource(R.string.debug_mode),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.debug_mode),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = if (debugMode) "Mock GPS data voor testen" else "Gebruik echte GPS data",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = debugMode,
                            onCheckedChange = { newValue ->
                                coroutineScope.launch {
                                    preferencesManager.setDebugMode(newValue)
                                }
                            }
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
                
                // Logout button
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = stringResource(R.string.logout),
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.logout),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = stringResource(R.string.logout_subtitle),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                            )
                        }
                        IconButton(
                            onClick = { showLogoutDialog = true }
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = stringResource(R.string.logout),
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Open",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
} 