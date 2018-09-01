package com.example.erik_spectre.tootsigymmb.Model

import android.bluetooth.*
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import com.beepiz.bluetooth.gattcoroutines.experimental.GattConnection
import com.example.erik_spectre.tootsigymmb.Controller.MainActivity
import android.bluetooth.le.ScanSettings
import android.graphics.Color
import android.view.MenuItem
import android.widget.GridLayout
import com.beepiz.blegattcoroutines.experimental.genericaccess.GenericAccess
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.*
import com.example.erik_spectre.tootsigymmb.Utilities.*


class BLE(private val context: Context) {

    var connectionActive = false

    lateinit var connectionColorBar: GridLayout
    lateinit var connectionTextView: MenuItem

    lateinit var adapter : BluetoothAdapter
    lateinit var bleManager : BluetoothManager

    lateinit var mainService : BluetoothGattService
    lateinit var mainChar : BluetoothGattCharacteristic
    lateinit var mainDevice : GattConnection

    private var operationAttempt: Job? = null


    fun Init() {
        bleManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        adapter = bleManager.adapter
    }

    fun setConnectionBar(bar: GridLayout) {
        connectionColorBar = bar
    }

    fun setConnectionText(element: MenuItem) {
        connectionTextView = element
    }

    fun setConnectionState(state: String) {
        when (state) {
            "Disconnected" -> {
                if (connectionActive)
                    ColorSwitcher.changeBackground(connectionColorBar, Color.BLUE, Color.RED)
                else
                    ColorSwitcher.changeBackground(connectionColorBar, Color.YELLOW, Color.RED)
                connectionTextView.title = "Connect"
                connectionActive = false
            }
            "Connecting" -> {
                ColorSwitcher.changeBackground(connectionColorBar, Color.RED, Color.YELLOW)
                connectionTextView.title = "Connecting"
            }
            "Connected" -> {
                ColorSwitcher.changeBackground(connectionColorBar, Color.YELLOW, Color.BLUE)
                connectionTextView.title = "Disconnect"
                connectionActive = true
            }
            "Disconnecting" -> {
                ColorSwitcher.changeBackground(connectionColorBar, Color.BLUE, Color.YELLOW)
                connectionTextView.title = "Disconnecting"
            }
        }
    }

    fun sendData(data:String) {
        operationAttempt = launch(UI) {
            mainDevice.connect()
            mainChar.setValue(data)
            mainDevice.writeCharacteristic(mainChar)
        }
        println("sending: $data")
    }

    fun adapterEnabled() : Boolean {
        return adapter.isEnabled
    }

    fun adapterState() : Int {
        return adapter.state
    }

    fun startScan() {
        val scanSettings = ScanSettings.Builder().apply {
            val scanMode = ScanSettings.SCAN_MODE_LOW_LATENCY
            setScanMode(scanMode)
        }.build()
        bleManager.adapter.bluetoothLeScanner?.startScan(null, scanSettings, scanCallback)

        connectToDevice()
    }

    fun disconnect() {
        operationAttempt = launch(UI) {
            mainDevice.close()
        }
        setConnectionState("Disconnected")
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanFailed(errorCode: Int) {
            println("Scan failed. Errorcode: $errorCode")
        }

        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result);
            println("I found a ble device ${result?.device?.address}, ${result?.device?.name}")
            if (result?.device?.address == MOONBOARD_MAC) {
                //Device found
            }
        }
    }

    suspend inline fun BluetoothDevice.useBasic(timeout: Long, block:GattBasicUsage) {
        val deviceConnection = GattConnection(this)
        try {
            withTimeout(timeout) {
                deviceConnection.connect()
            }
            println("connected!")
            val services = deviceConnection.discoverServices()
            println("Services discovered!")
            block(deviceConnection, services)
        } catch (e: TimeoutCancellationException) {
            println("Connection timed out after $timeout milliseconds!")
            setConnectionState("Disconnected")
            throw e
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            println("Exception: $e")
        } finally {
            //deviceConnection.close()
            //println("Closed!")
        }
    }

    fun connectToDevice(connectionTimeOut: Long = 5000L) {
        operationAttempt?.cancel()
        operationAttempt = launch(UI) {
            bleManager.adapter.getRemoteDevice(MOONBOARD_MAC).useBasic(connectionTimeOut) { device, services ->
                services.forEach {
                    println("Service found with UUID: ${it.uuid}")
                    if (it.uuid.toString() == MOONBOARD_DATA_SERVICE_UUID) {
                        mainService = it
                        println("Yes!")
                        it.characteristics.forEach {
                            println("Char found with UUID: ${it.uuid}")
                            if (it.uuid.toString() == MOONBOARD_DATA_CHAR_UUID) {
                                println("Double Yes!")
                                mainChar = it
                                connectionActive = true
                                //ColorSwitcher.changeBackground(connectionColorBar, Color.YELLOW, Color.BLUE)
                                setConnectionState("Connected")
                                println("Found Moonboard")
                            }
                        }
                        println("Main service found!")
                    }
                }
                with(GenericAccess) {
                    device.readAppearance()
                    println("Device appearance: ${device.appearance}")
                    device.readDeviceName()
                    println("Device name: ${device.deviceName}")
                    mainDevice = device
                    with(mainDevice) {
                        connect()
                        writeCharacteristic(characteristic = mainChar)
                    }
                }
            }
            operationAttempt = null
        }
    }
}