package com.tlab.erik_spectre.tootsigymmb.Controller

import android.graphics.*
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.util.DisplayMetrics
import android.view.GestureDetector
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import com.example.erik_spectre.tootsigymmb.R
import com.example.erik_spectre.tootsigymmb.R.drawable.*
import com.tlab.erik_spectre.tootsigymmb.Model.MQTT
import com.tlab.erik_spectre.tootsigymmb.Utilities.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener  {

    private lateinit var mqtt: MQTT
    private lateinit var gestureDetector: GestureDetector

    private var ledColor = BLUE_COLOR
    private var actionBarMenu: Menu? = null

    private val gestureListener =  object : GestureDetector.SimpleOnGestureListener() {

        val contentCoordinates = IntArray(2)

        override fun onSingleTapUp(e: MotionEvent?): Boolean {
            content_layout.getLocationInWindow(contentCoordinates)
            val rc = GestureParser.onDown(e?.rawX, e?.rawY)
            if (rc == "") return super.onSingleTapUp(e)

            val drawerOpen = drawer_layout.isDrawerOpen(GravityCompat.START)
            if (drawerOpen) return super.onSingleTapUp(e)

            mqtt.sendData("$rc,$ledColor")
            CanvasData.addHold(rc, ledColor)

            return super.onSingleTapUp(e)
        }

        override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
            val data = RandomGenerator.getRandomLED()

            val x1 = e1?.x ?: 0f
            val x2 = e2?.x ?: 0f
            val y1 = e1?.y ?: 0f
            val y2 = e2?.y ?: 0f

            val screen = GestureParser.getScreenDimensions()
            val height = screen[0]
            val width = screen[1]

            val deltaX = Math.abs(x2 - x1) / width
            val deltaY = Math.abs(y2-y1) / height

            if (deltaY > 0.2 || deltaX < 0.15)
                mqtt.sendData("-1,$data")
            else
                drawer_layout.openDrawer(GravityCompat.START)

            return super.onFling(e1, e2, velocityX, velocityY)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        gestureDetector = GestureDetector(this, gestureListener)
        drawer_layout.setOnTouchListener { v, event ->
            gestureDetector.onTouchEvent(event)
        }

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)


        HoldsCanvas.setCanvasImage(canvasImage)

        mqtt = MQTT(this, "m20.cloudmqtt.com", "11957", "ayogkqnq", "_e4HiuI73ywB")
        mqtt.init()

        //Set content dimensions after content layout has loaded
        content_layout.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            val contentCoordinates = IntArray(2)
            mainImage.getLocationOnScreen(contentCoordinates)

            val displayMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(displayMetrics)
            GestureParser.setScreenDimensions(displayMetrics.heightPixels, displayMetrics.widthPixels, contentCoordinates[1])

            println("screen. height: ${displayMetrics.heightPixels}, width: ${displayMetrics.widthPixels}")
            HoldsCanvas.init(GestureParser.getScreenDimensions())
        }
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
        this.actionBarMenu = menu
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_favorite -> {
                val index = RandomGenerator.getRandomHoldRC()
                val data = RandomGenerator.getRandomLED()
                HoldsCanvas.clear()
                mqtt.sendData("-1,0,0,0")
                true
            }
            R.id.action_red -> {
                ledColor = RED_COLOR
                resetTopBarColors()
                item.setIcon(ic_menu_red_toggled)
                true
            }
            R.id.action_blue -> {
                ledColor = BLUE_COLOR
                resetTopBarColors()
                item.setIcon(ic_menu_blue_toggled)
                true
            }
            R.id.action_green -> {
                ledColor = GREEN_COLOR
                resetTopBarColors()
                item.setIcon(ic_menu_green_toggled)
                true
            }
            R.id.action_blank -> {
                ledColor = NO_COLOR
                resetTopBarColors()
                item.setIcon(ic_menu_blank_toggled)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun resetTopBarColors() {
        actionBarMenu?.getItem(0)?.setIcon(ic_menu_blank)
        actionBarMenu?.getItem(1)?.setIcon(ic_menu_green)
        actionBarMenu?.getItem(2)?.setIcon(ic_menu_blue)
        actionBarMenu?.getItem(3)?.setIcon(ic_menu_red)
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
        }

        if (closeNav)
            drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }
}
