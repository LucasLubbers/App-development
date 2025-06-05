package com.example.workoutbuddyapplication.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.workoutbuddyapplication.services.NotificationService
import java.util.concurrent.TimeUnit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.example.workoutbuddyapplication.ui.theme.strings
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private val NOTIFICATIONS_ENABLED_KEY = booleanPreferencesKey("notifications_enabled")
private val GOAL_REMINDER_NOTIFICATIONS_ENABLED_KEY = booleanPreferencesKey("goal_reminder_notifications_enabled")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val strings = strings()
    val coroutineScope = rememberCoroutineScope()
    var notificationsEnabled by remember { mutableStateOf(true) }
    var goalReminderEnabled by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val prefs = context.dataStore.data.first()
        val enabled = prefs[NOTIFICATIONS_ENABLED_KEY] ?: true
        val goalReminder = prefs[GOAL_REMINDER_NOTIFICATIONS_ENABLED_KEY] ?: false
        notificationsEnabled = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED && enabled
        } else {
            enabled
        }
        goalReminderEnabled = goalReminder

        if (!prefs.contains(GOAL_REMINDER_NOTIFICATIONS_ENABLED_KEY)) {
            coroutineScope.launch {
                context.dataStore.edit { prefsEdit ->
                    prefsEdit[GOAL_REMINDER_NOTIFICATIONS_ENABLED_KEY] = false
                }
            }
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            notificationsEnabled = isGranted
            coroutineScope.launch {
                context.dataStore.edit { prefs ->
                    prefs[NOTIFICATIONS_ENABLED_KEY] = isGranted
                }
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.notifications) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.back)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Main notification switch
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Enable Notifications",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = { checked ->
                        coroutineScope.launch {
                            context.dataStore.edit { prefs ->
                                prefs[NOTIFICATIONS_ENABLED_KEY] = checked
                            }
                        }
                        if (checked) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }
                        notificationsEnabled = checked
                    }
                )
            }

            // Goal reminder notification switch
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Enable Goal Reminder Notifications",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = goalReminderEnabled,
                    onCheckedChange = { checked ->
                        coroutineScope.launch {
                            context.dataStore.edit { prefs ->
                                prefs[GOAL_REMINDER_NOTIFICATIONS_ENABLED_KEY] = checked
                            }
                        }
                        goalReminderEnabled = checked
                    }
                )
            }

            Button(
                onClick = {
                    NotificationService.createNotificationChannel(context)
                    NotificationService.sendTestNotification(context)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Test Notificatie")
            }
        }
    }
}