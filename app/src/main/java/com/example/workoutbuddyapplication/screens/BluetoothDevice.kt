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
                snackbarHostState.showSnackbar("Bluetooth moet ingeschakeld zijn om apparaten te scannen")
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
            title = { Text("Bluetooth uitgeschakeld") },
            text = { Text("Bluetooth moet ingeschakeld zijn om apparaten te scannen. Wil je Bluetooth inschakelen?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showBluetoothDisabledDialog = false
                        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                        bluetoothEnableLauncher.launch(enableBtIntent)
                    }
                ) { Text("Inschakelen") }
            },
            dismissButton = {
                TextButton(onClick = { showBluetoothDisabledDialog = false }) {
                    Text("Annuleren")
                }
            }
        )
    }

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
                ) { Text("Instellingen") }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
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
                                permissionsRequested = false
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
                        val device = discoveredDevices[index]
                        val isConnected = bluetoothService.isDeviceConnected(device)
                        DeviceCard(
                            device = device,
                            isConnected = isConnected,
                            onConnectClick = {
                                scope.launch {
                                    if (isConnected) {
                                        bluetoothService.disconnectDevice(device)
                                    } else {
                                        bluetoothService.connectToDevice(device)
                                    }
                                }
                            },
                            bluetoothService = bluetoothService,
                            context = context
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
    device: BluetoothDevice,
    isConnected: Boolean,
    onConnectClick: () -> Unit,
    bluetoothService: BluetoothService,
    context: Context
) {
    val deviceName = remember(device) {
        if (bluetoothService.hasRequiredPermissions()) {
            try {
                device.name ?: "Onbekend apparaat"
            } catch (e: SecurityException) {
                "Toestemming vereist"
            }
        } else {
            "Toestemming vereist"
        }
    }

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
                imageVector = if (isConnected) Icons.Default.BluetoothConnected else Icons.Default.Bluetooth,
                contentDescription = "Bluetooth",
                tint = if (isConnected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.padding(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = deviceName,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = device.address,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Button(
                onClick = {
                    if (bluetoothService.hasRequiredPermissions()) {
                        onConnectClick()
                    }
                }
            ) {
                Text(if (isConnected) "Verbreken" else "Verbinden")
            }
        }
    }
}

private fun requestPermissions(permissionLauncher: ActivityResultLauncher<Array<String>>) {
    val permissions = mutableListOf<String>()

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        permissions.add(Manifest.permission.BLUETOOTH_SCAN)
        permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
    } else {
        permissions.add(Manifest.permission.BLUETOOTH)
        permissions.add(Manifest.permission.BLUETOOTH_ADMIN)
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    permissionLauncher.launch(permissions.toTypedArray())
}
