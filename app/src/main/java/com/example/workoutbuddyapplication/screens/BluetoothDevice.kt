package com.example.workoutbuddyapplication.screens

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import com.example.workoutbuddyapplication.services.BluetoothService
import kotlinx.coroutines.launch
import androidx.core.net.toUri

@SuppressLint("UseKtx")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BluetoothDevicesScreen(navController: NavController) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Create BluetoothService
    val bluetoothService = remember { BluetoothService(context) }

    // State from BluetoothService
    val scanningState by bluetoothService.scanningState.collectAsState()
    val discoveredDevices by bluetoothService.discoveredDevices.collectAsState()
    val errorMessage by bluetoothService.errorMessage.collectAsState()

    // Local state
    var showBluetoothDisabledDialog by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var permissionsRequested by remember { mutableStateOf(false) }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissionsRequested = true
        val allGranted = permissions.entries.all { it.value }

        if (allGranted) {
            if (bluetoothService.isBluetoothEnabled()) {
                bluetoothService.startScan()
            } else {
                showBluetoothDisabledDialog = true
            }
        } else {
            showPermissionDialog = true
        }
    }

    // Bluetooth enable launcher
    val bluetoothEnableLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (bluetoothService.isBluetoothEnabled()) {
            bluetoothService.startScan()
        } else {
            scope.launch {
                snackbarHostState.showSnackbar("Bluetooth moet ingeschakeld zijn om apparaten te scannen")
            }
        }
    }

    // Handle lifecycle events
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (!permissionsRequested) {
                    requestPermissions(permissionLauncher)
                } else if (bluetoothService.hasRequiredPermissions()) {
                    if (!bluetoothService.isBluetoothEnabled()) {
                        showBluetoothDisabledDialog = true
                    }
                }
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            bluetoothService.cleanup()
        }
    }

    // Show error messages in snackbar
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    // Bluetooth disabled dialog
    if (showBluetoothDisabledDialog) {
        AlertDialog(
            onDismissRequest = { showBluetoothDisabledDialog = false },
            title = { Text("Bluetooth uitgeschakeld") },
            text = { Text("Bluetooth moet ingeschakeld zijn om apparaten te scannen. Wil je Bluetooth inschakelen?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showBluetoothDisabledDialog = false
                        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                        bluetoothEnableLauncher.launch(enableBtIntent)
                    }
                ) {
                    Text("Inschakelen")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showBluetoothDisabledDialog = false }
                ) {
                    Text("Annuleren")
                }
            }
        )
    }

    // Permission dialog
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("Toestemmingen vereist") },
            text = { Text("Bluetooth scan en locatie toestemmingen zijn nodig om apparaten te vinden. Ga naar instellingen om deze toestemmingen te verlenen.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPermissionDialog = false
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = "package:${context.packageName}".toUri()
                        }
                        context.startActivity(intent)
                    }
                ) {
                    Text("Instellingen")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showPermissionDialog = false }
                ) {
                    Text("Annuleren")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bluetooth Apparaten") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Terug")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (bluetoothService.hasRequiredPermissions()) {
                                if (bluetoothService.isBluetoothEnabled()) {
                                    bluetoothService.startScan()
                                } else {
                                    showBluetoothDisabledDialog = true
                                }
                            } else {
                                requestPermissions(permissionLauncher)
                            }
                        }
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Vernieuwen")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (scanningState) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )

                    Spacer(modifier = Modifier.padding(8.dp))

                    Text(
                        text = "Zoeken naar apparaten...",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            Text(
                text = "Beschikbare Apparaten",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (discoveredDevices.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        if (scanningState) {
                            Icon(
                                imageVector = Icons.Default.BluetoothSearching,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.BluetoothDisabled,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = if (scanningState)
                                "Zoeken naar apparaten..."
                            else
                                "Geen apparaten gevonden. Druk op vernieuwen om opnieuw te scannen.",
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    items(discoveredDevices.size) { index ->
                        val deviceInfo = discoveredDevices[index]
                        DeviceCard(
                            deviceInfo = deviceInfo,
                            onConnectClick = {
                                scope.launch {
                                    bluetoothService.connectToDevice(deviceInfo)
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { navController.navigateUp() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Bevestigen")
            }
        }
    }
}

@Composable
fun DeviceCard(
    deviceInfo: BluetoothService.BluetoothDeviceInfo,
    onConnectClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (deviceInfo.isConnected)
                    Icons.Default.BluetoothConnected
                else
                    Icons.Default.Bluetooth,
                contentDescription = "Bluetooth",
                tint = if (deviceInfo.isConnected)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.padding(8.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = deviceInfo.name ?: "Onbekend apparaat",
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = deviceInfo.address,
                    style = MaterialTheme.typography.bodySmall
                )

                // Show device type
                Text(
                    text = when (deviceInfo.deviceType) {
                        BluetoothService.DeviceType.HEART_RATE_MONITOR -> "Hartslagmeter"
                        BluetoothService.DeviceType.SMART_WATCH -> "Smartwatch"
                        BluetoothService.DeviceType.FITNESS_TRACKER -> "Fitness Tracker"
                        BluetoothService.DeviceType.UNKNOWN -> "Onbekend apparaat"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Button(
                onClick = onConnectClick
            ) {
                Text(if (deviceInfo.isConnected) "Verbreken" else "Verbinden")
            }
        }
    }
}

// Helper function to request the necessary permissions
private fun requestPermissions(permissionLauncher: ActivityResultLauncher<Array<String>>) {
    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT
        )
    } else {
        arrayOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    permissionLauncher.launch(permissions)
}
