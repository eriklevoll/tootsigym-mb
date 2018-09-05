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
import com.example.erik_spectre.tootsigymmb.Model.BLE
import kotlinx.android.synthetic.main.nav_header_main.*
import java.util.*
import kotlin.concurrent.schedule
import com.example.erik_spectre.tootsigymmb.Utilities.*


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var bleConnection : BLE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

        bleConnection = BLE(this)
        bleConnection.init()
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
                bleConnection.sendRandomLED()
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

                if (bleConnection.getConnectingState() == "Connecting") return true

                bleConnection.setConnectionBar(connectionBar)
                bleConnection.setConnectionText(item)
                bleConnection.setConnectionState("Connecting")


                if (!bleConnection.connectionActive)
                    initAdapter()
                else
                    bleConnection.disconnect()
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
            bleConnection.startAdvertising()
        }
    }

    private fun enableBleAdapter() {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        startActivityForResult(enableBtIntent, REQUEST_BLUETOOTH)
    }
}
