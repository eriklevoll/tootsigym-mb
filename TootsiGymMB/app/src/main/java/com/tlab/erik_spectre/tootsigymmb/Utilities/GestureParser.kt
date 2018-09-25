package com.tlab.erik_spectre.tootsigymmb.Utilities

import java.lang.Exception

object GestureParser {

    var height = 0;
    var width = 0;
    private var topBarHeight = 0

    private const val ROW_SLOPE = -19.992
    private const val ROW_INTERCEPT = 19.72267
    private const val COL_SLOPE = 12.889366
    private const val COL_INTERCEPT = -0.761547

    fun setScreenDimensions(h: Int, w: Int, topBarH: Int) {
        height = h
        width = w
        topBarHeight = topBarH
        println("Dims set. H: $h, w: $w, top: $topBarHeight")
    }

    fun getScreenDimensions(): IntArray {
        return intArrayOf(height, width, topBarHeight)
    }

    fun onDown(X: Float?, Y: Float?): String {
    //fun onDown(X: Float?, Y: Float?): FloatArray {
        if (X == null) return ""
        if (Y == null) return ""

        return convertCoordToRC(X, Y)
    }

    private fun convertCoordToRC(x: Float, y: Float) : String {
    //private fun convertCoordToRC(x: Float, y: Float) : FloatArray {
        val relativeX = x / width
        val relativeY = (y - topBarHeight) / (height - topBarHeight)
        val row = Math.round(ROW_SLOPE * relativeY + ROW_INTERCEPT)
        val col = Math.round(COL_SLOPE * relativeX + COL_INTERCEPT)
        val cCol = (col+64).toChar()

        println("$cCol$row")

        //return convertRCtoCoord("$cCol$row")

        return "$cCol$row"
    }

    fun convertRCtoCoord(rc: String): FloatArray {
        return try {
            val col = rc.substring(0,1)[0].toInt() - 64
            val row = rc.substring(1,rc.length).toInt()

            val relX = (col - COL_INTERCEPT) / COL_SLOPE
            val relY = (row - ROW_INTERCEPT) / ROW_SLOPE

            val x = relX * width
            val y = relY * (height - topBarHeight)

//            val x = (COL_SLOPE_RC2PX * col + COL_INTERCEPT_RC2PX) * width
//            val y = (ROW_SLOPE_RC2PX * row + ROW_INTERCEPT_RC2PX) * (height + topBarHeight)

            println("Got $x, $y, $col, $row")

            floatArrayOf(x.toFloat(),y.toFloat())

        } catch (e: Exception) {
            println("Failed to convert $rc, ${e.message}")
            floatArrayOf(1f,1f)
        }
    }
}