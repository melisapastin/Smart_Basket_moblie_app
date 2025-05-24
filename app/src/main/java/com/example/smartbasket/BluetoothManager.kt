package com.example.smartbasket

// BluetoothManager.kt
import android.Manifest
import android.content.Context
import android.util.Log
import android.widget.Toast
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import androidx.annotation.RequiresPermission
import java.io.OutputStream
import java.util.UUID
import android.os.Handler
import android.os.Looper

class BluetoothManager(private val context: Context) {
    private val TAG = "BluetoothManager"
    private val DEVICE_NAME = "ESP32_Servo_Control"
    private val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    private val btAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var btSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun sendSignal(command: Char) {
        if (btAdapter == null) {
            showToast("Bluetooth not supported")
            return
        }

        if (!btAdapter.isEnabled) {
            showToast("Please enable Bluetooth")
            return
        }

        val device: BluetoothDevice? = btAdapter.bondedDevices.find { it.name == DEVICE_NAME }

        if (device == null) {
            showToast("Device not paired: $DEVICE_NAME")
            return
        }

        Thread @androidx.annotation.RequiresPermission(android.Manifest.permission.BLUETOOTH_SCAN) {
            try {
                btSocket = device.createRfcommSocketToServiceRecord(SPP_UUID)
                btAdapter.cancelDiscovery()
                btSocket?.connect()
                outputStream = btSocket?.outputStream

                outputStream?.write(command.code)
                outputStream?.flush()
                // showToast("Signal sent: $command")

                Thread.sleep(300)
                outputStream?.close()
                btSocket?.close()
            } catch (e: Exception) {
                Log.e(TAG, "Bluetooth error: ${e.message}", e)
                showToast("Connection failed: ${e.message}")
            }
        }.start()
    }

    private fun showToast(message: String) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
}