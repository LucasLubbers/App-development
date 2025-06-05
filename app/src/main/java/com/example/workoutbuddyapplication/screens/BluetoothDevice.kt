package com.example.workoutbuddyapplication.screens

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.workoutbuddyapplication.ui.theme.strings
import kotlinx.coroutines.launch
import androidx.core.net.toUri

@SuppressLint("UseKtx")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BluetoothDeviceScreen(navController: NavController) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val strings = strings()

    val bluetoothService = remember { BluetoothService(context) }

    val scanningState by bluetoothService.scanningState.collectAsState()
    val discoveredDevices by bluetoothService.discoveredDevices.collectAsState()
    val errorMessage by bluetoothService.errorMessage.collectAsState()
    val connectedDevice by bluetoothService.connectedDevice.collectAsState()

    var showBluetoothDisabledDialog by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var permissionsRequested by remember { mutableStateOf(false) }

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

    val bluetoothEnableLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        if (bluetoothService.isBluetoothEnabled()) {
            bluetoothService.startScan()
        } else {
            scope.launch {
                snackbarHostState.showSnackbar(strings.bluetoothRequired)
            }
        }
    }

    LaunchedEffect(Unit) {
        if (!bluetoothService.hasRequiredPermissions()) {
            requestPermissions(permissionLauncher)
        } else if (!bluetoothService.isBluetoothEnabled()) {
            showBluetoothDisabledDialog = true
        } else {
            bluetoothService.startScan()
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (bluetoothService.hasRequiredPermissions()) {
                    if (!bluetoothService.isBluetoothEnabled()) {
                        showBluetoothDisabledDialog = true
                    } else if (discoveredDevices.isEmpty()) {
                        bluetoothService.startScan()
                    }
                } else if (!permissionsRequested) {
                    requestPermissions(permissionLauncher)
                }
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            bluetoothService.cleanup()
        }
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            bluetoothService.clearErrorMessage()
        }
    }

    if (showBluetoothDisabledDialog) {
        AlertDialog(
            onDismissRequest = { showBluetoothDisabledDialog = false },
            title = { Text(strings.bluetoothDisabled) },
            text = { Text(strings.bluetoothDisabledPrompt) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showBluetoothDisabledDialog = false
                        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                        bluetoothEnableLauncher.launch(enableBtIntent)
                    }
                ) { Text(strings.enable) }
            },
            dismissButton = {
                TextButton(onClick = { showBluetoothDisabledDialog = false }) {
                    Text(strings.cancel)
                }
            }
        )
    }

    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text(strings.permissionsRequired) },
            text = { Text(strings.permissionsRequiredPrompt) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPermissionDialog = false
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = "package:${context.packageName}".toUri()
                        }
                        context.startActivity(intent)
                    }
                ) { Text(strings.settings) }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    Text(strings.cancel)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.bluetoothDevices) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = strings.back)
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
                                permissionsRequested = false
                                requestPermissions(permissionLauncher)
                            }
                        }
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = strings.refresh)
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
                        text = strings.searchingDevices,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            Text(
                text = strings.availableDevices,
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
                                imageVector = Icons.Default.SentimentDissatisfied,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = strings.noDevicesFound,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            Text(
                                text = strings.ensureDeviceIsNear,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(discoveredDevices.size) { index ->
                        val device = discoveredDevices[index]
                        DeviceItem(
                            device = device,
                            isConnected = device.address == connectedDevice?.address,
                            onConnect = { device -> 
                                scope.launch {
                                    bluetoothService.connectToDevice(device)
                                }
                            },
                            onDisconnect = { bluetoothService.disconnect() }
                        )
                    }
                }
            }
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun DeviceItem(
    device: BluetoothDevice,
    isConnected: Boolean,
    onConnect: (BluetoothDevice) -> Unit,
    onDisconnect: () -> Unit
) {
    val strings = strings()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isConnected) Icons.Default.BluetoothConnected else Icons.Default.Bluetooth,
                contentDescription = null,
                tint = if (isConnected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = device.name ?: "Unknown Device",
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = device.address,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Button(
                onClick = {
                    if (isConnected) onDisconnect() else onConnect(device)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isConnected) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = when {
                        isConnected -> strings.disconnect
                        else -> strings.connect
                    }.uppercase()
                )
            }
        }
    }
}

private fun requestPermissions(launcher: ActivityResultLauncher<Array<String>>) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        launcher.launch(
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        )
    } else {
        launcher.launch(
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        )
    }
}
