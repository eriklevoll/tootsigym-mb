package com.example.erik_spectre.tootsigymmb.Controller

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
import android.support.v4.view.GestureDetectorCompat
import android.view.GestureDetector
import android.view.MotionEvent
import com.example.erik_spectre.tootsigymmb.Model.BLE
import com.example.erik_spectre.tootsigymmb.Model.MQTT
import kotlinx.android.synthetic.main.nav_header_main.*
import java.util.*
import kotlin.concurrent.schedule
import com.example.erik_spectre.tootsigymmb.Utilities.*


class MainActivity : AppCompatActivity(), GestureDetector.OnGestureListener,  GestureDetector.OnDoubleTapListener, NavigationView.OnNavigationItemSelectedListener  {

    override fun onDoubleTap(p0: MotionEvent?): Boolean {
        return true
    }

    override fun onDoubleTapEvent(p0: MotionEvent?): Boolean {
        return true
    }

    override fun onSingleTapConfirmed(p0: MotionEvent?): Boolean {
        return true
    }

    override fun onShowPress(p0: MotionEvent?) {
    }

    override fun onSingleTapUp(p0: MotionEvent?): Boolean {
        return true
    }

    override fun onDown(p0: MotionEvent?): Boolean {
        return true
    }

    override fun onFling(p0: MotionEvent?, p1: MotionEvent?, p2: Float, p3: Float): Boolean {
        return true
    }

    override fun onScroll(p0: MotionEvent?, p1: MotionEvent?, p2: Float, p3: Float): Boolean {
        return true
    }

    override fun onLongPress(p0: MotionEvent?) {
    }

    var gDetector: GestureDetectorCompat? = null

    override fun onTouchEvent(event: MotionEvent): Boolean {
        this.gDetector?.onTouchEvent(event)
        return super.onTouchEvent(event)
    }

    private lateinit var bleConnection : BLE
    private lateinit var mqtt: MQTT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        this.gDetector = GestureDetectorCompat(this, this)
        gDetector?.setOnDoubleTapListener(this)

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

        mqtt = MQTT(this, "m20.cloudmqtt.com", "11957", "ayogkqnq", "_e4HiuI73ywB")
        mqtt.init()

//        bleConnection = BLE(this)
//        bleConnection.init()
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_favorite -> {
                mqtt.sendData("40")
//                bleConnection.sendRandomLED()
                true
            }
            else -> super.onOptionsItemSelected(item)
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
                bleConnection.sendData("test")
            }
            R.id.nav_connection -> {
                closeNav = false
//
//                if (bleConnection.getConnectingState() == "Connecting") return true
//
//                bleConnection.setConnectionBar(connectionBar)
//                bleConnection.setConnectionText(item)
//                bleConnection.setConnectionState("Connecting")
//
//
//                if (!bleConnection.connectionActive)
//                    initAdapter()
//                else
//                    bleConnection.disconnect()
            }
        }

        if (closeNav)
            drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun initAdapter() {
        if (!bleConnection.adapterEnabled())
            enableBleAdapter()

        //Wait for adapter to actually turn on
        waitAdapterInit()
    }

    private fun waitAdapterInit() {
        val timer = Timer("schedule", true)

        val bleEnabled = bleConnection.adapterState() == ADAPTER_STATE_ON

        //Check bluetooth permissions
        if (!bleEnabled) {
            timer.schedule(1000) {
                println("Waiting")
                waitAdapterInit()
            }
        } else {
            if (!bleConnection.adapterParametersInitialized)
                bleConnection.initializeAdapterParameters()
            bleConnection.startAdvertising(timeout = 7000)
        }
    }

    private fun enableBleAdapter() {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        startActivityForResult(enableBtIntent, REQUEST_BLUETOOTH)
    }
}
