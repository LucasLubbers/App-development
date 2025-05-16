package com.example.workoutbuddyapplication.services

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.ParcelUuid
import android.util.Log
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.UUID

class BluetoothService(private val context: Context) {

    companion object {
        private const val TAG = "BluetoothService"
        private const val SCAN_PERIOD: Long = 10000 // 10 seconds

        // Standard UUIDs for common services
        val HEART_RATE_SERVICE_UUID = UUID.fromString("0000180D-0000-1000-8000-00805F9B34FB")
        val DEVICE_INFO_SERVICE_UUID = UUID.fromString("0000180A-0000-1000-8000-00805F9B34FB")
        val BATTERY_SERVICE_UUID = UUID.fromString("0000180F-0000-1000-8000-00805F9B34FB")
        val APPLE_WATCH_SERVICE_UUID = UUID.fromString("9FA480E0-4967-4542-9390-D343DC5D04AE")
    }

    private val bluetoothManager: BluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
    private var bluetoothLeScanner: BluetoothLeScanner? = null

    private var socket: BluetoothSocket? = null
    private var scanning = false
    private val handler = Handler(Looper.getMainLooper())

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _heartRate = MutableStateFlow(0)
    val heartRate: StateFlow<Int> = _heartRate.asStateFlow()

    private val _deviceName = MutableStateFlow<String?>(null)
    val deviceName: StateFlow<String?> = _deviceName.asStateFlow()

    private val _discoveredDevices = MutableStateFlow<List<BluetoothDeviceInfo>>(emptyList())
    val discoveredDevices: StateFlow<List<BluetoothDeviceInfo>> = _discoveredDevices.asStateFlow()

    private val _scanningState = MutableStateFlow(false)
    val scanningState: StateFlow<Boolean> = _scanningState.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    data class BluetoothDeviceInfo(
        val device: BluetoothDevice,
        val name: String,
        val address: String,
        val rssi: Int = 0,
        val isConnected: Boolean = false,
        val deviceType: DeviceType = DeviceType.UNKNOWN
    )

    enum class DeviceType {
        HEART_RATE_MONITOR,
        SMART_WATCH,
        FITNESS_TRACKER,
        UNKNOWN
    }

    init {
        if (bluetoothAdapter != null) {
            bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
            registerBluetoothReceiver()
        } else {
            _errorMessage.value = "Bluetooth is niet beschikbaar op dit apparaat"
        }
    }

    private val bluetoothReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    }

                    device?.let {
                        val deviceName = if (ActivityCompat.checkSelfPermission(
                                context,
                                Manifest.permission.BLUETOOTH_CONNECT
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            it.name ?: "Onbekend apparaat"
                        } else {
                            "Onbekend apparaat (Geen toestemming)"
                        }

                        val rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE).toInt()

                        val deviceInfo = BluetoothDeviceInfo(
                            device = it,
                            name = deviceName,
                            address = it.address,
                            rssi = rssi,
                            deviceType = determineDeviceType(deviceName)
                        )

                        val currentDevices = _discoveredDevices.value.toMutableList()
                        if (!currentDevices.any { d -> d.address == deviceInfo.address }) {
                            currentDevices.add(deviceInfo)
                            _discoveredDevices.value = currentDevices
                        }
                    }
                }
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    _scanningState.value = true
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    _scanningState.value = false
                }
                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                    if (state == BluetoothAdapter.STATE_OFF) {
                        _errorMessage.value = "Bluetooth is uitgeschakeld"
                        stopScan()
                    }
                }
            }
        }
    }

    private fun registerBluetoothReceiver() {
        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
            addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        }
        context.registerReceiver(bluetoothReceiver, filter)
    }

    private fun unregisterBluetoothReceiver() {
        try {
            context.unregisterReceiver(bluetoothReceiver)
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Receiver not registered", e)
        }
    }

    private fun determineDeviceType(deviceName: String): DeviceType {
        return when {
            deviceName.contains("watch", ignoreCase = true) ||
                    deviceName.contains("apple", ignoreCase = true) -> DeviceType.SMART_WATCH
            deviceName.contains("hr", ignoreCase = true) ||
                    deviceName.contains("heart", ignoreCase = true) ||
                    deviceName.contains("polar", ignoreCase = true) ||
                    deviceName.contains("h10", ignoreCase = true) -> DeviceType.HEART_RATE_MONITOR
            deviceName.contains("fitbit", ignoreCase = true) ||
                    deviceName.contains("garmin", ignoreCase = true) ||
                    deviceName.contains("band", ignoreCase = true) -> DeviceType.FITNESS_TRACKER
            else -> DeviceType.UNKNOWN
        }
    }

    // BLE scanning callback
    private val leScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            val device = result.device

            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                _errorMessage.value = "Bluetooth Connect toestemming ontbreekt"
                return
            }

            val deviceName = device.name ?: "Onbekend BLE apparaat"
            val deviceInfo = BluetoothDeviceInfo(
                device = device,
                name = deviceName,
                address = device.address,
                rssi = result.rssi,
                deviceType = determineDeviceType(deviceName)
            )

            val currentDevices = _discoveredDevices.value.toMutableList()
            if (!currentDevices.any { it.address == deviceInfo.address }) {
                currentDevices.add(deviceInfo)
                _discoveredDevices.value = currentDevices
            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            _scanningState.value = false
            _errorMessage.value = "BLE scan mislukt met foutcode: $errorCode"
            Log.e(TAG, "BLE Scan failed with error code: $errorCode")
        }
    }

    fun startScan() {
        if (!hasRequiredPermissions()) {
            _errorMessage.value = "Bluetooth scan toestemmingen ontbreken"
            return
        }

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            _errorMessage.value = "Bluetooth is niet ingeschakeld"
            return
        }

        _discoveredDevices.value = emptyList()
        _errorMessage.value = null

        // Start both classic Bluetooth and BLE scanning
        startClassicScan()
        startBleScan()
    }

    private fun startClassicScan() {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        // Cancel any ongoing discovery
        if (bluetoothAdapter?.isDiscovering == true) {
            bluetoothAdapter?.cancelDiscovery()
        }

        // Start new discovery
        bluetoothAdapter?.startDiscovery()
    }

    private fun startBleScan() {
        if (bluetoothLeScanner == null) {
            Log.e(TAG, "BLE Scanner is not available")
            return
        }

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        if (!scanning) {
            // Stop scanning after a predefined scan period
            handler.postDelayed({
                scanning = false
                bluetoothLeScanner?.stopScan(leScanCallback)
                _scanningState.value = false
            }, SCAN_PERIOD)

            scanning = true
            _scanningState.value = true

            // Setup scan filters for specific device types
            val filters = mutableListOf<ScanFilter>()

            // Add filter for heart rate service
            filters.add(
                ScanFilter.Builder()
                    .setServiceUuid(ParcelUuid(HEART_RATE_SERVICE_UUID))
                    .build()
            )

            // Add filter for Apple Watch (if known UUID)
            filters.add(
                ScanFilter.Builder()
                    .setServiceUuid(ParcelUuid(APPLE_WATCH_SERVICE_UUID))
                    .build()
            )

            // Scan settings
            val settings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build()

            bluetoothLeScanner?.startScan(filters, settings, leScanCallback)
        }
    }

    fun stopScan() {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        // Stop classic Bluetooth discovery
        if (bluetoothAdapter?.isDiscovering == true) {
            bluetoothAdapter?.cancelDiscovery()
        }

        // Stop BLE scanning
        if (scanning) {
            scanning = false
            bluetoothLeScanner?.stopScan(leScanCallback)
        }

        _scanningState.value = false
    }

    suspend fun scanForDevices(): List<BluetoothDevice> {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            return emptyList()
        }

        return try {
            bluetoothAdapter.bondedDevices.toList()
        } catch (e: SecurityException) {
            // Handle permission issues
            emptyList()
        }
    }

    suspend fun connectToDevice(deviceInfo: BluetoothDeviceInfo) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            _errorMessage.value = "Bluetooth Connect toestemming ontbreekt"
            return
        }

        // Stop scanning before connecting
        stopScan()

        withContext(Dispatchers.IO) {
            try {
                val device = deviceInfo.device

                // For BLE devices, we would use a different approach with GATT
                // For classic Bluetooth, we use the socket approach

                // Try to connect using the appropriate UUID based on device type
                val uuid = when (deviceInfo.deviceType) {
                    DeviceType.HEART_RATE_MONITOR -> HEART_RATE_SERVICE_UUID
                    DeviceType.SMART_WATCH -> if (deviceInfo.name.contains("apple", ignoreCase = true)) {
                        APPLE_WATCH_SERVICE_UUID
                    } else {
                        // Generic UUID for serial port profile
                        UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
                    }
                    else -> UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
                }

                socket = device.createRfcommSocketToServiceRecord(uuid)
                socket?.connect()

                _isConnected.value = socket?.isConnected == true
                _deviceName.value = deviceInfo.name

                // Update the device list to show connected status
                val updatedDevices = _discoveredDevices.value.map {
                    if (it.address == deviceInfo.address) {
                        it.copy(isConnected = true)
                    } else {
                        it
                    }
                }
                _discoveredDevices.value = updatedDevices

                if (_isConnected.value) {
                    startListening()
                }
            } catch (e: SecurityException) {
                Log.e(TAG, "Security exception during connection", e)
                _errorMessage.value = "Beveiligingsfout: ${e.localizedMessage}"
                disconnect()
            } catch (e: IOException) {
                Log.e(TAG, "IO exception during connection", e)
                _errorMessage.value = "Verbindingsfout: ${e.localizedMessage}"
                disconnect()
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error during connection", e)
                _errorMessage.value = "Onverwachte fout: ${e.localizedMessage}"
                disconnect()
            }
        }
    }

    suspend fun connectToDevice(device: BluetoothDevice) {
        withContext(Dispatchers.IO) {
            try {
                // Standard SPP UUID
                val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
                socket = device.createRfcommSocketToServiceRecord(uuid)
                socket?.connect()

                _isConnected.value = socket?.isConnected == true
                _deviceName.value = device.name

                if (_isConnected.value) {
                    startListening()
                }
            } catch (e: SecurityException) {
                // Handle permission issues
            } catch (e: IOException) {
                // Handle connection issues
                disconnect()
            }
        }
    }

    private suspend fun startListening() {
        withContext(Dispatchers.IO) {
            val inputStream = socket?.inputStream
            val buffer = ByteArray(1024)

            while (_isConnected.value) {
                try {
                    val bytes = inputStream?.read(buffer) ?: -1
                    if (bytes > 0) {
                        // Process the received data
                        // This is a simplified example - actual data parsing
                        // would depend on the specific device protocol
                        val data = String(buffer, 0, bytes)
                        Log.d(TAG, "Received data: $data")

                        if (data.contains("HR:")) {
                            val hrValue = data.substringAfter("HR:").substringBefore("\n").trim().toIntOrNull()
                            if (hrValue != null) {
                                _heartRate.value = hrValue
                            }
                        }
                    }
                } catch (e: IOException) {
                    Log.e(TAG, "Error reading from device", e)
                    break
                }
            }
        }
    }

    fun disconnect() {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        try {
            socket?.close()
        } catch (e: IOException) {
            Log.e(TAG, "Error closing socket", e)
        } finally {
            socket = null
            _isConnected.value = false

            // Update the device list to show disconnected status
            val currentDeviceName = _deviceName.value
            _deviceName.value = null

            if (currentDeviceName != null) {
                val updatedDevices = _discoveredDevices.value.map {
                    if (it.name == currentDeviceName) {
                        it.copy(isConnected = false)
                    } else {
                        it
                    }
                }
                _discoveredDevices.value = updatedDevices
            }
        }
    }

    fun isBluetoothEnabled(): Boolean {
        return bluetoothAdapter?.isEnabled == true
    }

    fun hasRequiredPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) == PackageManager.PERMISSION_GRANTED
        } else {
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH
            ) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.BLUETOOTH_ADMIN
                    ) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun cleanup() {
        stopScan()
        disconnect()
        unregisterBluetoothReceiver()
    }
}
