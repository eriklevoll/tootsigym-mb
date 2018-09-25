package com.tlab.erik_spectre.tootsigymmb.Utilities

import android.graphics.*
import android.widget.ImageView

private const val RADIUS_MULTIPLIER = 0.017f
private const val STROKE_MULTIPLIER = 0.0065f

class HoldsCanvas (val canvasImage: ImageView) {

    private lateinit var canvas: Canvas
    private lateinit var canvasPaint: Paint
    private var height = 0

    fun init(screenSize: IntArray) {
        height = screenSize[0]
        val width = screenSize[1]
        val top = screenSize[2]

        canvasPaint = Paint()
        canvasPaint.isAntiAlias = true
        canvasPaint.style = Paint.Style.STROKE
        canvasPaint.strokeWidth = STROKE_MULTIPLIER * height

        val bitmap = Bitmap.createBitmap(width, height-top, Bitmap.Config.ARGB_8888)
        canvas = Canvas(bitmap)

        val imageView = canvasImage
        imageView.setImageBitmap(bitmap)
    }

    fun drawCircle(x: Float, y: Float, color: Int) {
        canvasPaint.color = color
        canvas.drawCircle(x, y, RADIUS_MULTIPLIER * height, canvasPaint)
        canvasImage.invalidate()
    }

    fun drawHoldCircle(hold: String, color: Int) {
        val coords = GestureParser.convertRCtoCoord(hold)
        val x = coords[0]
        val y = coords[1]

        drawCircle(x, y, color)
    }
}