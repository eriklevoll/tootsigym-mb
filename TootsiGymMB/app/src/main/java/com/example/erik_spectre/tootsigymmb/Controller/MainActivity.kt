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
                BleConnection.sendData("test")
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
                    //BleConnection.disconnect()
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
            BleConnection.startAdvertising()
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
