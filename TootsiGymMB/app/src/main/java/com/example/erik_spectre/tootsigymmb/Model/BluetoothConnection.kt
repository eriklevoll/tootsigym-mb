package com.example.erik_spectre.tootsigymmb.Model

import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.graphics.Color
import android.os.ParcelUuid
import android.view.MenuItem
import android.widget.GridLayout
import com.example.erik_spectre.tootsigymmb.Utilities.*
import java.util.*


class BLE(private val context: Context) {

    var connectionActive = false

    lateinit var connectionColorBar: GridLayout
    lateinit var connectionTextView: MenuItem

    lateinit var adapter : BluetoothAdapter
    lateinit var bleManager : BluetoothManager

    lateinit var mainChar : BluetoothGattCharacteristic
    lateinit var mainDevice : BluetoothDevice

    private var bluetoothGattServer: BluetoothGattServer? = null


    fun Init() {
        bleManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        adapter = bleManager.adapter
        adapter.name = DEVICE_NAME
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

    fun startAdvertising() {
        val advertiser = adapter.bluetoothLeAdvertiser

        val settings = AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                .setConnectable(true)
                .build()

        val advData = AdvertiseData.Builder()
                .addServiceUuid(ParcelUuid(UUID.fromString(DEVICE_SERVICE_UUID)))
                .build()

        val advScanResponse = AdvertiseData.Builder()
                .setIncludeDeviceName(true)
                .build()

        val advCallback = object : AdvertiseCallback() {

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

        bluetoothGattServer = bleManager.openGattServer(context, gattServerCallback)
        bluetoothGattServer?.addService(createGATTService())
        advertiser.startAdvertising(settings, advData, advScanResponse, advCallback)
        println("start advertising")
    }

    fun stopAdvertising() {
        val adapter = BluetoothAdapter.getDefaultAdapter()
        val advertiser = adapter.bluetoothLeAdvertiser

        val advCallback = object : AdvertiseCallback() {}
        advertiser.stopAdvertising(advCallback)
    }

    fun createGATTService(): BluetoothGattService {
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
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                println("BluetoothDevice DISCONNECTED: $device")
                //Remove device from any active subscriptions
                //registeredDevices.remove(device)
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