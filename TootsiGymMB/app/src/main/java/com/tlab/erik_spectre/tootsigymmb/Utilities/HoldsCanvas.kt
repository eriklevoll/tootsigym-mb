package com.tlab.erik_spectre.tootsigymmb.Utilities

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.view.View

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
        canvas.drawCircle(547.5f, 920f-topBarHeight, 2.5f, brush1)
    }

    fun updateDimensions(screenDims: IntArray) {
        screenHeight    = screenDims[0]
        screenWidth     = screenDims[1]
        topBarHeight    = screenDims[2]
    }
}