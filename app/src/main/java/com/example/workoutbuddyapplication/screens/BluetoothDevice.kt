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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import com.example.workoutbuddyapplication.R
import com.example.workoutbuddyapplication.services.BluetoothService
import kotlinx.coroutines.launch

@SuppressLint("UseKtx")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BluetoothDeviceScreen(navController: NavController) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

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
                snackbarHostState.showSnackbar(context.getString(R.string.bluetooth_required))
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
            title = { Text(stringResource(R.string.bluetooth_disabled)) },
            text = { Text(stringResource(R.string.bluetooth_disabled_prompt)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showBluetoothDisabledDialog = false
                        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                        bluetoothEnableLauncher.launch(enableBtIntent)
                    }
                ) { Text(stringResource(R.string.enable)) }
            },
            dismissButton = {
                TextButton(onClick = { showBluetoothDisabledDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text(stringResource(R.string.permissions_required)) },
            text = { Text(stringResource(R.string.permissions_required_prompt)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPermissionDialog = false
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = "package:${context.packageName}".toUri()
                        }
                        context.startActivity(intent)
                    }
                ) { Text(stringResource(R.string.settings)) }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.bluetooth_devices)) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
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
                        Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.refresh))
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
                        text = stringResource(R.string.searching_devices),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            Text(
                text = stringResource(R.string.available_devices),
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
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.SentimentDissatisfied,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = stringResource(R.string.no_devices_found),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = stringResource(R.string.ensure_device_is_near),
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    items(discoveredDevices.size) { index ->
                        val device = discoveredDevices[index]
                        DeviceItem(
                            device = device,
                            isConnected = device.address == connectedDevice?.address,
                            onConnect = { 
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
    onConnect: () -> Unit,
    onDisconnect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
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
            if (isConnected) {
                Button(
                    onClick = onDisconnect,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.disconnect))
                }
            } else {
                Button(onClick = onConnect) {
                    Text(stringResource(R.string.connect))
                }
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
