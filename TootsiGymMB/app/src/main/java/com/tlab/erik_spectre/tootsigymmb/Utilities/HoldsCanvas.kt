package com.tlab.erik_spectre.tootsigymmb.Utilities

import android.annotation.SuppressLint
import android.graphics.*
import android.widget.ImageView
import com.tlab.erik_spectre.tootsigymmb.Utilities.GestureParser.height


@SuppressLint("StaticFieldLeak")
object HoldsCanvas {
    var canvasPaint = Paint()
    var mainCanvas = Canvas()

    lateinit var canvasView: ImageView
    var holdsData: HashMap<String, Int> = hashMapOf()

    fun addHold(hold: String, color: Int) {
        holdsData[hold] = color
        HoldsCanvas.updateCanvas()
    }

    fun addHold(hold: String, color: String, update: Boolean = true) {
        val c = HoldsCanvas.convertColorString(color)
        holdsData[hold] = c
        if (update) HoldsCanvas.updateCanvas()
    }

    fun removeHold(hold: String) {
        holdsData.remove(hold)
        HoldsCanvas.updateCanvas()
    }
    fun init(screenSize: IntArray) {
        height = screenSize[0]
        val width = screenSize[1]
        val top = screenSize[2]

        canvasPaint = Paint()
        canvasPaint.isAntiAlias = true
        canvasPaint.style = Paint.Style.STROKE
        canvasPaint.strokeWidth = STROKE_MULTIPLIER * height

        val bitmap = Bitmap.createBitmap(width, height-top, Bitmap.Config.ARGB_8888)
        mainCanvas = Canvas(bitmap)
        canvasView.setImageBitmap(bitmap)
    }
    fun setCanvasImage(image: ImageView) {
        canvasView = image
    }

    fun drawCircle(x: Float, y: Float, color: Int, updating: Boolean = false) {
        canvasPaint.color = color
        mainCanvas.drawCircle(x, y, RADIUS_MULTIPLIER * height, canvasPaint)
        if (!updating) canvasView.invalidate()
    }

    fun drawHoldCircle(hold: String, color: Int, updating: Boolean = false) {
        val coords = GestureParser.convertRCtoCoord(hold)
        val x = coords[0]
        val y = coords[1]

        drawCircle(x, y, color, updating)
    }

    fun drawHoldCircle(hold: String, c: String, updating: Boolean = false) {
        val coords = GestureParser.convertRCtoCoord(hold)
        val x = coords[0]
        val y = coords[1]

        val color = convertColorString(c)

        drawCircle(x, y, color, updating)
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
        clear(false)

        for ((hold, color) in holdsData) {
            drawHoldCircle(hold, color, true)
        }
        canvasView.invalidate()
    }

    fun clear(clearData: Boolean = true) {
        if (clearData) holdsData.clear()
        mainCanvas.drawColor(0, PorterDuff.Mode.CLEAR)
        canvasView.invalidate()
    }
}