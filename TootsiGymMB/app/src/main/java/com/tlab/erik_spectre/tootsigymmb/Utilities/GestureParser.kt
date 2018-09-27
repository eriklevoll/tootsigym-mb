package com.tlab.erik_spectre.tootsigymmb.Utilities

import java.lang.Exception

object GestureParser {

    var height = 0
    var width = 0
    private var topBarHeight = 0
    private var previousRC = ""

    private const val ROW_SLOPE = -19.992
    private const val ROW_INTERCEPT = 19.72267
    private const val COL_SLOPE = 12.889366
    private const val COL_INTERCEPT = -0.761547

    fun setScreenDimensions(h: Int, w: Int, topBarH: Int) {
        height = h
        width = w
        topBarHeight = topBarH
    }

    fun getScreenDimensions(): IntArray {
        return intArrayOf(height, width, topBarHeight)
    }

    fun onDown(X: Float?, Y: Float?, scroll: Boolean = false): String {
        if (X == null) return ""
        if (Y == null) return ""

        val rc = convertCoordToRC(X, Y)

        return if (!scroll) rc
        else {
            if (sameAsPreviousRC(rc)) ""
            else rc
        }
    }

    private fun sameAsPreviousRC(rc: String) : Boolean {
        return if (rc == previousRC) true
        else {
            previousRC = rc
            false
        }
    }

    private fun convertCoordToRC(x: Float, y: Float) : String {
        val relativeX = x / width
        val relativeY = (y - topBarHeight) / (height - topBarHeight)
        val row = Math.round(ROW_SLOPE * relativeY + ROW_INTERCEPT)
        val col = Math.round(COL_SLOPE * relativeX + COL_INTERCEPT)
        val cCol = (col+64).toChar()

        return if (valid(row, col)) "$cCol$row"
        else ""
    }

    fun convertRCtoCoord(rc: String): FloatArray {
        return try {
            val col = rc.substring(0,1)[0].toInt() - 64
            val row = rc.substring(1,rc.length).toInt()

            val relX = (col - COL_INTERCEPT) / COL_SLOPE
            val relY = (row - ROW_INTERCEPT) / ROW_SLOPE

            val x = relX * width
            val y = relY * (height - topBarHeight)

            floatArrayOf(x.toFloat(),y.toFloat())

        } catch (e: Exception) {
            println("Failed to convert $rc, ${e.message}")
            floatArrayOf(1f,1f)
        }
    }

    private fun valid(row: Long, col: Long) : Boolean {
        var validIndex = true
        if (row < 1 || row > NUM_OF_ROWS) validIndex = false
        if (col < 1 || col > NUM_OF_COLS) validIndex = false

        return validIndex
    }
}