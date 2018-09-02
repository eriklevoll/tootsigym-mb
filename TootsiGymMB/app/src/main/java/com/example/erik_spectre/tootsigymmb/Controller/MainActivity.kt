package com.example.erik_spectre.tootsigymmb.Controller

import android.Manifest
import android.bluetooth.*
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.example.erik_spectre.tootsigymmb.R
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import android.content.Intent
import android.os.Build
import android.support.v4.content.PermissionChecker
import com.example.erik_spectre.tootsigymmb.Model.BLE
import kotlinx.android.synthetic.main.nav_header_main.*
import java.util.*
import kotlin.concurrent.schedule
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.content.Context
import android.os.ParcelUuid
import android.util.Log
import com.example.erik_spectre.tootsigymmb.Utilities.*


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    lateinit var BleConnection : BLE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
//            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                    .setAction("Action", null).show()
            sendRandomLED()
        }

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

        BleConnection = BLE(this)
        BleConnection.Init()

    }

    private fun startAdvertising() {
        val bleManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val adapter = bleManager.adapter
        val advertiser = adapter.bluetoothLeAdvertiser

        adapter.name = DEVICE_NAME

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

        advertiser.startAdvertising(settings, advData, advScanResponse, advCallback)
        val server = bleManager.openGattServer(this, gattServerCallback)
        server.addService(createGATTService())
        println("start advertising")
    }

    fun createGATTService(): BluetoothGattService {
        val service = BluetoothGattService(UUID.fromString(MOONBOARD_DATA_SERVICE_UUID),
                BluetoothGattService.SERVICE_TYPE_PRIMARY)

        val chr = BluetoothGattCharacteristic(UUID.fromString(MOONBOARD_DATA_CHAR_UUID),
                BluetoothGattCharacteristic.PROPERTY_NOTIFY or
                        BluetoothGattCharacteristic.PROPERTY_WRITE or
                BluetoothGattCharacteristic.PROPERTY_READ,
                BluetoothGattCharacteristic.PERMISSION_READ or
                BluetoothGattCharacteristic.PROPERTY_WRITE or
                BluetoothGattCharacteristic.PROPERTY_NOTIFY)
        service.addCharacteristic(chr)
        return service
    }

    fun stopAdvertising() {
        val adapter = BluetoothAdapter.getDefaultAdapter()
        val advertiser = adapter.bluetoothLeAdvertiser

        val advCallback = object : AdvertiseCallback() {}
        advertiser.stopAdvertising(advCallback)
    }

    fun startBleGATTServer() {
        val bleManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothGattServer = bleManager.openGattServer(this, gattServerCallback)
    }

    /**
     * Callback to handle incoming requests to the GATT server.
     * All read/write requests for characteristics and descriptors are handled here.
     */

    private var bluetoothGattServer: BluetoothGattServer? = null

    private val gattServerCallback = object : BluetoothGattServerCallback() {

        override fun onConnectionStateChange(device: BluetoothDevice, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                println("BluetoothDevice CONNECTED: $device")
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                println("BluetoothDevice DISCONNECTED: $device")
                //Remove device from any active subscriptions
                //registeredDevices.remove(device)
            }
        }

        override fun onCharacteristicReadRequest(device: BluetoothDevice, requestId: Int, offset: Int,
                                                 characteristic: BluetoothGattCharacteristic) {
            val now = System.currentTimeMillis()
            when (characteristic.uuid.toString()){
                "345ue89sduh3784235" -> {
                    println("Read Char")
                    bluetoothGattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, byteArrayOf(1,2,3))
                }
                else -> {
                    // Invalid characteristic
                    println("Invalid Characteristic Read: ${characteristic.uuid}")
                    bluetoothGattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_FAILURE, 0, null)
                }
            }
        }

        override fun onDescriptorReadRequest(device: BluetoothDevice, requestId: Int, offset: Int,
                                             descriptor: BluetoothGattDescriptor) {
            if (descriptor.uuid.toString() == "something") {
                println("Config descriptor read")
                BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                //BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
                bluetoothGattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, byteArrayOf(1,2,3))
            } else {
                println("Unknown descriptor read request")
                bluetoothGattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_FAILURE, 0, null)
            }
        }

        override fun onDescriptorWriteRequest(device: BluetoothDevice, requestId: Int,
                                              descriptor: BluetoothGattDescriptor,
                                              preparedWrite: Boolean, responseNeeded: Boolean,
                                              offset: Int, value: ByteArray) {
            if (descriptor.uuid.toString() == "something") {
                if (Arrays.equals(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE, value)) {
                    println("Subscribe device to notifications: $device")
                } else if (Arrays.equals(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE, value)) {
                    println("Unsubscribe device from notifications: $device")
                }

                if (responseNeeded) {
                    bluetoothGattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null)
                }
            } else {
                println("Unknown descriptor write request")
                if (responseNeeded) {
                    bluetoothGattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_FAILURE, 0, null)
                }
            }
        }
    }

    fun ClosedRange<Int>.random() = Random().nextInt((endInclusive + 1) - start) +  start

    fun sendRandomLED() {
        var r = (0..255).random().toString()
        var g = (0..255).random().toString()
        var b = (0..255).random().toString()

        if (BleConnection.connectionActive)
            BleConnection.sendData("-1,$r,$g,$b")
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_settings -> return true
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        var closeNav = true
        when (item.itemId) {
            R.id.nav_grid -> {

            }
            R.id.nav_canvas -> {

            }
            R.id.nav_database -> {

            }
            R.id.nav_connection -> {
                closeNav = false

                BleConnection.setConnectionBar(connectionBar)
                BleConnection.setConnectionText(item)
                BleConnection.setConnectionState("Connecting")

                //Check bluetooth adapter state and start scan when ready
                if (!BleConnection.connectionActive) {
                    InitAdapter()
                } else {
                    BleConnection.disconnect()
                }
            }
        }

        if (closeNav)
            drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    fun InitAdapter(checkCounter: Int = 0) {
        if (!BleConnection.adapterEnabled()) {
            enableBleAdapter()
        }
        val perm = PermissionChecker.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION)
        if (perm == PermissionChecker.PERMISSION_GRANTED) {
            println("Location permission granted")
        } else requestEnableLocation()

        //Wait for adapter to actually turn on
        waitAdapterInit()
    }

    fun waitAdapterInit()
    {
        val timer = Timer("schedule", true)

        val bleEnabled = BleConnection.adapterState() == ADAPTER_STATE_ON
        val locationEnabled = PermissionChecker.
                checkSelfPermission(applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION) == PermissionChecker.PERMISSION_GRANTED

        if (!bleEnabled || locationEnabled) {
            timer.schedule(1000) {
                println("Waiting")
                waitAdapterInit()
            }
        } else {
            //BleConnection.startScan()
            startAdvertising()
        }
    }

    fun enableBleAdapter() {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        startActivityForResult(enableBtIntent, REQUEST_BLUETOOTH)
    }

    fun requestEnableLocation() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                REQUEST_COARSE_LOCATION)
    } else {
        println("SDK < 23")
    }
}
