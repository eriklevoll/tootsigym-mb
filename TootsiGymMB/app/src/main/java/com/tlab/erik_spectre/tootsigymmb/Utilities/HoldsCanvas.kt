package com.tlab.erik_spectre.tootsigymmb.Utilities

import android.graphics.*
import android.widget.ImageView

class HoldsCanvas (val canvasImage: ImageView) {

    lateinit var canvas: Canvas

    fun init(screenSize: IntArray) {
        val height = screenSize[0]
        val width = screenSize[1]
        val top = screenSize[2]

        val bitmap = Bitmap.createBitmap(width, height-top, Bitmap.Config.ARGB_8888)
        canvas = Canvas(bitmap)

        val imageView = canvasImage
        imageView.setImageBitmap(bitmap)
    }

    fun drawCircle(x: Float, y: Float) {
        val paint = Paint()
        paint.isAntiAlias = true
        paint.color = Color.BLUE
        canvas.drawCircle(x, y, 10f, paint)
    }
}