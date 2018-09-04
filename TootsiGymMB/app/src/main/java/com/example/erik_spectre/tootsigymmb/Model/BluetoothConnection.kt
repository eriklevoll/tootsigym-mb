package com.example.erik_spectre.tootsigymmb.Model

import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.graphics.Color
import android.os.ParcelUuid
import android.view.MenuItem
import android.widget.GridLayout
import com.example.erik_spectre.tootsigymmb.Utilities.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import java.util.*
import kotlin.concurrent.schedule


class BLE(private val context: Context) {

    var connectionActive = false

    private var connectionState = "Disconnected"

    lateinit var connectionColorBar: GridLayout
    lateinit var connectionTextView: MenuItem

    lateinit var adapter : BluetoothAdapter
    lateinit var bleManager : BluetoothManager
    lateinit var advertiser: BluetoothLeAdvertiser

    lateinit var mainService : BluetoothGattService
    lateinit var mainChar : BluetoothGattCharacteristic
    lateinit var mainDevice : BluetoothDevice

    private var bluetoothGattServer: BluetoothGattServer? = null


    fun init() {
        bleManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        adapter = bleManager.adapter
        mainService = createGATTService()
    }

    fun setConnectionBar(bar: GridLayout) {
        connectionColorBar = bar
    }

    fun setConnectionText(element: MenuItem) {
        connectionTextView = element
    }

    fun setConnectionState(state: String) {
        connectionState = state
        val stateText: String
        val fromColor: Int
        val toColor: Int

        when (state) {
            "Disconnected" -> {
                if (connectionActive)
                    fromColor = Color.BLUE
                else
                    fromColor = Color.YELLOW
                stateText = "Connect"
                toColor = Color.RED
                connectionActive = false
            }
            "Connecting" -> {
                stateText = "Connecting"
                fromColor = Color.RED
                toColor = Color.YELLOW
            }
            "Connected" -> {
                stateText = "Disconnect"
                fromColor = Color.YELLOW
                toColor = Color.BLUE
                connectionActive = true
            }
            "Disconnecting" -> {
                stateText = "Disconnecting"
                fromColor = Color.BLUE
                toColor = Color.YELLOW
            }
            else -> {
                stateText = "Disconnected"
                fromColor = Color.RED
                toColor = Color.RED
            }
        }
        launch(UI) {
            ColorSwitcher.changeBackground(connectionColorBar, fromColor, toColor)
            connectionTextView.title = stateText
        }
    }

    fun disconnect() {
        bluetoothGattServer?.cancelConnection(mainDevice)
        bluetoothGattServer?.close()
        setConnectionState("Disconnected")
    }

    private val advCallback = object : AdvertiseCallback() {

        override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
            super.onStartSuccess(settingsInEffect)
            println("Bluetooth Advertise success.")

        }

        override fun onStartFailure(errorCode: Int) {
            super.onStartFailure(errorCode)
            when (errorCode) {
                ADVERTISE_FAILED_ALREADY_STARTED -> println("ADVERTISE_FAILED_ALREADY_STARTED")
                ADVERTISE_FAILED_DATA_TOO_LARGE -> println("ADVERTISE_FAILED_DATA_TOO_LARGE")
                ADVERTISE_FAILED_FEATURE_UNSUPPORTED -> println("ADVERTISE_FAILED_FEATURE_UNSUPPORTED")
                ADVERTISE_FAILED_INTERNAL_ERROR -> println("ADVERTISE_FAILED_INTERNAL_ERROR")
                ADVERTISE_FAILED_TOO_MANY_ADVERTISERS -> println("ADVERTISE_FAILED_TOO_MANY_ADVERTISERS")
                else -> println("Advertise failed. Errorcode: $errorCode")
            }
        }
    }

    fun startAdvertising() {
        adapter.name = DEVICE_NAME
        advertiser = adapter.bluetoothLeAdvertiser

        val settings = AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                .setConnectable(true)
                .build()

        val advData = AdvertiseData.Builder()
                .addServiceUuid(ParcelUuid(UUID.fromString(DEVICE_SERVICE_UUID)))
                .build()

        val advScanResponse = AdvertiseData.Builder()
                .setIncludeDeviceName(true)
                .build()

        bluetoothGattServer = bleManager.openGattServer(context, gattServerCallback)
        if (bluetoothGattServer?.services?.contains(mainService)!!) {
            println("on juba olemas")
        } else {
            println("ei ole veel olemas")
        }
        bluetoothGattServer?.addService(mainService)
        advertiser.startAdvertising(settings, advData, advScanResponse, advCallback)
        println("start advertising")
    }

    fun stopAdvertising() {
        advertiser.stopAdvertising(advCallback)
        println("Stopped Advertising")
    }

    private fun createGATTService(): BluetoothGattService {
        val service = BluetoothGattService(UUID.fromString(MOONBOARD_DATA_SERVICE_UUID),
                BluetoothGattService.SERVICE_TYPE_PRIMARY)

        mainChar = BluetoothGattCharacteristic(UUID.fromString(MOONBOARD_DATA_CHAR_UUID),
                BluetoothGattCharacteristic.PROPERTY_NOTIFY or
                        BluetoothGattCharacteristic.PROPERTY_WRITE or
                        BluetoothGattCharacteristic.PROPERTY_READ,
                BluetoothGattCharacteristic.PERMISSION_READ or
                        BluetoothGattCharacteristic.PROPERTY_WRITE or
                        BluetoothGattCharacteristic.PROPERTY_NOTIFY)

        val dscrpt = BluetoothGattDescriptor(UUID.fromString(MOONBOARD_DATA_DESCRIPTOR_UUID),
                BluetoothGattDescriptor.PERMISSION_READ or BluetoothGattDescriptor.PERMISSION_WRITE)

        mainChar.addDescriptor(dscrpt)

        mainChar.value = "hello".toByteArray()

        service.addCharacteristic(mainChar)
        return service
    }

    private val gattServerCallback = object : BluetoothGattServerCallback() {

        override fun onConnectionStateChange(device: BluetoothDevice, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                println("BluetoothDevice CONNECTED: $device")
                mainDevice = device
                setConnectionState("Connected")
                stopAdvertising()

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                println("BluetoothDevice DISCONNECTED: $device")
                setConnectionState("Disconnected")
                disconnect()
            }
        }

        override fun onCharacteristicReadRequest(device: BluetoothDevice, requestId: Int, offset: Int,
                                                 characteristic: BluetoothGattCharacteristic) {
            when (characteristic.uuid.toString()){
                MOONBOARD_DATA_CHAR_UUID -> {
                    println("MOONBOARD_DATA_CHAR_UUID READ")
                    val resp = String(characteristic.value)
                    println(resp)
                    val chrValue = characteristic.value
                    bluetoothGattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, chrValue)
                }
                else -> {
                    // Invalid characteristic
                    println("Invalid Characteristic Read: ${characteristic.uuid}")
                    bluetoothGattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_FAILURE, 0, null)
                }
            }
        }

        override fun onCharacteristicWriteRequest(device: BluetoothDevice?, requestId: Int,
                                                  characteristic: BluetoothGattCharacteristic?, preparedWrite: Boolean, responseNeeded: Boolean, offset: Int, value: ByteArray?) {
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value)
            println("MOONBOARD_DATA_CHAR_UUID WRITE")
            val res = if (value == null) "None" else String(value)
            println(res)
            characteristic?.value = value
            bluetoothGattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, value)
        }
    }

    fun sendData(data:String) {
        mainChar.value = data.toByteArray()
        bluetoothGattServer?.notifyCharacteristicChanged(mainDevice, mainChar, false)
    }

    fun adapterEnabled() : Boolean {
        return adapter.isEnabled
    }

    fun adapterState() : Int {
        return adapter.state
    }
}