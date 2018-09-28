package com.tlab.erik_spectre.tootsigymmb.Controller

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
import com.tlab.erik_spectre.tootsigymmb.Utilities.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener  {

    private lateinit var mqtt: MQTT
    private lateinit var gestureDetector: GestureDetector

    private var ledColor = BLUE_COLOR
    private var actionBarMenu: Menu? = null
    private var viewMode = VIEWMODE_GRID
    private var UIInitialized = false

    private val gestureListener =  object : GestureDetector.SimpleOnGestureListener() {

        override fun onSingleTapUp(e: MotionEvent?): Boolean {
            if (viewMode == VIEWMODE_DATABASE) super.onSingleTapUp(e)
            val rc = GestureParser.onDown(e?.rawX, e?.rawY)
            if (rc == "") return super.onSingleTapUp(e)

            val drawerOpen = drawer_layout.isDrawerOpen(GravityCompat.START)
            if (drawerOpen) return super.onSingleTapUp(e)

            mqtt.sendData("$rc,$ledColor")
            HoldsCanvas.addHold(rc, ledColor)

            return super.onSingleTapUp(e)
        }

        override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
            if (viewMode == VIEWMODE_DATABASE) return super.onScroll(e1, e2, distanceX, distanceY)
            val x = e1?.x ?: 0f
            if (x / GestureParser.width < 0.1) return super.onScroll(e1, e2, distanceX, distanceY)

            val rc = GestureParser.onDown(e2?.rawX, e2?.rawY, true)
            if (rc == "") return super.onScroll(e1, e2, distanceX, distanceY)

            mqtt.sendData("$rc,$ledColor")
            HoldsCanvas.addHold(rc, ledColor)

            return super.onScroll(e1, e2, distanceX, distanceY)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        gestureDetector = GestureDetector(this, gestureListener)
        drawer_layout.setOnTouchListener { _, event ->
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
            InitializeUI()
        }
    }

    private fun InitializeUI() {
        if (UIInitialized) return
        UIInitialized = true

        val contentCoordinates = IntArray(2)
        mainImage.getLocationOnScreen(contentCoordinates)

        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        GestureParser.setScreenDimensions(displayMetrics.heightPixels, displayMetrics.widthPixels, contentCoordinates[1])

        println("screen. height: ${displayMetrics.heightPixels}, width: ${displayMetrics.widthPixels}")

        HoldsCanvas.init(GestureParser.getScreenDimensions())
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
                HoldsCanvas.clear()
                mqtt.sendData("-1,0,0,0")
                true
            }
            R.id.action_glow -> {
                val data = RandomGenerator.getRandomLED()
                mqtt.sendData("-1,$data")
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

    private fun resetTopBarColors() {
        actionBarMenu?.getItem(0)?.setIcon(ic_menu_blank)
        actionBarMenu?.getItem(1)?.setIcon(ic_menu_green)
        actionBarMenu?.getItem(2)?.setIcon(ic_menu_blue)
        actionBarMenu?.getItem(3)?.setIcon(ic_menu_red)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_grid -> {
                viewMode = VIEWMODE_GRID
                mainImage.visibility = View.VISIBLE
                canvasImage.visibility = View.VISIBLE
                routeOneBtn.visibility = View.GONE
                routeTwoBtn.visibility = View.GONE
                HoldsCanvas.updateCanvas()
            }
            R.id.nav_canvas -> {
                viewMode = VIEWMODE_GRID
                mainImage.visibility = View.VISIBLE
                canvasImage.visibility = View.VISIBLE
                routeOneBtn.visibility = View.GONE
                routeTwoBtn.visibility = View.GONE
                HoldsCanvas.updateCanvas()
            }
            R.id.nav_database -> {
                viewMode = VIEWMODE_DATABASE
                mainImage.visibility = View.INVISIBLE
                canvasImage.visibility = View.INVISIBLE
                routeOneBtn.visibility = View.VISIBLE
                routeTwoBtn.visibility = View.VISIBLE
            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    fun routeOneClick(view: View) {
        mqtt.sendData(RandomGenerator.getRandomRoute())
    }

    fun routeTwoClick(view: View) {
        mqtt.sendData("J1;G1,E1,D1,C1,B1;A1,A2,B2,C2,D2")
    }
}
