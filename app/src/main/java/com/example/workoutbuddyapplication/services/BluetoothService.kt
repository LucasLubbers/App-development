package com.example.workoutbuddyapplication.services

import android.Manifest
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.*

class BluetoothService(private val context: Context) {

    companion object {
        private const val TAG = "BluetoothService"
        private const val SCAN_PERIOD = 20000L
        private val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    }

    private val bluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter = bluetoothManager.adapter
    private val bleScanner = bluetoothAdapter?.bluetoothLeScanner

    private var socket: BluetoothSocket? = null
    private var scanning = false
    private val handler = Handler(Looper.getMainLooper())

    private val _scanningState = MutableStateFlow(false)
    val scanningState: StateFlow<Boolean> = _scanningState.asStateFlow()

    private val _discoveredDevices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val discoveredDevices: StateFlow<List<BluetoothDevice>> = _discoveredDevices.asStateFlow()

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _connectedDevice = MutableStateFlow<BluetoothDevice?>(null)
    val connectedDevice: StateFlow<BluetoothDevice?> = _connectedDevice.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun isBluetoothEnabled(): Boolean {
        return bluetoothAdapter?.isEnabled == true
    }

    fun isDeviceConnected(device: BluetoothDevice): Boolean {
        return _connectedDevice.value?.address == device.address && _isConnected.value
    }

    fun disconnectDevice(device: BluetoothDevice) {
        if (_connectedDevice.value?.address == device.address) {
            disconnect()
        }
    }

    fun startScan() {
        if (!hasRequiredPermissions()) {
            _errorMessage.value = "Missing Bluetooth permissions"
            return
        }

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            _errorMessage.value = "Bluetooth is not enabled"
            return
        }

        _discoveredDevices.value = emptyList()
        _errorMessage.value = null

        startBleScan()
    }

    private fun startBleScan() {
        if (!hasRequiredPermissions()) {
            _errorMessage.value = "Missing permissions for BLE scan"
            return
        }

        if (bleScanner == null || scanning) return

        Log.d(TAG, "Starting BLE scan...")

        handler.postDelayed({
            try {
                if (hasRequiredPermissions()) {
                    scanning = false
                    bleScanner.stopScan(leScanCallback)
                    _scanningState.value = false
                    Log.d(TAG, "Stopping BLE scan after timeout")
                }
            } catch (e: SecurityException) {
                Log.e(TAG, "SecurityException stopping BLE scan", e)
            }
        }, SCAN_PERIOD)

        scanning = true
        _scanningState.value = true

        try {
            val settings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build()

            bleScanner.startScan(null, settings, leScanCallback)
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException starting BLE scan", e)
        }
    }

    fun stopScan() {
        if (hasRequiredPermissions()) {
            try {
                if (scanning) {
                    bleScanner?.stopScan(leScanCallback)
                    scanning = false
                }
            } catch (e: SecurityException) {
                Log.e(TAG, "SecurityException stopping scan", e)
            }
        }
        _scanningState.value = false
    }

    suspend fun connectToDevice(device: BluetoothDevice) {
        stopScan()

        withContext(Dispatchers.IO) {
            if (!hasRequiredPermissions()) {
                _errorMessage.value = "Missing permissions to connect"
                return@withContext
            }

            try {
                val socket = device.createRfcommSocketToServiceRecord(SPP_UUID)
                this@BluetoothService.socket = socket
                socket.connect()
                _isConnected.value = true
                _connectedDevice.value = device
                Log.d(TAG, "Connected to ${device.name}")
            } catch (e: SecurityException) {
                Log.e(TAG, "SecurityException while connecting", e)
                _errorMessage.value = "Permission denied during connection"
            } catch (e: IOException) {
                Log.e(TAG, "Connection failed", e)
                _errorMessage.value = "Connection failed: ${e.message}"
                disconnect()
            }
        }
    }

    fun disconnect() {
        try {
            socket?.close()
        } catch (e: IOException) {
            Log.e(TAG, "Failed to close socket", e)
        } finally {
            socket = null
            _isConnected.value = false
            _connectedDevice.value = null
        }
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
                    ) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
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

    private val leScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            if (!hasRequiredPermissions()) return

            try {
                val device = result.device
                Log.d(TAG, "BLE discovered: ${device.name ?: "Unnamed"} - ${device.address}")

                val current = _discoveredDevices.value
                if (current.none { it.address == device.address }) {
                    _discoveredDevices.value = current + device
                }
            } catch (e: SecurityException) {
                Log.e(TAG, "SecurityException in BLE scan result", e)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            _errorMessage.value = "BLE scan failed with code: $errorCode"
            _scanningState.value = false
        }
    }

    fun cleanup() {
        stopScan()
        disconnect()
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}