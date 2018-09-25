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
import com.tlab.erik_spectre.tootsigymmb.Utilities.GestureParser
import com.tlab.erik_spectre.tootsigymmb.Utilities.RandomGenerator
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.R.attr.bitmap
import android.opengl.ETC1.getHeight
import android.opengl.ETC1.getWidth
import com.tlab.erik_spectre.tootsigymmb.Utilities.HoldsCanvas


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
            println("${e?.x}, ${e?.rawY}")
            val paint = Paint()
            paint.isAntiAlias = true
            paint.color = Color.RED

            val u = e?.x
            val u2 = e?.rawY
            if (u == null) return super.onSingleTapUp(e)
            if (u2 == null) return super.onSingleTapUp(e)

//            canvas.drawCircle(82f, 920f-92, 2.5f, paint) //A1
//            canvas.drawCircle(82f, 170f-92, 2.5f, paint) //A18
//            canvas.drawCircle(547.5f, 920f-92, 2.5f, paint) // K1
//            canvas.drawCircle(547.5f, 170f-92, 2.5f, paint) //K18
//            canvas.drawCircle(175.5f, 170f-92, 2.5f, paint) // C18
            //canvas.drawCircle(u, u2-94+30, 10f, paint) // C18
            holdsCanvas.drawCircle(u, u2-94)
            mainImage.invalidate()
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

    lateinit var holdsCanvas: HoldsCanvas


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

        holdsCanvas = HoldsCanvas(canvasImage)

        //Set content dimensions after content layout has loaded
        content_layout.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            val contentCoordinates = IntArray(2)
            mainImage.getLocationOnScreen(contentCoordinates)

            val displayMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(displayMetrics)
            GestureParser.setScreenDimensions(displayMetrics.heightPixels, displayMetrics.widthPixels, contentCoordinates[1])

            println("screen. height: ${displayMetrics.heightPixels}, width: ${displayMetrics.widthPixels}")
            holdsCanvas.init(GestureParser.getScreenDimensions())
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
