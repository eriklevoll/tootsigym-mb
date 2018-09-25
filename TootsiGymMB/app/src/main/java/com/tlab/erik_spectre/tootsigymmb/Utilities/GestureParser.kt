package com.tlab.erik_spectre.tootsigymmb.Utilities

object GestureParser {

    var height = 0;
    var width = 0;
    private var topBarHeight = 0

    private const val ROW_SLOPE = -22.1226667 //-19.8596026
    private const val ROW_INTERCEPT = 21.85333 //19.75629139
    private const val COL_SLOPE = 12.889366 // 13.1291028
    private const val COL_INTERCEPT = -0.761547 // -1.03501094

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
        if (X == null) return ""
        if (Y == null) return ""

        return convertCoordToRC(X, Y)
    }

    private fun convertCoordToRC(x: Float, y: Float) : String {
        val relativeX = x / width
        val relativeY = y / height
        val row = Math.round(ROW_SLOPE * relativeY + ROW_INTERCEPT)
        val col = Math.round(COL_SLOPE * relativeX + COL_INTERCEPT)
        val cCol = (col+64).toChar()

        return "$cCol$row"
    }
}