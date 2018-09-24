package com.tlab.erik_spectre.tootsigymmb.Controller

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.util.DisplayMetrics
import android.view.*
import com.example.erik_spectre.tootsigymmb.R
import com.example.erik_spectre.tootsigymmb.R.drawable.*
import com.tlab.erik_spectre.tootsigymmb.Model.MQTT
import com.tlab.erik_spectre.tootsigymmb.Utilities.GestureParser
import com.tlab.erik_spectre.tootsigymmb.Utilities.RandomGenerator
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*


class HoldsCanvas (context: Context): View(context) {

    private var screenHeight    = 0
    private var screenWidth     = 0
    private var topBarHeight    = 0

    override fun onDraw (canvas: Canvas) {
        println("Top bar Canvas: $topBarHeight")
        canvas.drawARGB (0,255, 0, 255)
        val width = width
        val hieght = height
        val brush1 = Paint ()
        brush1.setARGB (255, 255, 0, 0)
        brush1.style = Paint.Style.STROKE
        for (f in 0..9)
            canvas.drawCircle ((width / 2) .toFloat (), (hieght / 2) .toFloat (), (f * 15) .toFloat (), brush1)
        canvas.drawCircle(100f, 214.5f-topBarHeight, 2.5f, brush1)
    }

    fun updateDimensions(screenDims: IntArray) {
        screenHeight    = screenDims[0]
        screenWidth     = screenDims[1]
        topBarHeight    = screenDims[2]
    }
}


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener  {
    private lateinit var mqtt: MQTT
    private var ledColor = "0,0,255"

    private val myListener =  object : GestureDetector.SimpleOnGestureListener() {

        val contentCoordinates = IntArray(2)

        override fun onSingleTapUp(e: MotionEvent?): Boolean {
            content_layout.getLocationInWindow(contentCoordinates)
            println("Top height: ${contentCoordinates[1]}")
            val rc = GestureParser.onDown(e?.rawX, e?.rawY)

            val drawerOpen = drawer_layout.isDrawerOpen(GravityCompat.START)
            if (!drawerOpen) mqtt.sendData("$rc,$ledColor")
            println("${e?.x}, ${e?.y}")
            return super.onSingleTapUp(e)
        }

        override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
            val data = RandomGenerator.getRandomLED()
            mqtt.sendData("-1,$data")
            return super.onFling(e1, e2, velocityX, velocityY)
        }
    }

    lateinit var gestureDetector: GestureDetector
    private var actionBarMenu: Menu? = null

    lateinit var canvas: HoldsCanvas


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        gestureDetector = GestureDetector(this, myListener)
        drawer_layout.setOnTouchListener { v, event ->
            gestureDetector.onTouchEvent(event)
        }

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

        mqtt = MQTT(this, "m20.cloudmqtt.com", "11957", "ayogkqnq", "_e4HiuI73ywB")
        mqtt.init()

        //Set content dimensions after content layout has loaded
        content_layout.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            val contentCoordinates = IntArray(2)
            mainImage.getLocationOnScreen(contentCoordinates)
            val displayMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(displayMetrics)
            GestureParser.setScreenDimensions(displayMetrics.heightPixels, displayMetrics.widthPixels, contentCoordinates[1])

            canvas.updateDimensions(GestureParser.getScreenDimensions())
        }

        canvas = HoldsCanvas(this)
        content_layout.addView(canvas)
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
                mqtt.sendData("-1,0,0,0")
                true
            }
            R.id.action_red -> {
                ledColor = "255,0,0"
                resetTopBarColors()
                item.setIcon(ic_menu_red_toggled)
                true
            }
            R.id.action_blue -> {
                ledColor = "0,0,255"
                resetTopBarColors()
                item.setIcon(ic_menu_blue_toggled)
                true
            }
            R.id.action_green -> {
                ledColor = "0,255,0"
                resetTopBarColors()
                item.setIcon(ic_menu_green_toggled)
                true
            }
            R.id.action_blank -> {
                ledColor = "0,0,0"
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
