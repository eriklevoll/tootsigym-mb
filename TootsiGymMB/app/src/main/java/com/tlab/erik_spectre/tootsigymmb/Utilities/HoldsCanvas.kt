package com.tlab.erik_spectre.tootsigymmb.Utilities

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.view.View

class HoldsCanvas (context: Context): View(context) {

    private var screenHeight    = 0
    private var screenWidth     = 0
    private var topBarHeight    = 0

    lateinit var mainCanvas: Canvas


    override fun onDraw(canvas: Canvas) {
        mainCanvas = canvas
        println("Canvas set")
        //super.onDraw(canvas)
    }

//    override fun onDraw (canvas: Canvas) {
//        println("Top bar Canvas: $topBarHeight")
//        canvas.drawARGB (0,255, 0, 255)
//        val width = width
//        val hieght = height
//        val brush1 = Paint ()
//        brush1.setARGB (255, 255, 0, 0)
//        println("Here: height: $height, width: $width")
//        brush1.style = Paint.Style.STROKE
//
//        canvas.drawCircle(82f, 920f-topBarHeight, 2.5f, brush1) //A1
//        canvas.drawCircle(82f, 170f-topBarHeight, 2.5f, brush1) //A18
//        canvas.drawCircle(547.5f, 920f-topBarHeight, 2.5f, brush1) // K1
//        canvas.drawCircle(547.5f, 170f-topBarHeight, 2.5f, brush1) //K18
//        canvas.drawCircle(175.5f, 170f-topBarHeight, 2.5f, brush1) // C18
//    }

    fun updateDimensions(screenDims: IntArray) {
        screenHeight    = screenDims[0]
        screenWidth     = screenDims[1]
        topBarHeight    = screenDims[2]
    }

    fun draw()
    {
        println("Top bar Canvas: $topBarHeight")
        mainCanvas.drawARGB (0,255, 0, 255)
        val width = width
        val hieght = height
        val brush1 = Paint ()
        brush1.setARGB (255, 255, 0, 0)
        println("Here: height: $height, width: $width")
        brush1.style = Paint.Style.STROKE

        mainCanvas.drawCircle(82f, 920f-topBarHeight, 2.5f, brush1) //A1
        mainCanvas.drawCircle(82f, 170f-topBarHeight, 2.5f, brush1) //A18
        mainCanvas.drawCircle(547.5f, 920f-topBarHeight, 2.5f, brush1) // K1
        mainCanvas.drawCircle(547.5f, 170f-topBarHeight, 2.5f, brush1) //K18
        mainCanvas.drawCircle(175.5f, 170f-topBarHeight, 2.5f, brush1) // C18
    }
}