package com.tlab.erik_spectre.tootsigymmb.Utilities

import android.annotation.SuppressLint
import android.graphics.*
import android.widget.ImageView
import com.tlab.erik_spectre.tootsigymmb.Utilities.GestureParser.height

@SuppressLint("StaticFieldLeak")
object CanvasData {
    var canvasPaint = Paint()
    var mainCanvas = Canvas()

    lateinit var canvasView: ImageView
    var holdsData: HashMap<String, Int> = hashMapOf()

    fun addHold(hold: String, color: Int) {
        holdsData[hold] = color
    }

    fun addHold(hold: String, color: String) {
        val c = HoldsCanvas.convertColorString(color)
        holdsData[hold] = c
    }

    fun removeHold(hold: String) {
        holdsData.remove(hold)
    }
}



object HoldsCanvas {

    //private lateinit var canvas: Canvas
    //private lateinit var canvasPaint: Paint
    //private var holdsData: HashMap<String, Int> = hashMapOf()
    //private var height = 0
    //private var canvasImage: ImageView = ImageView(this)

    fun init(screenSize: IntArray) {
        height = screenSize[0]
        val width = screenSize[1]
        val top = screenSize[2]

        val canvasPaint = Paint()
        canvasPaint.isAntiAlias = true
        canvasPaint.style = Paint.Style.STROKE
        canvasPaint.strokeWidth = STROKE_MULTIPLIER * height

        val bitmap = Bitmap.createBitmap(width, height-top, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        CanvasData.canvasPaint = canvasPaint
        CanvasData.mainCanvas = canvas

        //CanvasData.canvasView = canvasImage
        CanvasData.canvasView.setImageBitmap(bitmap)
    }
    fun setCanvasImage(image: ImageView) {
        CanvasData.canvasView = image
    }

//    fun addHold(hold: String, color: Int) {
//        holdsData[hold] = color
//    }
//
//    fun addHold(hold: String, color: String) {
//        val c = convertColorString(color)
//        holdsData[hold] = c
//    }
//
//    fun removeHold(hold: String) {
//        holdsData.remove(hold)
//    }

    fun drawCircle(x: Float, y: Float, color: Int) {
        CanvasData.canvasPaint.color = color
        CanvasData.mainCanvas.drawCircle(x, y, RADIUS_MULTIPLIER * height, CanvasData.canvasPaint)
        CanvasData.canvasView.invalidate()
    }

    fun drawHoldCircle(hold: String, color: Int) {
        val coords = GestureParser.convertRCtoCoord(hold)
        val x = coords[0]
        val y = coords[1]

        drawCircle(x, y, color)
    }

    fun drawHoldCircle(hold: String, c: String) {
        val coords = GestureParser.convertRCtoCoord(hold)
        val x = coords[0]
        val y = coords[1]

        val color = convertColorString(c)

        drawCircle(x, y, color)
    }

    fun convertColorString(c: String): Int {
        return when (c) {
            NO_COLOR -> Color.TRANSPARENT
            RED_COLOR -> Color.RED
            BLUE_COLOR -> Color.BLUE
            GREEN_COLOR -> Color.GREEN
            else -> Color.BLUE
        }
    }

    fun updateCanvas() {
        println(CanvasData.holdsData)
        clear(false)

        for ((hold, color) in CanvasData.holdsData) {
            println("$hold: $color")
            drawHoldCircle(hold, color)
        }
    }

    fun clear(clearData: Boolean = true) {
        if (clearData) CanvasData.holdsData.clear()
        CanvasData.mainCanvas.drawColor(0, PorterDuff.Mode.CLEAR)
    }
}