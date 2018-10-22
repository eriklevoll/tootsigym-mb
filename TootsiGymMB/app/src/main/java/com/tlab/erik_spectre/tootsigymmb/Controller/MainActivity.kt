package com.tlab.erik_spectre.tootsigymmb.Controller

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.res.Configuration
import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.util.DisplayMetrics
import android.view.*
import android.widget.ListView
import android.widget.SeekBar
import android.widget.Toast
import com.example.erik_spectre.tootsigymmb.R
import com.example.erik_spectre.tootsigymmb.R.drawable.*
import com.tlab.erik_spectre.tootsigymmb.Model.Route
import com.tlab.erik_spectre.tootsigymmb.Utilities.MQTT
import com.tlab.erik_spectre.tootsigymmb.Utilities.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.io.*
import com.google.gson.GsonBuilder
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.internal.schedulers.IoScheduler
import kotlinx.coroutines.experimental.*
import org.jetbrains.anko.AnkoAsyncContext
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.doAsyncResult
import org.jetbrains.anko.uiThread
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import java.lang.Exception
import java.util.concurrent.Future


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, SeekBar.OnSeekBarChangeListener  {
    override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
        val vGrade = "V${p0?.progress}"
        val fontGrade = GradeMapping[vGrade]

        GradeTopText.text = vGrade
        GradeBottomText.text = fontGrade?.joinToString(" / ")
    }

    override fun onStartTrackingTouch(p0: SeekBar?) {
    }

    override fun onStopTrackingTouch(p0: SeekBar?) {
    }

    private lateinit var gestureDetector: GestureDetector

    private var ledColor = BLUE_COLOR
    private var actionBarMenu: Menu? = null
    private var viewMode = VIEWMODE_GRID
    private var UIInitialized = false

    private val gestureListener =  object : GestureDetector.SimpleOnGestureListener() {

        override fun onSingleTapUp(e: MotionEvent?): Boolean {
            if (viewMode == VIEWMODE_DATABASE) return super.onSingleTapUp(e)

            val rc = GestureParser.onDown(e?.rawX, e?.rawY)
            if (rc == "") return super.onSingleTapUp(e)

            val drawerOpen = drawer_layout.isDrawerOpen(GravityCompat.START)
            if (drawerOpen) return super.onSingleTapUp(e)

            MQTT.sendData("$rc,$ledColor")
            HoldsCanvas.addHold(rc, ledColor)

            return super.onSingleTapUp(e)
        }

        override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
            if (viewMode == VIEWMODE_DATABASE) return super.onScroll(e1, e2, distanceX, distanceY)
            val x = e1?.x ?: 0f
            if (x / GestureParser.width < 0.1) return super.onScroll(e1, e2, distanceX, distanceY)

            val rc = GestureParser.onDown(e2?.rawX, e2?.rawY, true)
            if (rc == "") return super.onScroll(e1, e2, distanceX, distanceY)

            MQTT.sendData("$rc,$ledColor")
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

        GradeSlider.setOnSeekBarChangeListener(this)

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

        HoldsCanvas.setCanvasImage(canvasImage)

        MQTT.init(this, "m20.cloudmqtt.com", "11957", "ayogkqnq", "_e4HiuI73ywB")

        //Set content dimensions after content layout has loaded
        content_layout.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            InitializeUI()
        }

        HoldsCanvas.updateCanvas()

        RoutesData.readRoutesFromInternal(this)
    }

    override fun onResume() {
        super.onResume()
        if (MQTT.initialized) {
            MQTT.checkConnection()
            MQTT.sendData("#status")
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            val contentCoordinates = IntArray(2)
            mainImage.getLocationOnScreen(contentCoordinates)
            GestureParser.height = mainImage.height + contentCoordinates[1]
            println("new height: ${mainImage.height + contentCoordinates[1]}")
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
                MQTT.sendData("-1,0,0,0")
                true
            }
            R.id.action_glow -> {
                val data = RandomGenerator.getRandomLED()
                MQTT.sendData("-1,$data")
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
                database_layout.visibility = View.GONE
                content_layout.setBackgroundResource(R.color.MBYellow)
                HoldsCanvas.updateCanvas()
            }
            R.id.nav_canvas -> {
                viewMode = VIEWMODE_GRID
                mainImage.visibility = View.VISIBLE
                canvasImage.visibility = View.VISIBLE
                database_layout.visibility = View.GONE
                content_layout.setBackgroundResource(R.color.MBYellow)
                HoldsCanvas.updateCanvas()
            }
            R.id.nav_database -> {
                viewMode = VIEWMODE_DATABASE
                mainImage.visibility = View.INVISIBLE
                canvasImage.visibility = View.INVISIBLE
                database_layout.visibility = View.VISIBLE
                content_layout.setBackgroundColor(Color.WHITE)
            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    fun routesCountClick(view: View) {
        RoutesData.getRoutesCount(this)
    }


    fun updateRoutesClick(view: View) {
        RoutesData.downloadRoutes(this)
    }

    fun routeOneClick(view: View) {
        //MQTT.sendData(RandomGenerator.getRandomRoute())
    }


    fun routeTwoClick(view: View) {
        HoldsCanvas.clear(true)
        //val grade = "V${GradeSlider.progress}"
        val route = RandomGenerator.getRandomFromDB("V${GradeSlider.progress}")
        setRouteDescription(route)
        val data = HoldsCanvas.addRouteFromDB(route)
//        MQTT.sendData(data)
    }

    fun setRouteDescription(route: Route?) {
        val routeData = route?.Data
        RouteNameText.text = routeData?.Name
        RouteGradeText.text = routeData?.Grade
        RouteIsBenchmarkText.text = "Is Benchmark: ${routeData?.IsBenchmark}"
        RouteSetterName.text = "${routeData?.Setter?.Firstname} ${routeData?.Setter?.Lastname}"
        RouteRatingText.text = "Rating: ${routeData?.Rating}"
        RouteUserRatingText.text = "User Rating: ${routeData?.UserRating}"
        RouteRepeatsText.text = "Repeats: ${routeData?.Repeats}"
    }

    fun setToast(text: String, short: Boolean = true) {
        val duration = if (short) Toast.LENGTH_SHORT else Toast.LENGTH_LONG
        Toast.makeText(this, text, duration).show()
    }
}