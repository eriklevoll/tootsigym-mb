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

            val u = e?.x?.toFloat()
            val u2 = e?.y?.toFloat()
            if (u == null) return super.onSingleTapUp(e)
            if (u2 == null) return super.onSingleTapUp(e)

//            canvas.drawCircle(82f, 920f-92, 2.5f, paint) //A1
//            canvas.drawCircle(82f, 170f-92, 2.5f, paint) //A18
//            canvas.drawCircle(547.5f, 920f-92, 2.5f, paint) // K1
//            canvas.drawCircle(547.5f, 170f-92, 2.5f, paint) //K18
//            canvas.drawCircle(175.5f, 170f-92, 2.5f, paint) // C18
            canvas.drawCircle(u, u2-94+30, 10f, paint) // C18
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

    lateinit var canvas: Canvas


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
        content_layout.addOnLayoutChangeListener { _, a, b, _, _, _, _, _, _ ->
            println("Changed: $a, $b")
            val contentCoordinates = IntArray(2)
            mainImage.getLocationOnScreen(contentCoordinates)
            val displayMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(displayMetrics)
            GestureParser.setScreenDimensions(displayMetrics.heightPixels, displayMetrics.widthPixels, contentCoordinates[1])
            println("screen. height: ${displayMetrics.heightPixels}, width: ${displayMetrics.widthPixels}")
            //canvas.updateDimensions(GestureParser.getScreenDimensions())
        }

        val myOptions = BitmapFactory.Options()
        myOptions.inDither = true
        myOptions.inScaled = true
        myOptions.inPreferredConfig = Bitmap.Config.ARGB_8888// important
        myOptions.inPurgeable = true

        var mPaint = Paint();
        mPaint.setAlpha(0);
        mPaint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
        mPaint.setAntiAlias(true);

        //val bmp = BitmapFactory.decodeResource(resources, R.drawable.moonboard, myOptions)
        //bmp.setHasAlpha(true);
        //val bitmap = bmp.copy(Bitmap.Config.ARGB_8888 , true);

        val conf = Bitmap.Config.ARGB_8888 // see other conf types
        val bitmap = Bitmap.createBitmap(600, 882, conf) // this creates a MUTABLE bitmap
        bitmap.setHasAlpha(true)

        canvas = Canvas(bitmap);
        canvas.drawBitmap(bitmap, 0f, 0f, mPaint);

        //if(bitmap.getPixel(0, 0)==Color.rgb(0xff, 0x00, 0xff))
        //{
            for (x in 0 until bitmap.width) {
                for (y in 0 until bitmap.height) {
                    //if(bitmap.getPixel(x, y)==Color.rgb(0xff, 0x00, 0xff))
                    //{
                        bitmap.setPixel(x, y,Color.TRANSPARENT);
                    //}
                }
            }
        //}
        val paint2 = Paint()
        paint2.isAntiAlias = true
        paint2.color = Color.BLUE
        canvas.drawCircle(82f, 920f-92, 2.5f, paint2) //A1
        //return


        //val myImageView = mainImage
        //content_layout.addView(myImageView);

        //val w = 600
        //val h = 882

        //val bmp = BitmapFactory.decodeResource(resources, R.color.transparent)

        //val conf = Bitmap.Config.ARGB_8888 // see other conf types
        //val bmp = Bitmap.createBitmap(w, h, conf) // this creates a MUTABLE bitmap
        //for (x in 0 until bmp.width) {
         //   for (y in 0 until bmp.height) {
         //       bmp.setPixel(x, y, Color.TRANSPARENT)
          //  }
       // }
        //Bitmap.createBitmap(bmp)
        //CanvasView.setImageResource(R.color.transparent)
        //val bmp = BitmapFactory.decodeResource(resources, R.drawable.moonboard, myOptions)
        //canvas = Canvas(bmp)
       // val paint = Paint()
        //paint.isAntiAlias = true
        //paint.color = Color.BLUE

        //canvas.drawRGB(255,255,255)

       // content_layout.draw(canvas)
        //content_layout.invalidate()


        //val workingBitmap = Bitmap.createBitmap(bmp)
        //val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)


        canvas = Canvas(bitmap)
        //canvas.drawARGB(0,50,255,255)
        canvas.drawCircle(60f, 50f, 4f, paint2)

        val imageView = canvasImage
        //imageView.setAdjustViewBounds(true)
        imageView.setImageBitmap(bitmap)

        println("Orig BMP size: ${bitmap.height}, ${bitmap.width}")
        //println("Orig BMP size: ${workingBitmap.height}, ${workingBitmap.width}")
        //println("Mutab BMP size: ${mutableBitmap.height}, ${mutableBitmap.width}")
        println("Canvas size: ${canvas.height}, ${canvas.width}")

        canvas.drawCircle(82f, 920f-92, 2.5f, paint2) //A1
        canvas.drawCircle(82f, 170f-92, 2.5f, paint2) //A18
        canvas.drawCircle(547.5f, 920f-92, 2.5f, paint2) // K1
        canvas.drawCircle(547.5f, 170f-92, 2.5f, paint2) //K18
        canvas.drawCircle(175.5f, 170f-92, 2.5f, paint2) // C18

        //canvas = HoldsCanvas(this)
        //content_layout.addView(canvas)
        //canvas.draw()
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
